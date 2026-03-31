package com.agentcourse.reviewer.service;

import com.agentcourse.reviewer.agent.*;
import com.agentcourse.reviewer.model.AgentReviewResult;
import com.agentcourse.reviewer.model.ReviewPlan;
import com.agentcourse.reviewer.model.ReviewRequest;
import com.agentcourse.reviewer.model.ReviewResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class MultiAgentReviewService {

    private final PRSupervisorAgent supervisorAgent;
    private final SecurityReviewAgent securityReviewAgent;
    private final StyleReviewAgent styleReviewAgent;
    private final LogicReviewAgent logicReviewAgent;
    private final PerformanceReviewAgent performanceReviewAgent;
    private final ReportSynthesisAgent reportSynthesisAgent;
    private final TeamNormsMemory teamNormsMemory;
    private final ReviewHistoryService reviewHistoryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReviewResult review(ReviewRequest request) {
        log.info("开始多Agent审查 PR #{} - {}", request.getPrNumber(), request.getTitle());

        ReviewPlan plan = createPlan(request);
        List<String> retrievedNorms = teamNormsMemory.retrieveRelevantNorms(request.getDiff());
        List<CompletableFuture<AgentReviewResult>> futures = dispatchTasks(plan, request, retrievedNorms);

        List<AgentReviewResult> agentResults = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        ReviewResult result = synthesize(request, plan, agentResults, retrievedNorms);
        persistArtifacts(request, plan, retrievedNorms, result);
        learnNormsFromResult(request, result);
        return result;
    }

    private ReviewPlan createPlan(ReviewRequest request) {
        String raw = supervisorAgent.analyze(
            request.getRepository(),
            request.getTitle(),
            request.getDescription() == null ? "" : request.getDescription(),
            truncateDiff(request.getDiff())
        );

        try {
            JsonNode node = extractJson(raw);
            ReviewPlan plan = new ReviewPlan();
            plan.setChangeType(node.path("changeType").asText());
            plan.setRiskLevel(node.path("riskLevel").asText());
            plan.setReviewTasks(readStringList(node.path("reviewTasks")));
            plan.setFocusAreas(readStringList(node.path("focusAreas")));
            plan.setSummary(node.path("summary").asText());
            return plan;
        } catch (Exception e) {
            log.error("Supervisor 结果解析失败，使用默认审查计划", e);
            ReviewPlan fallback = new ReviewPlan();
            fallback.setChangeType(request.getChangeType() == null ? "feature" : request.getChangeType());
            fallback.setRiskLevel("MEDIUM");
            fallback.setReviewTasks(List.of("security", "style", "logic", "performance"));
            fallback.setFocusAreas(List.of());
            fallback.setSummary("默认全量审查");
            return fallback;
        }
    }

    private List<CompletableFuture<AgentReviewResult>> dispatchTasks(ReviewPlan plan, ReviewRequest request, List<String> retrievedNorms) {
        List<CompletableFuture<AgentReviewResult>> futures = new ArrayList<>();
        String titleWithNorms = enrichTitleWithNorms(request.getTitle(), retrievedNorms);
        for (String task : plan.getReviewTasks()) {
            switch (task) {
                case "security" -> futures.add(CompletableFuture.supplyAsync(() ->
                    parseAgentResult("security", securityReviewAgent.review(titleWithNorms, request.getDiff()))));
                case "style" -> futures.add(CompletableFuture.supplyAsync(() ->
                    parseAgentResult("style", styleReviewAgent.review(titleWithNorms, request.getDiff()))));
                case "logic" -> futures.add(CompletableFuture.supplyAsync(() ->
                    parseAgentResult("logic", logicReviewAgent.review(titleWithNorms, request.getDiff()))));
                case "performance" -> futures.add(CompletableFuture.supplyAsync(() ->
                    parseAgentResult("performance", performanceReviewAgent.review(titleWithNorms, request.getDiff()))));
                default -> log.warn("未知审查任务: {}", task);
            }
        }
        return futures;
    }

    private AgentReviewResult parseAgentResult(String agentName, String raw) {
        try {
            JsonNode node = extractJson(raw);
            return AgentReviewResult.builder()
                .agentName(agentName)
                .level(node.path("level").asText("NONE"))
                .issues(readStringList(node.path("issues")))
                .suggestions(readStringList(node.path("suggestions")))
                .summary(node.path("summary").asText())
                .score(node.has("performance_score") ? node.path("performance_score").asInt() : null)
                .build();
        } catch (Exception e) {
            log.error("{} Agent 结果解析失败", agentName, e);
            return AgentReviewResult.builder()
                .agentName(agentName)
                .level("NONE")
                .issues(List.of())
                .suggestions(List.of())
                .summary("解析失败")
                .score(null)
                .build();
        }
    }

    private ReviewResult synthesize(ReviewRequest request, ReviewPlan plan, List<AgentReviewResult> results, List<String> retrievedNorms) {
        try {
            String raw = reportSynthesisAgent.synthesize(
                enrichTitleWithNorms(request.getTitle(), retrievedNorms),
                objectMapper.writeValueAsString(plan),
                objectMapper.writeValueAsString(results)
            );
            JsonNode node = extractJson(raw);
            return ReviewResult.builder()
                .score(node.path("score").asInt(0))
                .summary(node.path("summary").asText())
                .securityReview(node.path("security").asText())
                .styleReview(node.path("style").asText())
                .logicReview(node.path("logic").asText())
                .performanceReview(node.path("performance").asText())
                .suggestions(readStringList(node.path("suggestions")))
                .build();
        } catch (Exception e) {
            log.error("汇总报告解析失败", e);
            return ReviewResult.builder()
                .score(0)
                .summary("汇总报告生成失败")
                .securityReview("失败")
                .styleReview("失败")
                .logicReview("失败")
                .performanceReview("失败")
                .suggestions(List.of())
                .build();
        }
    }

    private JsonNode extractJson(String raw) throws Exception {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        String json = (start >= 0 && end >= 0) ? raw.substring(start, end + 1) : raw;
        return objectMapper.readTree(json);
    }

    private List<String> readStringList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node != null && node.isArray()) {
            node.forEach(item -> list.add(item.asText()));
        }
        return list;
    }

    private String truncateDiff(String diff) {
        if (diff == null) {
            return "";
        }
        return diff.length() <= 10000 ? diff : diff.substring(0, 10000);
    }

    private String enrichTitleWithNorms(String title, List<String> retrievedNorms) {
        if (retrievedNorms == null || retrievedNorms.isEmpty()) {
            return title;
        }
        return title + "\n团队历史规范参考：" + String.join("；", retrievedNorms);
    }

    private void persistArtifacts(ReviewRequest request, ReviewPlan plan, List<String> retrievedNorms, ReviewResult result) {
        reviewHistoryService.save(request, plan.getRiskLevel(), retrievedNorms, result);
    }

    private void learnNormsFromResult(ReviewRequest request, ReviewResult result) {
        if (result.getSuggestions() == null || result.getSuggestions().isEmpty()) {
            return;
        }
        result.getSuggestions().stream()
            .filter(suggestion -> suggestion != null && !suggestion.isBlank())
            .limit(3)
            .forEach(suggestion -> teamNormsMemory.rememberNorm(suggestion, request.getDiff()));
    }
}
