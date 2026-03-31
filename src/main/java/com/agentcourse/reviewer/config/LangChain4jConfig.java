package com.agentcourse.reviewer.config;

import com.agentcourse.reviewer.agent.CodeReviewAgent;
import com.agentcourse.reviewer.agent.LogicReviewAgent;
import com.agentcourse.reviewer.agent.PRSupervisorAgent;
import com.agentcourse.reviewer.agent.PerformanceReviewAgent;
import com.agentcourse.reviewer.agent.ReportSynthesisAgent;
import com.agentcourse.reviewer.agent.SecurityReviewAgent;
import com.agentcourse.reviewer.agent.StyleReviewAgent;
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

    @Bean
    public PRSupervisorAgent prSupervisorAgent(OpenAiChatModel chatModel) {
        return AiServices.builder(PRSupervisorAgent.class)
            .chatLanguageModel(chatModel)
            .build();
    }

    @Bean
    public SecurityReviewAgent securityReviewAgent(OpenAiChatModel chatModel) {
        return AiServices.builder(SecurityReviewAgent.class)
            .chatLanguageModel(chatModel)
            .build();
    }

    @Bean
    public StyleReviewAgent styleReviewAgent(OpenAiChatModel chatModel) {
        return AiServices.builder(StyleReviewAgent.class)
            .chatLanguageModel(chatModel)
            .build();
    }

    @Bean
    public LogicReviewAgent logicReviewAgent(OpenAiChatModel chatModel) {
        return AiServices.builder(LogicReviewAgent.class)
            .chatLanguageModel(chatModel)
            .build();
    }

    @Bean
    public PerformanceReviewAgent performanceReviewAgent(OpenAiChatModel chatModel) {
        return AiServices.builder(PerformanceReviewAgent.class)
            .chatLanguageModel(chatModel)
            .build();
    }

    @Bean
    public ReportSynthesisAgent reportSynthesisAgent(OpenAiChatModel chatModel) {
        return AiServices.builder(ReportSynthesisAgent.class)
            .chatLanguageModel(chatModel)
            .build();
    }
}
