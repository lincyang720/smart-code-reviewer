package com.agentcourse.reviewer.config;

import com.agentcourse.reviewer.service.ChromaDbTeamNormsMemory;
import com.agentcourse.reviewer.service.InMemoryTeamNormsMemory;
import com.agentcourse.reviewer.service.TeamNormsMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TeamNormsMemoryConfig {

    @Bean
    public TeamNormsMemory teamNormsMemory(
            @Value("${app.team-norms.provider:in-memory}") String provider,
            @Value("${app.team-norms.chromadb.base-url:http://localhost:8000}") String baseUrl,
            @Value("${app.team-norms.chromadb.collection:team-norms}") String collection) {
        if ("chromadb".equalsIgnoreCase(provider)) {
            return new ChromaDbTeamNormsMemory(baseUrl, collection);
        }
        return new InMemoryTeamNormsMemory();
    }
}
