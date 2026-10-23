package com.quizapp.dto;

import com.quizapp.entity.QuestionEntity;
import java.util.ArrayList;
import java.util.List;

public class QuestionPaperSetDto {
    private String setId;
    private String setName;
    private String assessmentId;
    private String assessmentTitle;
    private String subjectName;
    private String className;
    private int durationMinutes;
    private int totalMarks;
    private List<String> instructions = new ArrayList<>();
    private List<QuestionEntity> questions = new ArrayList<>();
    private List<AnswerKeyItemDto> answerKey = new ArrayList<>();

    public QuestionPaperSetDto() {}

    public String getSetId() { return setId; }
    public void setSetId(String setId) { this.setId = setId; }

    public String getSetName() { return setName; }
    public void setSetName(String setName) { this.setName = setName; }

    public String getAssessmentId() { return assessmentId; }
    public void setAssessmentId(String assessmentId) { this.assessmentId = assessmentId; }

    public String getAssessmentTitle() { return assessmentTitle; }
    public void setAssessmentTitle(String assessmentTitle) { this.assessmentTitle = assessmentTitle; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }

    public List<String> getInstructions() { return instructions; }
    public void setInstructions(List<String> instructions) { this.instructions = instructions; }

    public List<QuestionEntity> getQuestions() { return questions; }
    public void setQuestions(List<QuestionEntity> questions) { this.questions = questions; }

    public List<AnswerKeyItemDto> getAnswerKey() { return answerKey; }
    public void setAnswerKey(List<AnswerKeyItemDto> answerKey) { this.answerKey = answerKey; }
}
