package com.quizapp.repository;

import com.quizapp.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    List<UserEntity> findByRole(String role);
    Optional<UserEntity> findByEmailIgnoreCase(String email);
    Optional<UserEntity> findByEnrolmentNumberIgnoreCase(String enrolmentNumber);
    Optional<UserEntity> findByRoleAndEmailIgnoreCase(String role, String email);
    Optional<UserEntity> findByRoleAndEnrolmentNumberIgnoreCase(String role, String enrolmentNumber);
}
