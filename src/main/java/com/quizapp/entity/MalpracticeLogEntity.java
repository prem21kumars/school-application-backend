package com.quizapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "malpractice_logs")
public class MalpracticeLogEntity {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "timestamp", nullable = false)
    private String timestamp;

    @Column(name = "type", nullable = false)
    private String type; // tab_switch, key_press, mouse_leave, fullscreen_exit, copy_paste, devtools

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "violation_number", nullable = false)
    private Integer violationNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id")
    @JsonBackReference
    private StudentExamAttemptEntity attempt;

    public MalpracticeLogEntity() {}

    public MalpracticeLogEntity(String id, String timestamp, String type, String description, Integer violationNumber) {
        this.id = id;
        this.timestamp = timestamp;
        this.type = type;
        this.description = description;
        this.violationNumber = violationNumber;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getViolationNumber() { return violationNumber; }
    public void setViolationNumber(Integer violationNumber) { this.violationNumber = violationNumber; }

    public StudentExamAttemptEntity getAttempt() { return attempt; }
    public void setAttempt(StudentExamAttemptEntity attempt) { this.attempt = attempt; }
}
