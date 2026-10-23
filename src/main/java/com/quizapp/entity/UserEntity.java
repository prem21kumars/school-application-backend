package com.quizapp.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "role", nullable = false)
    private String role; // principal, teacher, student, parent

    @Column(name = "avatar", length = 500)
    private String avatar;

    @Column(name = "enrolment_number")
    private String enrolmentNumber;

    @Column(name = "class_id")
    private String classId;

    @Column(name = "section")
    private String section;

    @Column(name = "status", nullable = false)
    private String status = "active"; // active, inactive

    @Column(name = "password")
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_assigned_classes", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "class_id")
    private List<String> assignedClassIds = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_assigned_subjects", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "subject_id")
    private List<String> assignedSubjectIds = new ArrayList<>();

    public UserEntity() {}

    public UserEntity(String id, String name, String email, String role, String avatar,
                      String enrolmentNumber, String classId, String section, String status,
                      String password, List<String> assignedClassIds, List<String> assignedSubjectIds) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.avatar = avatar;
        this.enrolmentNumber = enrolmentNumber;
        this.classId = classId;
        this.section = section;
        this.status = status != null ? status : "active";
        this.password = password;
        if (assignedClassIds != null) this.assignedClassIds = assignedClassIds;
        if (assignedSubjectIds != null) this.assignedSubjectIds = assignedSubjectIds;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getEnrolmentNumber() { return enrolmentNumber; }
    public void setEnrolmentNumber(String enrolmentNumber) { this.enrolmentNumber = enrolmentNumber; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<String> getAssignedClassIds() { return assignedClassIds; }
    public void setAssignedClassIds(List<String> assignedClassIds) { this.assignedClassIds = assignedClassIds; }

    public List<String> getAssignedSubjectIds() { return assignedSubjectIds; }
    public void setAssignedSubjectIds(List<String> assignedSubjectIds) { this.assignedSubjectIds = assignedSubjectIds; }
}
