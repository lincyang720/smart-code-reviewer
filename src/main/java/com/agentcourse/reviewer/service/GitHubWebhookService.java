package com.agentcourse.reviewer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubWebhookService {

    @Value("${app.github.webhook-secret:}")
    private String webhookSecret;

    @Value("${app.github.token:}")
    private String githubToken;

    private final MultiAgentReviewService reviewService;
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
            return actual.equals(signature256);
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
            log.info("当前阶段仅完成Webhook骨架，后续补充diff抓取与自动回评。diffUrl={}", diffUrl);

            if (githubToken != null && !githubToken.isBlank()) {
                GitHub github = GitHub.connectUsingOAuth(githubToken);
                GHRepository repository = github.getRepository(repoFullName);
                repository.getPullRequest(prNumber).comment("Claude Code 已收到该 PR 的审查请求。当前处于阶段二开发中，完整自动审查与评论回写将在后续提交中完善。");
            }
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
