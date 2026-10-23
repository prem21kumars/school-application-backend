package com.quizapp.repository;

import com.quizapp.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, String> {
    List<NotificationEntity> findByRoleTargetOrderByTimestampDesc(String roleTarget);
    List<NotificationEntity> findAllByOrderByTimestampDesc();
}
