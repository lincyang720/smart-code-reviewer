package com.agentcourse.reviewer.service;

import com.agentcourse.reviewer.model.ReviewRequest;
import com.agentcourse.reviewer.model.ReviewResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubWebhookService {

    @Value("${app.github.webhook-secret:}")
    private String webhookSecret;

    private final MultiAgentReviewService reviewService;
    private final GitHubPullRequestService gitHubPullRequestService;
    private final ReviewCommentFormatter reviewCommentFormatter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean verifySignature(String payload, String signature256) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("未配置 GitHub webhook secret，跳过签名校验");
            return true;
        }
        if (signature256 == null || !signature256.startsWith("sha256=")) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String actual = "sha256=" + toHex(digest);
            return MessageDigest.isEqual(actual.getBytes(StandardCharsets.UTF_8), signature256.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("GitHub 签名校验失败", e);
            return false;
        }
    }

    @Async
    public void handlePullRequestEvent(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String action = root.path("action").asText();
            if (!("opened".equals(action) || "synchronize".equals(action) || "reopened".equals(action))) {
                log.info("忽略 PR 动作: {}", action);
                return;
            }

            String repoFullName = root.path("repository").path("full_name").asText();
            int prNumber = root.path("pull_request").path("number").asInt();
            String title = root.path("pull_request").path("title").asText();
            String body = root.path("pull_request").path("body").asText("");
            String diffUrl = root.path("pull_request").path("diff_url").asText();

            log.info("开始异步处理 PR webhook: {}/{}", repoFullName, prNumber);
            String diff = gitHubPullRequestService.fetchDiff(diffUrl);
            if (diff.isBlank()) {
                log.warn("PR diff 为空，跳过自动审查。repo={} pr={} diffUrl={}", repoFullName, prNumber, diffUrl);
                return;
            }

            ReviewRequest request = new ReviewRequest();
            request.setRepository(repoFullName);
            request.setPrNumber(prNumber);
            request.setTitle(title);
            request.setDescription(body);
            request.setDiff(diff);

            ReviewResult result = reviewService.review(request);
            String comment = reviewCommentFormatter.format(result);
            gitHubPullRequestService.postComment(repoFullName, prNumber, comment);
        } catch (Exception e) {
            log.error("处理 GitHub PR webhook 失败", e);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
