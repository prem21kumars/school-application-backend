package com.quizapp.dto;

public class AnswerKeyItemDto {
    private int questionNumber;
    private String questionId;
    private String correctAnswer;
    private int marks;
    private String explanation;
    private String rubric;

    public AnswerKeyItemDto() {}

    public AnswerKeyItemDto(int questionNumber, String questionId, String correctAnswer, int marks, String explanation, String rubric) {
        this.questionNumber = questionNumber;
        this.questionId = questionId;
        this.correctAnswer = correctAnswer;
        this.marks = marks;
        this.explanation = explanation;
        this.rubric = rubric;
    }

    public int getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(int questionNumber) { this.questionNumber = questionNumber; }

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getRubric() { return rubric; }
    public void setRubric(String rubric) { this.rubric = rubric; }
}
