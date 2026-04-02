package com.agentcourse.reviewer;

import com.agentcourse.reviewer.model.ReviewResult;
import com.agentcourse.reviewer.service.ReviewCommentFormatter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewCommentFormatterTest {

    private final ReviewCommentFormatter formatter = new ReviewCommentFormatter();

    @Test
    void shouldFormatFullMarkdownComment() {
        ReviewResult result = ReviewResult.builder()
            .score(82)
            .summary("整体实现清晰，建议补充边界校验。")
            .securityReview("未发现高危安全问题。")
            .styleReview("存在少量命名不统一。")
            .logicReview("空指针路径建议再收紧。")
            .performanceReview("性能风险较低。")
            .suggestions(List.of("补充空值判断", "统一变量命名"))
            .build();

        String markdown = formatter.format(result);

        assertThat(markdown).contains("审查通过 · 分数: 82/100");
        assertThat(markdown).contains("### 安全审查");
        assertThat(markdown).contains("- 补充空值判断");
        assertThat(markdown).contains("- 统一变量命名");
    }

    @Test
    void shouldRenderDefaultSuggestionWhenEmpty() {
        ReviewResult result = ReviewResult.builder()
            .score(40)
            .summary("存在明显问题。")
            .securityReview("需要修复鉴权缺失。")
            .styleReview("")
            .logicReview(null)
            .performanceReview("暂无明显性能问题。")
            .suggestions(List.of())
            .build();

        String markdown = formatter.format(result);

        assertThat(markdown).contains("审查未通过 · 分数: 40/100");
        assertThat(markdown).contains("暂无额外建议");
        assertThat(markdown).contains("暂无内容");
    }
}
