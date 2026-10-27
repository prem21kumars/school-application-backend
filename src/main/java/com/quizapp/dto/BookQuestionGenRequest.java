package com.quizapp.dto;

import java.util.ArrayList;
import java.util.List;

public class BookQuestionGenRequest {
    private String bookId;
    private List<Integer> chapterNumbers = new ArrayList<>();
    private List<MarkDistributionItemDto> markDistribution = new ArrayList<>();
    private Integer totalQuestions = 10;
    private String groqApiKey;
    private String groqModel;
    private Boolean useGroqAi = true;

    public BookQuestionGenRequest() {}

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public List<Integer> getChapterNumbers() { return chapterNumbers; }
    public void setChapterNumbers(List<Integer> chapterNumbers) { this.chapterNumbers = chapterNumbers; }

    public List<MarkDistributionItemDto> getMarkDistribution() { return markDistribution; }
    public void setMarkDistribution(List<MarkDistributionItemDto> markDistribution) { this.markDistribution = markDistribution; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public String getGroqApiKey() { return groqApiKey; }
    public void setGroqApiKey(String groqApiKey) { this.groqApiKey = groqApiKey; }

    public String getGroqModel() { return groqModel; }
    public void setGroqModel(String groqModel) { this.groqModel = groqModel; }

    public Boolean getUseGroqAi() { return useGroqAi; }
    public void setUseGroqAi(Boolean useGroqAi) { this.useGroqAi = useGroqAi; }
}
