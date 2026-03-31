package com.agentcourse.reviewer;

import com.agentcourse.reviewer.service.GitHubWebhookService;
import com.agentcourse.reviewer.service.MultiAgentReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GitHubWebhookServiceTest {

    @Test
    void shouldReturnTrueWhenSecretIsBlank() {
        GitHubWebhookService service = new GitHubWebhookService(mock(MultiAgentReviewService.class));
        ReflectionTestUtils.setField(service, "webhookSecret", "");

        boolean verified = service.verifySignature("{}", null);

        assertThat(verified).isTrue();
    }

    @Test
    void shouldReturnFalseWhenSignatureFormatIsInvalid() {
        GitHubWebhookService service = new GitHubWebhookService(mock(MultiAgentReviewService.class));
        ReflectionTestUtils.setField(service, "webhookSecret", "secret");

        boolean verified = service.verifySignature("{}", "invalid-signature");

        assertThat(verified).isFalse();
    }
}
