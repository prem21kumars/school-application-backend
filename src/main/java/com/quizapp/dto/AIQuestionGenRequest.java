package com.quizapp.dto;

import java.util.ArrayList;
import java.util.List;

public class AIQuestionGenRequest {
    private String subjectId;
    private String chapterId;
    private List<String> questionTypes = new ArrayList<>();
    private Integer marks = 2;
    private String difficulty = "medium";
    private Integer count = 5;
    private String customPrompt;
    private String groqApiKey;
    private String groqModel;

    public AIQuestionGenRequest() {}

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getChapterId() { return chapterId; }
    public void setChapterId(String chapterId) { this.chapterId = chapterId; }

    public List<String> getQuestionTypes() { return questionTypes; }
    public void setQuestionTypes(List<String> questionTypes) { this.questionTypes = questionTypes; }

    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }

    public String getCustomPrompt() { return customPrompt; }
    public void setCustomPrompt(String customPrompt) { this.customPrompt = customPrompt; }

    public String getGroqApiKey() { return groqApiKey; }
    public void setGroqApiKey(String groqApiKey) { this.groqApiKey = groqApiKey; }

    public String getGroqModel() { return groqModel; }
    public void setGroqModel(String groqModel) { this.groqModel = groqModel; }
}
