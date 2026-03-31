package com.agentcourse.reviewer.model;

import lombok.Data;

import java.util.List;

/**
 * Supervisor 制定的审查计划
 */
@Data
public class ReviewPlan {

    private String changeType;
    private String riskLevel;
    private List<String> reviewTasks;
    private List<String> focusAreas;
    private String summary;
}
