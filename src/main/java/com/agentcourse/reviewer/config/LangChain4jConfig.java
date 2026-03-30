package com.agentcourse.reviewer.config;

import com.agentcourse.reviewer.agent.CodeReviewAgent;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChain4jConfig {

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:gpt-4o}")
    private String modelName;

    @Bean
    public OpenAiChatModel openAiChatModel() {
        return OpenAiChatModel.builder()
            .apiKey(apiKey)
            .modelName(modelName)
            .temperature(0.1)
            .maxTokens(2048)
            .build();
    }

    @Bean
    public CodeReviewAgent codeReviewAgent(OpenAiChatModel chatModel) {
        return AiServices.builder(CodeReviewAgent.class)
            .chatLanguageModel(chatModel)
            .build();
    }
}
