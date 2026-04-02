package com.agentcourse.reviewer;

import dev.langchain4j.openai.spring.AutoConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = AutoConfig.class)
public class SmartCodeReviewerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartCodeReviewerApplication.class, args);
    }
}
