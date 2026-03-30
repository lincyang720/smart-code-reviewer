package com.agentcourse.reviewer.service;

import com.agentcourse.reviewer.agent.CodeReviewAgent;
import com.agentcourse.reviewer.model.ReviewRequest;
import com.agentcourse.reviewer.model.ReviewResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeReviewService {

    private final CodeReviewAgent codeReviewAgent;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReviewResult review(ReviewRequest request) {
        log.info("开始审查 PR #{} - {}", request.getPrNumber(), request.getTitle());

        String rawResult = codeReviewAgent.review(
            request.getRepository(),
            request.getTitle(),
            request.getChangeType() != null ? request.getChangeType() : "unknown",
            request.getDiff()
        );

        log.debug("Agent 原始返回: {}", rawResult);
        return parseResult(rawResult);
    }

    private ReviewResult parseResult(String raw) {
        try {
            // 提取 JSON 块（Agent 可能在 JSON 前后附带说明文字）
            int start = raw.indexOf('{');
            int end = raw.lastIndexOf('}');
            if (start == -1 || end == -1) {
                return fallbackResult(raw);
            }
            String json = raw.substring(start, end + 1);
            JsonNode node = objectMapper.readTree(json);

            List<String> suggestions = new ArrayList<>();
            JsonNode suggestionsNode = node.path("suggestions");
            if (suggestionsNode.isArray()) {
                suggestionsNode.forEach(s -> suggestions.add(s.asText()));
            }

            return ReviewResult.builder()
                .score(node.path("score").asInt(0))
                .summary(node.path("summary").asText())
                .securityReview(node.path("security").asText())
                .styleReview(node.path("style").asText())
                .logicReview(node.path("logic").asText())
                .performanceReview(node.path("performance").asText())
                .suggestions(suggestions)
                .build();
        } catch (Exception e) {
            log.error("解析 Agent 结果失败", e);
            return fallbackResult(raw);
        }
    }

    private ReviewResult fallbackResult(String raw) {
        return ReviewResult.builder()
            .score(0)
            .summary("审查结果解析失败，原始内容: " + raw)
            .securityReview("解析失败")
            .styleReview("解析失败")
            .logicReview("解析失败")
            .performanceReview("解析失败")
            .suggestions(List.of())
            .build();
    }
}
