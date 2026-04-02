package com.agentcourse.reviewer.service;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Service
public class GitHubPullRequestService {

    @Value("${app.github.token:}")
    private String githubToken;

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    public String fetchDiff(String diffUrl) {
        if (diffUrl == null || diffUrl.isBlank()) {
            return "";
        }

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(diffUrl))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/vnd.github.v3.diff")
                .GET();

            if (githubToken != null && !githubToken.isBlank()) {
                builder.header("Authorization", "Bearer " + githubToken);
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            }

            log.warn("拉取 GitHub diff 失败，status={} diffUrl={}", response.statusCode(), diffUrl);
            return "";
        } catch (Exception e) {
            log.error("拉取 GitHub diff 异常", e);
            return "";
        }
    }

    public void postComment(String repoFullName, int prNumber, String body) throws IOException {
        if (githubToken == null || githubToken.isBlank()) {
            log.warn("未配置 GitHub token，跳过 PR 评论回写");
            return;
        }
        GitHub github = GitHub.connectUsingOAuth(githubToken);
        GHRepository repository = github.getRepository(repoFullName);
        repository.getPullRequest(prNumber).comment(body);
    }
}
