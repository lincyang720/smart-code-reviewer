package com.agentcourse.reviewer.controller;

import com.agentcourse.reviewer.model.ReviewRequest;
import com.agentcourse.reviewer.model.ReviewResult;
import com.agentcourse.reviewer.service.CodeReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 代码审查 REST API
 *
 * 阶段一提供两个端点：
 *   POST /api/review        手动触发审查（调试用）
 *   POST /api/webhook/github GitHub Webhook 接入（后续阶段完善签名校验）
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final CodeReviewService reviewService;

    /**
     * 手动提交 diff 触发审查
     */
    @PostMapping("/review")
    public ResponseEntity<ReviewResult> review(@RequestBody ReviewRequest request) {
        ReviewResult result = reviewService.review(request);
        return ResponseEntity.ok(result);
    }

    /**
     * GitHub Webhook 接入点（阶段一：仅接收 PR opened/synchronize 事件）
     * 后续阶段补充：HMAC-SHA256 签名校验、异步处理、自动回评论
     */
    @PostMapping("/webhook/github")
    public ResponseEntity<String> githubWebhook(
            @RequestHeader(value = "X-GitHub-Event", defaultValue = "") String event,
            @RequestBody String payload) {

        log.info("收到 GitHub 事件: {}", event);

        if (!"pull_request".equals(event)) {
            return ResponseEntity.ok("ignored");
        }

        // TODO 阶段二：解析 payload，提取 diff，异步触发审查，回写 PR Comment
        log.info("PR 事件已接收，待阶段二实现完整处理逻辑");
        return ResponseEntity.ok("received");
    }
}
