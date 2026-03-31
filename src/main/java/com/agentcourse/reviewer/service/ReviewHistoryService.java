package com.agentcourse.reviewer.service;

import com.agentcourse.reviewer.model.ReviewRecord;
import com.agentcourse.reviewer.model.ReviewRequest;
import com.agentcourse.reviewer.model.ReviewResult;
import com.agentcourse.reviewer.repository.ReviewRecordRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewHistoryService {

    private final ReviewRecordRepository reviewRecordRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void save(ReviewRequest request, String riskLevel, List<String> retrievedNorms, ReviewResult result) {
        ReviewRecord record = new ReviewRecord();
        record.setRepository(defaultString(request.getRepository()));
        record.setPrNumber(request.getPrNumber() == null ? 0 : request.getPrNumber());
        record.setTitle(defaultString(request.getTitle()));
        record.setChangeType(defaultString(request.getChangeType()));
        record.setRiskLevel(defaultString(riskLevel));
        record.setScore(result.getScore());
        record.setPassed(result.isPassed());
        record.setSummary(defaultString(result.getSummary()));
        record.setSecurityReview(defaultString(result.getSecurityReview()));
        record.setStyleReview(defaultString(result.getStyleReview()));
        record.setLogicReview(defaultString(result.getLogicReview()));
        record.setPerformanceReview(defaultString(result.getPerformanceReview()));
        record.setSuggestionsJson(toJson(result.getSuggestions()));
        record.setRetrievedNormsJson(toJson(retrievedNorms));
        record.setCreatedAt(LocalDateTime.now());
        reviewRecordRepository.save(record);
    }

    public List<ReviewRecord> recentRecords(String repository) {
        return reviewRecordRepository.findTop20ByRepositoryOrderByCreatedAtDesc(repository);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
