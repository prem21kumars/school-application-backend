package com.quizapp.controller;

import com.quizapp.dto.AIQuestionGenRequest;
import com.quizapp.dto.BookQuestionGenRequest;
import com.quizapp.dto.QuestionPaperSetDto;
import com.quizapp.entity.AssessmentEntity;
import com.quizapp.entity.QuestionEntity;
import com.quizapp.service.AssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;

    @Autowired
    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping
    public ResponseEntity<List<AssessmentEntity>> getAllAssessments() {
        return ResponseEntity.ok(assessmentService.getAllAssessments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssessmentEntity> getAssessmentById(@PathVariable String id) {
        return assessmentService.getAssessmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AssessmentEntity> createAssessment(@RequestBody AssessmentEntity assessment) {
        return ResponseEntity.ok(assessmentService.createAssessment(assessment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssessmentEntity> updateAssessment(@PathVariable String id, @RequestBody AssessmentEntity assessment) {
        return ResponseEntity.ok(assessmentService.updateAssessment(id, assessment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssessment(@PathVariable String id) {
        assessmentService.deleteAssessment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/sets")
    public ResponseEntity<List<QuestionPaperSetDto>> generateRandomizedSets(
            @PathVariable String id,
            @RequestParam(defaultValue = "3") int count) {
        return ResponseEntity.ok(assessmentService.generateRandomizedSets(id, count));
    }

    @PostMapping("/generate-ai-questions")
    public ResponseEntity<List<QuestionEntity>> generateAIQuestions(@RequestBody AIQuestionGenRequest request) {
        return ResponseEntity.ok(assessmentService.generateAIQuestions(request));
    }

    @PostMapping("/generate-book-questions")
    public ResponseEntity<List<QuestionEntity>> generateBookQuestions(@RequestBody BookQuestionGenRequest request) {
        return ResponseEntity.ok(assessmentService.generateBookQuestions(request));
    }
}
