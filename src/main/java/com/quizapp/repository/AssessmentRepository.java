package com.quizapp.repository;

import com.quizapp.entity.AssessmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentRepository extends JpaRepository<AssessmentEntity, String> {
    List<AssessmentEntity> findByCreatedByTeacherId(String teacherId);
    List<AssessmentEntity> findByStatus(String status);
}
