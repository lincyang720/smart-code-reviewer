package com.agentcourse.reviewer.repository;

import com.agentcourse.reviewer.model.ReviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRecordRepository extends JpaRepository<ReviewRecord, Long> {

    List<ReviewRecord> findTop20ByRepositoryOrderByCreatedAtDesc(String repository);
}
