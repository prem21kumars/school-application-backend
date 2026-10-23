package com.quizapp.repository;

import com.quizapp.entity.OfflineAnswerSheetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfflineAnswerSheetRepository extends JpaRepository<OfflineAnswerSheetEntity, String> {
    List<OfflineAnswerSheetEntity> findByAssessmentId(String assessmentId);
    List<OfflineAnswerSheetEntity> findByStudentId(String studentId);
}
