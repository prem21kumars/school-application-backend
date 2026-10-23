package com.quizapp.controller;

import com.quizapp.entity.ChapterEntity;
import com.quizapp.entity.SubjectEntity;
import com.quizapp.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    @Autowired
    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public ResponseEntity<List<SubjectEntity>> getAllSubjects() {
        return ResponseEntity.ok(subjectService.getAllSubjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectEntity> getSubjectById(@PathVariable String id) {
        return subjectService.getSubjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SubjectEntity> createSubject(@RequestBody SubjectEntity subject) {
        return ResponseEntity.ok(subjectService.createSubject(subject));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubjectEntity> updateSubject(@PathVariable String id, @RequestBody SubjectEntity subject) {
        return ResponseEntity.ok(subjectService.updateSubject(id, subject));
    }

    @PostMapping("/{subjectId}/chapters")
    public ResponseEntity<ChapterEntity> addChapter(
            @PathVariable String subjectId,
            @RequestBody ChapterEntity chapter) {
        return ResponseEntity.ok(subjectService.addChapter(subjectId, chapter));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable String id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }
}
