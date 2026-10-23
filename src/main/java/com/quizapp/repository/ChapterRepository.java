package com.quizapp.repository;

import com.quizapp.entity.ChapterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<ChapterEntity, String> {
    List<ChapterEntity> findBySubjectId(String subjectId);
}
