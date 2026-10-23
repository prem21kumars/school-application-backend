package com.quizapp.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "school_classes")
public class SchoolClassEntity {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "name", nullable = false)
    private String name; // e.g. "Grade 10"

    @Column(name = "section", nullable = false)
    private String section; // e.g. "A"

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Column(name = "student_count")
    private Integer studentCount = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "class_subjects", joinColumns = @JoinColumn(name = "class_id"))
    @Column(name = "subject_id")
    private List<String> subjectIds = new ArrayList<>();

    public SchoolClassEntity() {}

    public SchoolClassEntity(String id, String name, String section, String academicYear, Integer studentCount, List<String> subjectIds) {
        this.id = id;
        this.name = name;
        this.section = section;
        this.academicYear = academicYear;
        this.studentCount = studentCount != null ? studentCount : 0;
        if (subjectIds != null) this.subjectIds = subjectIds;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Integer getStudentCount() { return studentCount; }
    public void setStudentCount(Integer studentCount) { this.studentCount = studentCount; }

    public List<String> getSubjectIds() { return subjectIds; }
    public void setSubjectIds(List<String> subjectIds) { this.subjectIds = subjectIds; }
}
