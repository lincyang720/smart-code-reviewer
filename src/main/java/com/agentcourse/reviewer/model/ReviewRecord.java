package com.agentcourse.reviewer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_record")
@Getter
@Setter
public class ReviewRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String repository;

    @Column(nullable = false)
    private Integer prNumber;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 50)
    private String changeType;

    @Column(length = 20)
    private String riskLevel;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private Boolean passed;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String securityReview;

    @Column(columnDefinition = "TEXT")
    private String styleReview;

    @Column(columnDefinition = "TEXT")
    private String logicReview;

    @Column(columnDefinition = "TEXT")
    private String performanceReview;

    @Column(columnDefinition = "TEXT")
    private String suggestionsJson;

    @Column(columnDefinition = "TEXT")
    private String retrievedNormsJson;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
