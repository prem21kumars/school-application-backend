package com.quizapp.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subjects")
public class SubjectEntity {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "name", nullable = false)
    private String name; // e.g. "Physics"

    @Column(name = "code", nullable = false, unique = true)
    private String code; // e.g. "PHY-101"

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    @OrderBy("number ASC")
    private List<ChapterEntity> chapters = new ArrayList<>();

    public SubjectEntity() {}

    public SubjectEntity(String id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    public void addChapter(ChapterEntity chapter) {
        chapters.add(chapter);
        chapter.setSubject(this);
    }

    public void removeChapter(ChapterEntity chapter) {
        chapters.remove(chapter);
        chapter.setSubject(null);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public List<ChapterEntity> getChapters() { return chapters; }
    public void setChapters(List<ChapterEntity> chapters) {
        this.chapters.clear();
        if (chapters != null) {
            for (ChapterEntity ch : chapters) {
                addChapter(ch);
            }
        }
    }
}
