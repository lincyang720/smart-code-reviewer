package com.agentcourse.reviewer.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 代码审查结果
 */
@Data
@Builder
public class ReviewResult {

    /** 总体评分 0-100 */
    private int score;

    /** 总体评价摘要 */
    private String summary;

    /** 安全审查结果 */
    private String securityReview;

    /** 代码规范审查结果 */
    private String styleReview;

    /** 逻辑缺陷审查结果 */
    private String logicReview;

    /** 性能问题审查结果 */
    private String performanceReview;

    /** 具体改进建议列表 */
    private List<String> suggestions;

    /** 是否通过审查（score >= 60） */
    public boolean isPassed() {
        return score >= 60;
    }
}
