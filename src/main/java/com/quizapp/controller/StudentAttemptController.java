package com.quizapp.controller;

import com.quizapp.dto.MalpracticeRequest;
import com.quizapp.dto.SubmitExamRequest;
import com.quizapp.entity.StudentExamAttemptEntity;
import com.quizapp.service.ExamEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attempts")
public class StudentAttemptController {

    private final ExamEngineService examEngineService;

    @Autowired
    public StudentAttemptController(ExamEngineService examEngineService) {
        this.examEngineService = examEngineService;
    }

    @GetMapping
    public ResponseEntity<List<StudentExamAttemptEntity>> getAttempts(
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String assessmentId,
            @RequestParam(required = false) String classId) {
        return ResponseEntity.ok(examEngineService.getAttempts(studentId, assessmentId, classId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentExamAttemptEntity> getAttemptById(@PathVariable String id) {
        return examEngineService.getAttemptById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/start")
    public ResponseEntity<StudentExamAttemptEntity> startAttempt(
            @RequestParam String assessmentId,
            @RequestParam String studentId) {
        return ResponseEntity.ok(examEngineService.startAttempt(assessmentId, studentId));
    }

    @PostMapping("/{attemptId}/malpractice")
    public ResponseEntity<StudentExamAttemptEntity> recordMalpractice(
            @PathVariable String attemptId,
            @RequestBody MalpracticeRequest request) {
        return ResponseEntity.ok(examEngineService.recordMalpractice(attemptId, request.getType(), request.getDescription()));
    }

    @PostMapping("/{attemptId}/submit")
    public ResponseEntity<StudentExamAttemptEntity> submitAttempt(
            @PathVariable String attemptId,
            @RequestBody SubmitExamRequest request) {
        return ResponseEntity.ok(examEngineService.submitAttempt(attemptId, request));
    }
}
