package com.quizapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
public class QuestionEntity {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(name = "type", nullable = false)
    private String type; // mcq, true_false, key_in, short_answer, long_answer

    @Column(name = "subject_id")
    private String subjectId;

    @Column(name = "chapter_id")
    private String chapterId;

    @Column(name = "chapter_number")
    private Integer chapterNumber;

    @Column(name = "marks", nullable = false)
    private Integer marks = 1;

    @Column(name = "difficulty")
    private String difficulty = "medium"; // easy, medium, hard, olympiad

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<QuestionOptionEntity> options = new ArrayList<>();

    @Column(name = "correct_answer_text", columnDefinition = "TEXT")
    private String correctAnswerText;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "marking_rubric", columnDefinition = "TEXT")
    private String markingRubric;

    @Column(name = "source")
    private String source = "manual"; // manual, book_upload, ai_generated

    @Column(name = "approved")
    private Boolean approved = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id")
    @JsonBackReference
    private AssessmentEntity assessment;

    public QuestionEntity() {}

    public QuestionEntity(String id, String text, String type, String subjectId, String chapterId,
                          Integer chapterNumber, Integer marks, String difficulty,
                          String correctAnswerText, String explanation, String markingRubric,
                          String source, Boolean approved) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.subjectId = subjectId;
        this.chapterId = chapterId;
        this.chapterNumber = chapterNumber;
        this.marks = marks != null ? marks : 1;
        this.difficulty = difficulty != null ? difficulty : "medium";
        this.correctAnswerText = correctAnswerText;
        this.explanation = explanation;
        this.markingRubric = markingRubric;
        this.source = source != null ? source : "manual";
        this.approved = approved != null ? approved : false;
    }

    public QuestionEntity(String id, String text, String type, String subjectId, String chapterId,
                          Integer chapterNumber, Integer marks, String difficulty,
                          String correctAnswerText, String markingRubric,
                          String source, Boolean approved) {
        this(id, text, type, subjectId, chapterId, chapterNumber, marks, difficulty, correctAnswerText, null, markingRubric, source, approved);
    }

    public void addOption(QuestionOptionEntity option) {
        options.add(option);
        option.setQuestion(this);
    }

    public void removeOption(QuestionOptionEntity option) {
        options.remove(option);
        option.setQuestion(null);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getChapterId() { return chapterId; }
    public void setChapterId(String chapterId) { this.chapterId = chapterId; }

    public Integer getChapterNumber() { return chapterNumber; }
    public void setChapterNumber(Integer chapterNumber) { this.chapterNumber = chapterNumber; }

    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public List<QuestionOptionEntity> getOptions() { return options; }
    public void setOptions(List<QuestionOptionEntity> options) {
        this.options.clear();
        if (options != null) {
            for (QuestionOptionEntity opt : options) {
                addOption(opt);
            }
        }
    }

    public String getCorrectAnswerText() { return correctAnswerText; }
    public void setCorrectAnswerText(String correctAnswerText) { this.correctAnswerText = correctAnswerText; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getMarkingRubric() { return markingRubric; }
    public void setMarkingRubric(String markingRubric) { this.markingRubric = markingRubric; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }

    public AssessmentEntity getAssessment() { return assessment; }
    public void setAssessment(AssessmentEntity assessment) { this.assessment = assessment; }
}
