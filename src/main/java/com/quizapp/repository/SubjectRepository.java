package com.quizapp.repository;

import com.quizapp.entity.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<SubjectEntity, String> {
    Optional<SubjectEntity> findByCodeIgnoreCase(String code);
}
