package com.quizapp.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "reattempt_requests")
public class ReattemptRequestEntity {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "attempt_id", nullable = false)
    private String attemptId;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(name = "enrolment_number")
    private String enrolmentNumber;

    @Column(name = "assessment_id", nullable = false)
    private String assessmentId;

    @Column(name = "assessment_title")
    private String assessmentTitle;

    @Column(name = "teacher_id")
    private String teacherId;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "requested_at", nullable = false)
    private String requestedAt;

    @Column(name = "status", nullable = false)
    private String status = "pending"; // pending, approved, rejected

    @Column(name = "resolved_at")
    private String resolvedAt;

    @Column(name = "paper_mode")
    private String paperMode; // same, new_random

    public ReattemptRequestEntity() {}

    public ReattemptRequestEntity(String id, String attemptId, String studentId, String studentName,
                                  String enrolmentNumber, String assessmentId, String assessmentTitle,
                                  String teacherId, String reason, String requestedAt, String status) {
        this.id = id;
        this.attemptId = attemptId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.enrolmentNumber = enrolmentNumber;
        this.assessmentId = assessmentId;
        this.assessmentTitle = assessmentTitle;
        this.teacherId = teacherId;
        this.reason = reason;
        this.requestedAt = requestedAt;
        this.status = status != null ? status : "pending";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAttemptId() { return attemptId; }
    public void setAttemptId(String attemptId) { this.attemptId = attemptId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getEnrolmentNumber() { return enrolmentNumber; }
    public void setEnrolmentNumber(String enrolmentNumber) { this.enrolmentNumber = enrolmentNumber; }

    public String getAssessmentId() { return assessmentId; }
    public void setAssessmentId(String assessmentId) { this.assessmentId = assessmentId; }

    public String getAssessmentTitle() { return assessmentTitle; }
    public void setAssessmentTitle(String assessmentTitle) { this.assessmentTitle = assessmentTitle; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getRequestedAt() { return requestedAt; }
    public void setRequestedAt(String requestedAt) { this.requestedAt = requestedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(String resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getPaperMode() { return paperMode; }
    public void setPaperMode(String paperMode) { this.paperMode = paperMode; }
}
