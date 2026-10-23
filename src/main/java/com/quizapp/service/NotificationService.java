package com.quizapp.service;

import com.quizapp.entity.NotificationEntity;
import com.quizapp.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationEntity> getNotifications(String roleTarget) {
        if (roleTarget != null && !roleTarget.trim().isEmpty()) {
            return notificationRepository.findByRoleTargetOrderByTimestampDesc(roleTarget.trim().toLowerCase());
        }
        return notificationRepository.findAllByOrderByTimestampDesc();
    }

    public NotificationEntity markRead(String id) {
        return notificationRepository.findById(id).map(notif -> {
            notif.setRead(true);
            return notificationRepository.save(notif);
        }).orElseThrow(() -> new RuntimeException("Notification not found: " + id));
    }

    public void markAllRead() {
        List<NotificationEntity> all = notificationRepository.findAll();
        for (NotificationEntity n : all) {
            n.setRead(true);
        }
        notificationRepository.saveAll(all);
    }

    public NotificationEntity createNotification(String title, String message, String type, String roleTarget, String linkAction) {
        NotificationEntity notif = new NotificationEntity(
                "notif-" + System.currentTimeMillis(),
                title,
                message,
                type != null ? type : "info",
                Instant.now().toString(),
                false,
                roleTarget,
                linkAction
        );
        return notificationRepository.save(notif);
    }
}
