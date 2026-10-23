package com.quizapp.repository;

import com.quizapp.entity.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, String> {
    List<QuestionEntity> findBySubjectId(String subjectId);
    List<QuestionEntity> findBySubjectIdAndChapterId(String subjectId, String chapterId);
}
