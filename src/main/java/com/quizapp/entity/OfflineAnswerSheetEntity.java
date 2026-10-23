package com.quizapp.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "offline_answer_sheets")
public class OfflineAnswerSheetEntity {

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

    @Column(name = "scanned_image_url", columnDefinition = "TEXT")
    private String scannedImageUrl;

    @Column(name = "upload_date", nullable = false)
    private String uploadDate;

    @Column(name = "evaluation_status", nullable = false)
    private String evaluationStatus = "pending"; // pending, ai_evaluated, teacher_verified

    @Column(name = "question_evaluations_json", columnDefinition = "LONGTEXT")
    private String questionEvaluationsJson = "[]";

    @Column(name = "total_marks_awarded")
    private Integer totalMarksAwarded = 0;

    @Column(name = "max_marks", nullable = false)
    private Integer maxMarks = 50;

    @Column(name = "percentage")
    private Integer percentage = 0;

    public OfflineAnswerSheetEntity() {}

    public OfflineAnswerSheetEntity(String id, String assessmentId, String studentId, String studentName,
                                   String enrolmentNumber, String classId, String scannedImageUrl,
                                   String uploadDate, String evaluationStatus, String questionEvaluationsJson,
                                   Integer totalMarksAwarded, Integer maxMarks, Integer percentage) {
        this.id = id;
        this.assessmentId = assessmentId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.enrolmentNumber = enrolmentNumber;
        this.classId = classId;
        this.scannedImageUrl = scannedImageUrl;
        this.uploadDate = uploadDate;
        this.evaluationStatus = evaluationStatus != null ? evaluationStatus : "pending";
        this.questionEvaluationsJson = questionEvaluationsJson != null ? questionEvaluationsJson : "[]";
        this.totalMarksAwarded = totalMarksAwarded != null ? totalMarksAwarded : 0;
        this.maxMarks = maxMarks != null ? maxMarks : 50;
        this.percentage = percentage != null ? percentage : 0;
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

    public String getScannedImageUrl() { return scannedImageUrl; }
    public void setScannedImageUrl(String scannedImageUrl) { this.scannedImageUrl = scannedImageUrl; }

    public String getUploadDate() { return uploadDate; }
    public void setUploadDate(String uploadDate) { this.uploadDate = uploadDate; }

    public String getEvaluationStatus() { return evaluationStatus; }
    public void setEvaluationStatus(String evaluationStatus) { this.evaluationStatus = evaluationStatus; }

    public String getQuestionEvaluationsJson() { return questionEvaluationsJson; }
    public void setQuestionEvaluationsJson(String questionEvaluationsJson) { this.questionEvaluationsJson = questionEvaluationsJson; }

    public Integer getTotalMarksAwarded() { return totalMarksAwarded; }
    public void setTotalMarksAwarded(Integer totalMarksAwarded) { this.totalMarksAwarded = totalMarksAwarded; }

    public Integer getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Integer maxMarks) { this.maxMarks = maxMarks; }

    public Integer getPercentage() { return percentage; }
    public void setPercentage(Integer percentage) { this.percentage = percentage; }
}
