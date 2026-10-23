package com.quizapp.service;

import com.quizapp.entity.AssessmentEntity;
import com.quizapp.entity.NotificationEntity;
import com.quizapp.entity.ReattemptRequestEntity;
import com.quizapp.entity.StudentExamAttemptEntity;
import com.quizapp.repository.AssessmentRepository;
import com.quizapp.repository.NotificationRepository;
import com.quizapp.repository.ReattemptRequestRepository;
import com.quizapp.repository.StudentExamAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ReattemptService {

    private final ReattemptRequestRepository requestRepository;
    private final StudentExamAttemptRepository attemptRepository;
    private final AssessmentRepository assessmentRepository;
    private final NotificationRepository notificationRepository;

    @Autowired
    public ReattemptService(ReattemptRequestRepository requestRepository,
                            StudentExamAttemptRepository attemptRepository,
                            AssessmentRepository assessmentRepository,
                            NotificationRepository notificationRepository) {
        this.requestRepository = requestRepository;
        this.attemptRepository = attemptRepository;
        this.assessmentRepository = assessmentRepository;
        this.notificationRepository = notificationRepository;
    }

    public List<ReattemptRequestEntity> getAllRequests(String studentId, String teacherId) {
        if (studentId != null && !studentId.trim().isEmpty()) {
            return requestRepository.findByStudentId(studentId);
        }
        if (teacherId != null && !teacherId.trim().isEmpty()) {
            return requestRepository.findByTeacherId(teacherId);
        }
        return requestRepository.findAll();
    }

    @Transactional
    public ReattemptRequestEntity submitRequest(ReattemptRequestEntity request) {
        if (request.getId() == null || request.getId().trim().isEmpty()) {
            request.setId("req-" + System.currentTimeMillis());
        }
        request.setRequestedAt(Instant.now().toString());
        request.setStatus("pending");

        // Update student attempt flag
        if (request.getAttemptId() != null) {
            attemptRepository.findById(request.getAttemptId()).ifPresent(att -> {
                att.setReattemptRequested(true);
                attemptRepository.save(att);
            });
        }

        // Notify teacher
        NotificationEntity notif = new NotificationEntity(
                "notif-req-" + System.currentTimeMillis(),
                "📩 Re-attempt Requested by " + request.getStudentName(),
                request.getStudentName() + " (" + request.getEnrolmentNumber() + ") is requesting a re-attempt for " + request.getAssessmentTitle() + ". Reason: " + request.getReason(),
                "warning",
                Instant.now().toString(),
                false,
                "teacher",
                "malpractice"
        );
        notificationRepository.save(notif);

        return requestRepository.save(request);
    }

    @Transactional
    public ReattemptRequestEntity resolveRequest(String requestId, String status, String paperMode) {
        ReattemptRequestEntity req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Reattempt request not found: " + requestId));

        req.setStatus(status);
        req.setResolvedAt(Instant.now().toString());
        req.setPaperMode(paperMode != null ? paperMode : "same");

        if ("approved".equalsIgnoreCase(status) && req.getAttemptId() != null) {
            attemptRepository.findById(req.getAttemptId()).ifPresent(att -> {
                att.setStatus("in_progress");
                att.setMalpracticeCount(0);
                att.getMalpracticeLogs().clear();
                att.setReattemptAllowed(true);
                att.setReattemptPaperMode(paperMode != null ? paperMode : "same");
                att.setReattemptRequested(false);
                att.setAnswersJson("[]");
                attemptRepository.save(att);
            });
        }

        return requestRepository.save(req);
    }
}
