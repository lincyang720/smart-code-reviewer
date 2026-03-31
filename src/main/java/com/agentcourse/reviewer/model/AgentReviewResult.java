package com.agentcourse.reviewer.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 单个专项 Agent 的审查结果
 */
@Data
@Builder
public class AgentReviewResult {

    private String agentName;
    private String level;
    private List<String> issues;
    private List<String> suggestions;
    private String summary;
    private Integer score;
}
