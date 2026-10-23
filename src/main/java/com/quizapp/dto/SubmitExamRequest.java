package com.quizapp.dto;

import java.util.ArrayList;
import java.util.List;

public class SubmitExamRequest {
    private List<StudentAnswerDto> answers = new ArrayList<>();

    public SubmitExamRequest() {}

    public SubmitExamRequest(List<StudentAnswerDto> answers) {
        if (answers != null) this.answers = answers;
    }

    public List<StudentAnswerDto> getAnswers() { return answers; }
    public void setAnswers(List<StudentAnswerDto> answers) { this.answers = answers; }
}
