package com.quizapp.repository;

import com.quizapp.entity.BookSourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookSourceRepository extends JpaRepository<BookSourceEntity, String> {
    List<BookSourceEntity> findBySubjectId(String subjectId);
}
