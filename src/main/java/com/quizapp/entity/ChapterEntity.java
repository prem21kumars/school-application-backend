package com.quizapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subject_chapters")
public class ChapterEntity {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "chapter_number", nullable = false)
    private Integer number;

    @Column(name = "title", nullable = false)
    private String title;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "chapter_topics", joinColumns = @JoinColumn(name = "chapter_id"))
    @Column(name = "topic")
    private List<String> topics = new ArrayList<>();

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "content_excerpt", columnDefinition = "TEXT")
    private String contentExcerpt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    @JsonBackReference
    private SubjectEntity subject;

    public ChapterEntity() {}

    public ChapterEntity(String id, Integer number, String title, List<String> topics, String summary, String contentExcerpt) {
        this.id = id;
        this.number = number;
        this.title = title;
        if (topics != null) this.topics = topics;
        this.summary = summary;
        this.contentExcerpt = contentExcerpt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Integer getNumber() { return number; }
    public void setNumber(Integer number) { this.number = number; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<String> getTopics() { return topics; }
    public void setTopics(List<String> topics) { this.topics = topics; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getContentExcerpt() { return contentExcerpt; }
    public void setContentExcerpt(String contentExcerpt) { this.contentExcerpt = contentExcerpt; }

    public SubjectEntity getSubject() { return subject; }
    public void setSubject(SubjectEntity subject) { this.subject = subject; }
}
