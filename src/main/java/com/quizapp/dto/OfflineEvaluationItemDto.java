package com.quizapp.dto;

public class OfflineEvaluationItemDto {
    private String questionId;
    private int questionNumber;
    private int maxMarks;
    private String extractedText;
    private int aiProposedMarks;
    private int aiConfidence;
    private String aiReasoning;
    private int teacherMarks;
    private String teacherFeedback;
    private boolean isFlaggedForReview;

    public OfflineEvaluationItemDto() {}

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public int getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(int questionNumber) { this.questionNumber = questionNumber; }

    public int getMaxMarks() { return maxMarks; }
    public void setMaxMarks(int maxMarks) { this.maxMarks = maxMarks; }

    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }

    public int getAiProposedMarks() { return aiProposedMarks; }
    public void setAiProposedMarks(int aiProposedMarks) { this.aiProposedMarks = aiProposedMarks; }

    public int getAiConfidence() { return aiConfidence; }
    public void setAiConfidence(int aiConfidence) { this.aiConfidence = aiConfidence; }

    public String getAiReasoning() { return aiReasoning; }
    public void setAiReasoning(String aiReasoning) { this.aiReasoning = aiReasoning; }

    public int getTeacherMarks() { return teacherMarks; }
    public void setTeacherMarks(int teacherMarks) { this.teacherMarks = teacherMarks; }

    public String getTeacherFeedback() { return teacherFeedback; }
    public void setTeacherFeedback(String teacherFeedback) { this.teacherFeedback = teacherFeedback; }

    public boolean isFlaggedForReview() { return isFlaggedForReview; }
    public void setFlaggedForReview(boolean flaggedForReview) { isFlaggedForReview = flaggedForReview; }
}
