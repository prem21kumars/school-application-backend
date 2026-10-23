package com.quizapp.controller;

import com.quizapp.config.DataInitializer;
import com.quizapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final DataInitializer dataInitializer;
    private final UserRepository userRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SubjectRepository subjectRepository;
    private final AssessmentRepository assessmentRepository;
    private final StudentExamAttemptRepository attemptRepository;
    private final OfflineAnswerSheetRepository offlineSheetRepository;
    private final com.quizapp.service.GroqAiService groqAiService;
    private final com.quizapp.service.EmailService emailService;

    @Autowired
    public SystemController(DataInitializer dataInitializer,
                            UserRepository userRepository,
                            SchoolClassRepository schoolClassRepository,
                            SubjectRepository subjectRepository,
                            AssessmentRepository assessmentRepository,
                            StudentExamAttemptRepository attemptRepository,
                            OfflineAnswerSheetRepository offlineSheetRepository,
                            com.quizapp.service.GroqAiService groqAiService,
                            com.quizapp.service.EmailService emailService) {
        this.emailService = emailService;
        this.dataInitializer = dataInitializer;
        this.userRepository = userRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.subjectRepository = subjectRepository;
        this.assessmentRepository = assessmentRepository;
        this.attemptRepository = attemptRepository;
        this.offlineSheetRepository = offlineSheetRepository;
        this.groqAiService = groqAiService;
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetData() {
        dataInitializer.seedDatabase();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "All data has been reset to baseline defaults.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("status", "UP");
        stats.put("service", "Quiz & Assessment Management Backend");
        stats.put("database", "MySQL 8.0 (quiz_app_db)");
        stats.put("usersCount", userRepository.count());
        stats.put("classesCount", schoolClassRepository.count());
        stats.put("subjectsCount", subjectRepository.count());
        stats.put("assessmentsCount", assessmentRepository.count());
        stats.put("attemptsCount", attemptRepository.count());
        stats.put("offlineSheetsCount", offlineSheetRepository.count());
        stats.put("groqAiEnabled", groqAiService.isEnabled());
        stats.put("groqAiReady", groqAiService.isAvailable());
        stats.put("groqAiModel", groqAiService.getModel());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/email-config")
    public ResponseEntity<Map<String, Object>> getEmailConfig() {
        return ResponseEntity.ok(emailService.getEmailConfig());
    }

    @PostMapping("/email-config")
    public ResponseEntity<Map<String, Object>> saveEmailConfig(@RequestBody Map<String, Object> body) {
        String host = (String) body.get("host");
        int port = 587;
        if (body.get("port") != null) {
            try {
                port = Integer.parseInt(body.get("port").toString());
            } catch (Exception ignored) {}
        }
        String username = (String) body.get("username");
        String password = (String) body.get("password");

        Map<String, Object> res = emailService.saveEmailConfig(host, port, username, password);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/test-email")
    public ResponseEntity<Map<String, Object>> testEmail(@RequestBody Map<String, String> body) {
        String toEmail = body.get("toEmail");
        if (toEmail == null || toEmail.isBlank()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Recipient email address (toEmail) is required.");
            return ResponseEntity.badRequest().body(err);
        }
        Map<String, Object> res = emailService.testEmailConnection(toEmail.trim());
        return ResponseEntity.ok(res);
    }

}
