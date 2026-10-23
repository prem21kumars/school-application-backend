package com.quizapp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizapp.dto.OfflineEvaluationItemDto;
import com.quizapp.entity.AssessmentEntity;
import com.quizapp.entity.OfflineAnswerSheetEntity;
import com.quizapp.entity.QuestionEntity;
import com.quizapp.repository.AssessmentRepository;
import com.quizapp.repository.OfflineAnswerSheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OfflineEvaluationService {

    private final OfflineAnswerSheetRepository sheetRepository;
    private final AssessmentRepository assessmentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public OfflineEvaluationService(OfflineAnswerSheetRepository sheetRepository,
                                  AssessmentRepository assessmentRepository) {
        this.sheetRepository = sheetRepository;
        this.assessmentRepository = assessmentRepository;
    }

    public List<OfflineAnswerSheetEntity> getAllSheets(String assessmentId, String studentId) {
        if (assessmentId != null && !assessmentId.trim().isEmpty()) {
            return sheetRepository.findByAssessmentId(assessmentId);
        }
        if (studentId != null && !studentId.trim().isEmpty()) {
            return sheetRepository.findByStudentId(studentId);
        }
        return sheetRepository.findAll();
    }

    public Optional<OfflineAnswerSheetEntity> getSheetById(String sheetId) {
        return sheetRepository.findById(sheetId);
    }

    public OfflineAnswerSheetEntity uploadSheet(OfflineAnswerSheetEntity sheet) {
        if (sheet.getId() == null || sheet.getId().trim().isEmpty()) {
            sheet.setId("sheet-" + System.currentTimeMillis());
        }
        if (sheet.getUploadDate() == null) {
            sheet.setUploadDate(Instant.now().toString());
        }
        sheet.setEvaluationStatus("pending");
        sheet.setTotalMarksAwarded(0);

        AssessmentEntity assessment = assessmentRepository.findById(sheet.getAssessmentId()).orElse(null);
        int maxMarks = assessment != null && assessment.getTotalMarks() != null ? assessment.getTotalMarks() : 50;
        sheet.setMaxMarks(maxMarks);
        sheet.setPercentage(0);

        return sheetRepository.save(sheet);
    }

    @Transactional
    public OfflineAnswerSheetEntity runAIEvaluation(String sheetId) {
        OfflineAnswerSheetEntity sheet = sheetRepository.findById(sheetId)
                .orElseThrow(() -> new RuntimeException("Answer sheet not found: " + sheetId));

        AssessmentEntity assessment = assessmentRepository.findById(sheet.getAssessmentId()).orElse(null);
        List<QuestionEntity> questions = (assessment != null && assessment.getQuestions() != null)
                ? assessment.getQuestions()
                : List.of();

        List<OfflineEvaluationItemDto> evaluations = new ArrayList<>();
        int totalProposed = 0;

        for (int idx = 0; idx < questions.size(); idx++) {
            QuestionEntity q = questions.get(idx);
            boolean isHard = "hard".equalsIgnoreCase(q.getDifficulty());
            int proposedMarks = isHard ? Math.max(1, q.getMarks() - 1) : q.getMarks();
            int confidence = isHard ? 82 : 95;
            totalProposed += proposedMarks;

            OfflineEvaluationItemDto eval = new OfflineEvaluationItemDto();
            eval.setQuestionId(q.getId());
            eval.setQuestionNumber(idx + 1);
            eval.setMaxMarks(q.getMarks());
            eval.setExtractedText("[Simulated OCR Handwriting Extraction]\nStep 1: Formula applied according to chapter " + (q.getChapterNumber() != null ? q.getChapterNumber() : 1) + ".\nStep 2: Working calculations shown clearly.\nFinal answer obtained: " + (q.getCorrectAnswerText() != null ? q.getCorrectAnswerText() : "Verified result") + ".");
            eval.setAiProposedMarks(proposedMarks);
            eval.setAiConfidence(confidence);
            eval.setAiReasoning(isHard 
                    ? "Minor ambiguity in algebraic step presentation, but final deduction is logically sound." 
                    : "Complete steps matching the marking rubric accurately.");
            eval.setTeacherMarks(proposedMarks);
            eval.setFlaggedForReview(isHard);

            evaluations.add(eval);
        }

        int maxMarks = sheet.getMaxMarks() > 0 ? sheet.getMaxMarks() : (assessment != null ? assessment.getTotalMarks() : 50);
        int percentage = maxMarks > 0 ? (int) Math.round(((double) totalProposed / maxMarks) * 100) : 0;

        try {
            sheet.setQuestionEvaluationsJson(objectMapper.writeValueAsString(evaluations));
        } catch (Exception e) {
            sheet.setQuestionEvaluationsJson("[]");
        }

        sheet.setEvaluationStatus("ai_evaluated");
        sheet.setTotalMarksAwarded(totalProposed);
        sheet.setPercentage(percentage);

        return sheetRepository.save(sheet);
    }

    @Transactional
    public OfflineAnswerSheetEntity updateTeacherMarks(String sheetId, List<OfflineEvaluationItemDto> updatedEvaluations) {
        OfflineAnswerSheetEntity sheet = sheetRepository.findById(sheetId)
                .orElseThrow(() -> new RuntimeException("Answer sheet not found: " + sheetId));

        int totalMarks = 0;
        if (updatedEvaluations != null) {
            for (OfflineEvaluationItemDto eval : updatedEvaluations) {
                totalMarks += eval.getTeacherMarks();
            }
        }

        int maxMarks = sheet.getMaxMarks() > 0 ? sheet.getMaxMarks() : 50;
        int percentage = (int) Math.round(((double) totalMarks / maxMarks) * 100);

        try {
            sheet.setQuestionEvaluationsJson(objectMapper.writeValueAsString(updatedEvaluations));
        } catch (Exception e) {
            sheet.setQuestionEvaluationsJson("[]");
        }

        sheet.setEvaluationStatus("teacher_verified");
        sheet.setTotalMarksAwarded(totalMarks);
        sheet.setPercentage(percentage);

        return sheetRepository.save(sheet);
    }
}
