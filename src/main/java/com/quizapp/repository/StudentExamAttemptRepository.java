package com.quizapp.repository;

import com.quizapp.entity.StudentExamAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentExamAttemptRepository extends JpaRepository<StudentExamAttemptEntity, String> {
    List<StudentExamAttemptEntity> findByStudentId(String studentId);
    List<StudentExamAttemptEntity> findByAssessmentId(String assessmentId);
    Optional<StudentExamAttemptEntity> findByAssessmentIdAndStudentId(String assessmentId, String studentId);
    List<StudentExamAttemptEntity> findByClassId(String classId);
}
