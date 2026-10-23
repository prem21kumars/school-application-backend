package com.quizapp.dto;

import java.util.ArrayList;
import java.util.List;

public class TeacherOfflineMarksRequest {
    private List<OfflineEvaluationItemDto> questionEvaluations = new ArrayList<>();

    public TeacherOfflineMarksRequest() {}

    public TeacherOfflineMarksRequest(List<OfflineEvaluationItemDto> questionEvaluations) {
        if (questionEvaluations != null) this.questionEvaluations = questionEvaluations;
    }

    public List<OfflineEvaluationItemDto> getQuestionEvaluations() { return questionEvaluations; }
    public void setQuestionEvaluations(List<OfflineEvaluationItemDto> questionEvaluations) { this.questionEvaluations = questionEvaluations; }
}
