package com.agentcourse.reviewer;

import com.agentcourse.reviewer.model.ReviewRequest;
import com.agentcourse.reviewer.model.ReviewResult;
import com.agentcourse.reviewer.service.CodeReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 阶段一集成测试
 * 运行前需设置环境变量 OPENAI_API_KEY
 */
@SpringBootTest
@ActiveProfiles("test")
class CodeReviewServiceTest {

    @Autowired
    private CodeReviewService reviewService;

    @Test
    void shouldReturnReviewResultForSimpleDiff() {
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
        // 上面的 diff 有明显 SQL 注入，安全分应较低
        assertThat(result.getScore()).isLessThan(60);
        assertThat(result.getSecurityReview()).isNotBlank();
    }
}
