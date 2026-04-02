package com.agentcourse.reviewer.service;

import com.agentcourse.reviewer.model.ReviewResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReviewCommentFormatter {

    public String format(ReviewResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("## ")
            .append(result.isPassed() ? "审查通过" : "审查未通过")
            .append(" · 分数: ")
            .append(result.getScore())
            .append("/100\n\n");

        sb.append("### 总结\n")
            .append(valueOrDefault(result.getSummary()))
            .append("\n\n");

        appendSection(sb, "安全审查", result.getSecurityReview());
        appendSection(sb, "代码规范", result.getStyleReview());
        appendSection(sb, "逻辑分析", result.getLogicReview());
        appendSection(sb, "性能分析", result.getPerformanceReview());

        List<String> suggestions = result.getSuggestions();
        sb.append("### 改进建议\n");
        if (suggestions == null || suggestions.isEmpty()) {
            sb.append("- 暂无额外建议\n\n");
        } else {
            suggestions.stream()
                .filter(item -> item != null && !item.isBlank())
                .forEach(item -> sb.append("- ").append(item).append("\n"));
            sb.append("\n");
        }

        sb.append("---\n")
            .append("_以上内容由 Smart Code Reviewer 自动生成_\n");
        return sb.toString();
    }

    private void appendSection(StringBuilder sb, String title, String content) {
        sb.append("### ")
            .append(title)
            .append("\n")
            .append(valueOrDefault(content))
            .append("\n\n");
    }

    private String valueOrDefault(String value) {
        return value == null || value.isBlank() ? "暂无内容" : value;
    }
}
