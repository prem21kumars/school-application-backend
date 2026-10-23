package com.quizapp.controller;

import com.quizapp.dto.TeacherOfflineMarksRequest;
import com.quizapp.entity.OfflineAnswerSheetEntity;
import com.quizapp.service.OfflineEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offline-sheets")
public class OfflineSheetController {

    private final OfflineEvaluationService offlineEvaluationService;

    @Autowired
    public OfflineSheetController(OfflineEvaluationService offlineEvaluationService) {
        this.offlineEvaluationService = offlineEvaluationService;
    }

    @GetMapping
    public ResponseEntity<List<OfflineAnswerSheetEntity>> getAllSheets(
            @RequestParam(required = false) String assessmentId,
            @RequestParam(required = false) String studentId) {
        return ResponseEntity.ok(offlineEvaluationService.getAllSheets(assessmentId, studentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfflineAnswerSheetEntity> getSheetById(@PathVariable String id) {
        return offlineEvaluationService.getSheetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OfflineAnswerSheetEntity> uploadSheet(@RequestBody OfflineAnswerSheetEntity sheet) {
        return ResponseEntity.ok(offlineEvaluationService.uploadSheet(sheet));
    }

    @PostMapping("/{id}/evaluate")
    public ResponseEntity<OfflineAnswerSheetEntity> runAIEvaluation(@PathVariable String id) {
        return ResponseEntity.ok(offlineEvaluationService.runAIEvaluation(id));
    }

    @PutMapping("/{id}/marks")
    public ResponseEntity<OfflineAnswerSheetEntity> updateTeacherMarks(
            @PathVariable String id,
            @RequestBody TeacherOfflineMarksRequest request) {
        return ResponseEntity.ok(offlineEvaluationService.updateTeacherMarks(id, request.getQuestionEvaluations()));
    }
}
