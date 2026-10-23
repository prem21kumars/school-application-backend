package com.quizapp.repository;

import com.quizapp.entity.MalpracticeLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MalpracticeLogRepository extends JpaRepository<MalpracticeLogEntity, String> {
    List<MalpracticeLogEntity> findByAttemptId(String attemptId);
}
