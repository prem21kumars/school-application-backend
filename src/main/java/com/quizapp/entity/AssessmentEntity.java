package com.quizapp.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assessments")
public class AssessmentEntity {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "type", nullable = false)
    private String type; // quiz, exam, unit_test, term_exam

    @Column(name = "subject_id", nullable = false)
    private String subjectId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "assessment_classes", joinColumns = @JoinColumn(name = "assessment_id"))
    @Column(name = "class_id")
    private List<String> classIds = new ArrayList<>();

    @Column(name = "mode", nullable = false)
    private String mode; // online, offline

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes = 30;

    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks = 25;

    @Column(name = "passing_marks", nullable = false)
    private Integer passingMarks = 10;

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "end_date")
    private String endDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "assessment_target_students", joinColumns = @JoinColumn(name = "assessment_id"))
    @Column(name = "student_id")
    private List<String> targetStudentIds = new ArrayList<>();

    @Column(name = "randomize_order")
    private Boolean randomizeOrder = true;

    @Column(name = "show_answers_after_submission")
    private Boolean showAnswersAfterSubmission = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "assessment_instructions", joinColumns = @JoinColumn(name = "assessment_id"))
    @Column(name = "instruction", columnDefinition = "TEXT")
    private List<String> instructions = new ArrayList<>();

    @Column(name = "sets_count")
    private Integer setsCount = 1;

    @Column(name = "status", nullable = false)
    private String status = "draft"; // draft, published, completed, archived

    @Column(name = "created_by_teacher_id")
    private String createdByTeacherId;

    @Column(name = "created_at")
    private String createdAt;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<QuestionEntity> questions = new ArrayList<>();

    public AssessmentEntity() {}

    public void addQuestion(QuestionEntity question) {
        questions.add(question);
        question.setAssessment(this);
    }

    public void removeQuestion(QuestionEntity question) {
        questions.remove(question);
        question.setAssessment(null);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public List<String> getClassIds() { return classIds; }
    public void setClassIds(List<String> classIds) { this.classIds = classIds; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }

    public Integer getPassingMarks() { return passingMarks; }
    public void setPassingMarks(Integer passingMarks) { this.passingMarks = passingMarks; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public List<String> getTargetStudentIds() { return targetStudentIds; }
    public void setTargetStudentIds(List<String> targetStudentIds) { this.targetStudentIds = targetStudentIds; }

    public Boolean getRandomizeOrder() { return randomizeOrder; }
    public void setRandomizeOrder(Boolean randomizeOrder) { this.randomizeOrder = randomizeOrder; }

    public Boolean getShowAnswersAfterSubmission() { return showAnswersAfterSubmission; }
    public void setShowAnswersAfterSubmission(Boolean showAnswersAfterSubmission) { this.showAnswersAfterSubmission = showAnswersAfterSubmission; }

    public List<String> getInstructions() { return instructions; }
    public void setInstructions(List<String> instructions) { this.instructions = instructions; }

    public Integer getSetsCount() { return setsCount; }
    public void setSetsCount(Integer setsCount) { this.setsCount = setsCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedByTeacherId() { return createdByTeacherId; }
    public void setCreatedByTeacherId(String createdByTeacherId) { this.createdByTeacherId = createdByTeacherId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public List<QuestionEntity> getQuestions() { return questions; }
    public void setQuestions(List<QuestionEntity> questions) {
        this.questions.clear();
        if (questions != null) {
            for (QuestionEntity q : questions) {
                addQuestion(q);
            }
        }
    }
}
