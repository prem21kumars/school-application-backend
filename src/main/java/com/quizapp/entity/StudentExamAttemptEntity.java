package com.quizapp.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "student_exam_attempts")
public class StudentExamAttemptEntity {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "assessment_id", nullable = false)
    private String assessmentId;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(name = "enrolment_number")
    private String enrolmentNumber;

    @Column(name = "class_id")
    private String classId;

    @Column(name = "start_time", nullable = false)
    private String startTime;

    @Column(name = "submit_time")
    private String submitTime;

    @Column(name = "status", nullable = false)
    private String status = "in_progress"; // in_progress, submitted, malpractice_terminated, evaluated

    @Column(name = "answers_json", columnDefinition = "LONGTEXT")
    private String answersJson = "[]";

    @Column(name = "total_score")
    private Integer totalScore = 0;

    @Column(name = "max_score", nullable = false)
    private Integer maxScore = 100;

    @Column(name = "percentage")
    private Integer percentage = 0;

    @Column(name = "malpractice_count", nullable = false)
    private Integer malpracticeCount = 0;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    @OrderBy("violationNumber ASC")
    private List<MalpracticeLogEntity> malpracticeLogs = new ArrayList<>();

    @Column(name = "reattempt_allowed")
    private Boolean reattemptAllowed = false;

    @Column(name = "reattempt_paper_mode")
    private String reattemptPaperMode; // same, new_random

    @Column(name = "reattempt_requested")
    private Boolean reattemptRequested = false;

    public StudentExamAttemptEntity() {}

    public void addMalpracticeLog(MalpracticeLogEntity log) {
        malpracticeLogs.add(log);
        log.setAttempt(this);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAssessmentId() { return assessmentId; }
    public void setAssessmentId(String assessmentId) { this.assessmentId = assessmentId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getEnrolmentNumber() { return enrolmentNumber; }
    public void setEnrolmentNumber(String enrolmentNumber) { this.enrolmentNumber = enrolmentNumber; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getSubmitTime() { return submitTime; }
    public void setSubmitTime(String submitTime) { this.submitTime = submitTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAnswersJson() { return answersJson; }
    public void setAnswersJson(String answersJson) { this.answersJson = answersJson; }

    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }

    public Integer getMaxScore() { return maxScore; }
    public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }

    public Integer getPercentage() { return percentage; }
    public void setPercentage(Integer percentage) { this.percentage = percentage; }

    public Integer getMalpracticeCount() { return malpracticeCount; }
    public void setMalpracticeCount(Integer malpracticeCount) { this.malpracticeCount = malpracticeCount; }

    public List<MalpracticeLogEntity> getMalpracticeLogs() { return malpracticeLogs; }
    public void setMalpracticeLogs(List<MalpracticeLogEntity> logs) {
        this.malpracticeLogs.clear();
        if (logs != null) {
            for (MalpracticeLogEntity log : logs) {
                addMalpracticeLog(log);
            }
        }
    }

    public Boolean getReattemptAllowed() { return reattemptAllowed; }
    public void setReattemptAllowed(Boolean reattemptAllowed) { this.reattemptAllowed = reattemptAllowed; }

    public String getReattemptPaperMode() { return reattemptPaperMode; }
    public void setReattemptPaperMode(String reattemptPaperMode) { this.reattemptPaperMode = reattemptPaperMode; }

    public Boolean getReattemptRequested() { return reattemptRequested; }
    public void setReattemptRequested(Boolean reattemptRequested) { this.reattemptRequested = reattemptRequested; }
}
