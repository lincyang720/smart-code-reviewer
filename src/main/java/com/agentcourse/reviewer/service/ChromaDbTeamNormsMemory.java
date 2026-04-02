package com.agentcourse.reviewer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class ChromaDbTeamNormsMemory implements TeamNormsMemory {

    private final String baseUrl;
    private final String collectionName;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChromaDbTeamNormsMemory(String baseUrl, String collectionName) {
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.collectionName = collectionName;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    @Override
    public void rememberNorm(String normDescription, String example) {
        if (normDescription == null || normDescription.isBlank()) {
            return;
        }
        try {
            String collectionId = ensureCollection();
            String payload = objectMapper.writeValueAsString(new AddRequest(
                List.of(UUID.randomUUID().toString()),
                List.of(normDescription + "\n" + (example == null ? "" : example)),
                List.of(new Metadata(normDescription, example == null ? "" : example))
            ));
            post(baseUrl + "/api/v1/collections/" + collectionId + "/add", payload);
        } catch (Exception e) {
            log.error("写入 ChromaDB 规范失败", e);
        }
    }

    @Override
    public List<String> retrieveRelevantNorms(String codeContext) {
        if (codeContext == null || codeContext.isBlank()) {
            return List.of();
        }
        try {
            String collectionId = ensureCollection();
            String payload = objectMapper.writeValueAsString(new QueryRequest(List.of(codeContext), 5));
            String response = post(baseUrl + "/api/v1/collections/" + collectionId + "/query", payload);
            JsonNode root = objectMapper.readTree(response);
            JsonNode documents = root.path("documents");
            if (!documents.isArray() || documents.isEmpty() || !documents.get(0).isArray()) {
                return List.of();
            }

            List<String> result = new ArrayList<>();
            for (JsonNode document : documents.get(0)) {
                String value = document.asText();
                if (value == null || value.isBlank()) {
                    continue;
                }
                String firstLine = value.lines().findFirst().orElse(value).trim();
                if (!firstLine.isBlank()) {
                    result.add(firstLine);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("查询 ChromaDB 规范失败", e);
            return List.of();
        }
    }

    @Override
    public List<String> dumpAll() {
        return List.of();
    }

    private String ensureCollection() throws Exception {
        String response = post(baseUrl + "/api/v1/collections", objectMapper.writeValueAsString(new CreateCollectionRequest(collectionName)));
        JsonNode created = objectMapper.readTree(response);
        JsonNode idNode = created.path("id");
        if (!idNode.isMissingNode() && !idNode.asText().isBlank()) {
            return idNode.asText();
        }

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/v1/collections"))
            .timeout(Duration.ofSeconds(20))
            .GET()
            .build();
        HttpResponse<String> listResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode collections = objectMapper.readTree(listResponse.body());
        for (JsonNode item : collections) {
            if (collectionName.equals(item.path("name").asText())) {
                return item.path("id").asText();
            }
        }
        throw new IllegalStateException("无法定位 ChromaDB collection: " + collectionName);
    }

    private String post(String url, String payload) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(20))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        }
        if (response.statusCode() == 409) {
            return response.body();
        }
        throw new IllegalStateException("ChromaDB 请求失败: " + response.statusCode() + " body=" + response.body());
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private record CreateCollectionRequest(String name) {
    }

    private record Metadata(String normDescription, String example) {
    }

    private record AddRequest(List<String> ids, List<String> documents, List<Metadata> metadatas) {
    }

    private record QueryRequest(List<String> query_texts, int n_results) {
    }
}
