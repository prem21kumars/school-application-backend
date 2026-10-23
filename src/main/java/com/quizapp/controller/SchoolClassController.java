package com.quizapp.controller;

import com.quizapp.entity.SchoolClassEntity;
import com.quizapp.service.SchoolClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
public class SchoolClassController {

    private final SchoolClassService schoolClassService;

    @Autowired
    public SchoolClassController(SchoolClassService schoolClassService) {
        this.schoolClassService = schoolClassService;
    }

    @GetMapping
    public ResponseEntity<List<SchoolClassEntity>> getAllClasses() {
        return ResponseEntity.ok(schoolClassService.getAllClasses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SchoolClassEntity> getClassById(@PathVariable String id) {
        return schoolClassService.getClassById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SchoolClassEntity> createClass(@RequestBody SchoolClassEntity schoolClass) {
        return ResponseEntity.ok(schoolClassService.createClass(schoolClass));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SchoolClassEntity> updateClass(@PathVariable String id, @RequestBody SchoolClassEntity schoolClass) {
        return ResponseEntity.ok(schoolClassService.updateClass(id, schoolClass));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable String id) {
        schoolClassService.deleteClass(id);
        return ResponseEntity.noContent().build();
    }
}
