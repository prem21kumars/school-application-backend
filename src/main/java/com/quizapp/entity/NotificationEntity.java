package com.quizapp.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "notifications")
public class NotificationEntity {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "type", nullable = false)
    private String type = "info"; // info, warning, alert, success

    @Column(name = "timestamp", nullable = false)
    private String timestamp;

    @Column(name = "is_read", nullable = false)
    private Boolean read = false;

    @Column(name = "role_target")
    private String roleTarget; // principal, teacher, student, parent

    @Column(name = "link_action")
    private String linkAction;

    public NotificationEntity() {}

    public NotificationEntity(String id, String title, String message, String type, String timestamp, Boolean read, String roleTarget, String linkAction) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type != null ? type : "info";
        this.timestamp = timestamp;
        this.read = read != null ? read : false;
        this.roleTarget = roleTarget;
        this.linkAction = linkAction;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }

    public String getRoleTarget() { return roleTarget; }
    public void setRoleTarget(String roleTarget) { this.roleTarget = roleTarget; }

    public String getLinkAction() { return linkAction; }
    public void setLinkAction(String linkAction) { this.linkAction = linkAction; }
}
