package com.quizapp.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "book_sources")
public class BookSourceEntity {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "subject_id", nullable = false)
    private String subjectId;

    @Column(name = "grade")
    private String grade;

    @Column(name = "author")
    private String author;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Column(name = "chapters_json", columnDefinition = "LONGTEXT")
    private String chaptersJson;

    public BookSourceEntity() {}

    public BookSourceEntity(String id, String title, String subjectId, String grade, String author, String coverImage, String chaptersJson) {
        this.id = id;
        this.title = title;
        this.subjectId = subjectId;
        this.grade = grade;
        this.author = author;
        this.coverImage = coverImage;
        this.chaptersJson = chaptersJson;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public String getChaptersJson() { return chaptersJson; }
    public void setChaptersJson(String chaptersJson) { this.chaptersJson = chaptersJson; }
}
