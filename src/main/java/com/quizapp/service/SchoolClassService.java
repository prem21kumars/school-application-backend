package com.quizapp.service;

import com.quizapp.entity.SchoolClassEntity;
import com.quizapp.repository.SchoolClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SchoolClassService {

    private final SchoolClassRepository schoolClassRepository;

    @Autowired
    public SchoolClassService(SchoolClassRepository schoolClassRepository) {
        this.schoolClassRepository = schoolClassRepository;
    }

    public List<SchoolClassEntity> getAllClasses() {
        return schoolClassRepository.findAll();
    }

    public Optional<SchoolClassEntity> getClassById(String id) {
        return schoolClassRepository.findById(id);
    }

    public SchoolClassEntity createClass(SchoolClassEntity schoolClass) {
        if (schoolClass.getId() == null || schoolClass.getId().trim().isEmpty()) {
            schoolClass.setId("cls-" + System.currentTimeMillis());
        }
        return schoolClassRepository.save(schoolClass);
    }

    public SchoolClassEntity updateClass(String id, SchoolClassEntity updated) {
        return schoolClassRepository.findById(id).map(cls -> {
            if (updated.getName() != null) cls.setName(updated.getName());
            if (updated.getSection() != null) cls.setSection(updated.getSection());
            if (updated.getAcademicYear() != null) cls.setAcademicYear(updated.getAcademicYear());
            if (updated.getStudentCount() != null) cls.setStudentCount(updated.getStudentCount());
            if (updated.getSubjectIds() != null) cls.setSubjectIds(updated.getSubjectIds());
            return schoolClassRepository.save(cls);
        }).orElseThrow(() -> new RuntimeException("Class not found with id: " + id));
    }

    public void deleteClass(String id) {
        schoolClassRepository.deleteById(id);
    }
}
