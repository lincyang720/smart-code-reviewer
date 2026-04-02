package com.agentcourse.reviewer;

import com.agentcourse.reviewer.agent.CodeReviewAgent;
import com.agentcourse.reviewer.model.ReviewRequest;
import com.agentcourse.reviewer.model.ReviewResult;
import com.agentcourse.reviewer.service.CodeReviewService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 阶段一单元测试
 * 使用 Mock Agent，不依赖真实 OpenAI API key
 */
class CodeReviewServiceTest {

    @Test
    void shouldReturnReviewResultForSimpleDiff() {
        CodeReviewAgent mockAgent = mock(CodeReviewAgent.class);
        when(mockAgent.review(anyString(), anyString(), anyString(), anyString()))
            .thenReturn("""
                {
                  "score": 35,
                  "summary": "存在明显 SQL 注入漏洞",
                  "security": "使用了字符串拼接 SQL，存在注入风险",
                  "style": "命名规范",
                  "logic": "无边界检查",
                  "performance": "无明显性能问题",
                  "suggestions": ["使用 PreparedStatement"]
                }
                """);
        CodeReviewService reviewService = new CodeReviewService(mockAgent);

        ReviewRequest request = new ReviewRequest();
        request.setRepository("example/demo");
        request.setPrNumber(1);
        request.setTitle("Add user login feature");
        request.setChangeType("feature");
        request.setDiff("""
            +public String login(String username, String password) {
            +    String sql = "SELECT * FROM users WHERE username='" + username + "'";
            +    return jdbcTemplate.queryForObject(sql, String.class);
            +}
            """);

        ReviewResult result = reviewService.review(request);

        assertThat(result).isNotNull();
        assertThat(result.getScore()).isBetween(0, 100);
        assertThat(result.getScore()).isLessThan(60);
        assertThat(result.getSecurityReview()).isNotBlank();
    }
}
