package com.quizapp.repository;

import com.quizapp.entity.ReattemptRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReattemptRequestRepository extends JpaRepository<ReattemptRequestEntity, String> {
    List<ReattemptRequestEntity> findByStudentId(String studentId);
    List<ReattemptRequestEntity> findByTeacherId(String teacherId);
    List<ReattemptRequestEntity> findByStatus(String status);
}
