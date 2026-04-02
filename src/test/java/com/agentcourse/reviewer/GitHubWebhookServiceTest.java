package com.agentcourse.reviewer;

import com.agentcourse.reviewer.model.ReviewResult;
import com.agentcourse.reviewer.service.GitHubPullRequestService;
import com.agentcourse.reviewer.service.GitHubWebhookService;
import com.agentcourse.reviewer.service.MultiAgentReviewService;
import com.agentcourse.reviewer.service.ReviewCommentFormatter;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class GitHubWebhookServiceTest {

    @Test
    void shouldReturnTrueWhenSecretIsBlank() {
        GitHubWebhookService service = new GitHubWebhookService(
            mock(MultiAgentReviewService.class),
            mock(GitHubPullRequestService.class),
            mock(ReviewCommentFormatter.class)
        );
        ReflectionTestUtils.setField(service, "webhookSecret", "");

        boolean verified = service.verifySignature("{}", null);

        assertThat(verified).isTrue();
    }

    @Test
    void shouldReturnFalseWhenSignatureFormatIsInvalid() {
        GitHubWebhookService service = new GitHubWebhookService(
            mock(MultiAgentReviewService.class),
            mock(GitHubPullRequestService.class),
            mock(ReviewCommentFormatter.class)
        );
        ReflectionTestUtils.setField(service, "webhookSecret", "secret");

        boolean verified = service.verifySignature("{}", "invalid-signature");

        assertThat(verified).isFalse();
    }

    @Test
    void shouldSkipReviewWhenDiffIsEmpty() throws Exception {
        MultiAgentReviewService reviewService = mock(MultiAgentReviewService.class);
        GitHubPullRequestService pullRequestService = mock(GitHubPullRequestService.class);
        ReviewCommentFormatter formatter = mock(ReviewCommentFormatter.class);
        GitHubWebhookService service = new GitHubWebhookService(reviewService, pullRequestService, formatter);

        when(pullRequestService.fetchDiff(anyString())).thenReturn("");

        service.handlePullRequestEvent("""
            {
              "action": "opened",
              "repository": {"full_name": "demo/repo"},
              "pull_request": {
                "number": 12,
                "title": "feat: test webhook",
                "body": "body",
                "diff_url": "https://example.com/pr.diff"
              }
            }
            """);

        verify(reviewService, never()).review(any());
        verify(pullRequestService, never()).postComment(anyString(), anyInt(), anyString());
    }

    @Test
    void shouldRunReviewAndPostCommentWhenDiffExists() throws Exception {
        MultiAgentReviewService reviewService = mock(MultiAgentReviewService.class);
        GitHubPullRequestService pullRequestService = mock(GitHubPullRequestService.class);
        ReviewCommentFormatter formatter = mock(ReviewCommentFormatter.class);
        GitHubWebhookService service = new GitHubWebhookService(reviewService, pullRequestService, formatter);
        ReviewResult result = ReviewResult.builder()
            .score(75)
            .summary("ok")
            .build();

        when(pullRequestService.fetchDiff(anyString())).thenReturn("diff-content");
        when(reviewService.review(any())).thenReturn(result);
        when(formatter.format(result)).thenReturn("formatted-comment");

        service.handlePullRequestEvent("""
            {
              "action": "opened",
              "repository": {"full_name": "demo/repo"},
              "pull_request": {
                "number": 12,
                "title": "feat: test webhook",
                "body": "body",
                "diff_url": "https://example.com/pr.diff"
              }
            }
            """);

        verify(reviewService).review(any());
        verify(pullRequestService).postComment("demo/repo", 12, "formatted-comment");
    }
}
