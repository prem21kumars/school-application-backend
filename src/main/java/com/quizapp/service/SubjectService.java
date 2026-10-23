package com.quizapp.service;

import com.quizapp.entity.ChapterEntity;
import com.quizapp.entity.SubjectEntity;
import com.quizapp.repository.ChapterRepository;
import com.quizapp.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;

    @Autowired
    public SubjectService(SubjectRepository subjectRepository, ChapterRepository chapterRepository) {
        this.subjectRepository = subjectRepository;
        this.chapterRepository = chapterRepository;
    }

    public List<SubjectEntity> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public Optional<SubjectEntity> getSubjectById(String id) {
        return subjectRepository.findById(id);
    }

    public SubjectEntity createSubject(SubjectEntity subject) {
        if (subject.getId() == null || subject.getId().trim().isEmpty()) {
            String codeSlug = subject.getCode() != null 
                    ? subject.getCode().toLowerCase().replaceAll("[^a-z0-9]", "-") 
                    : String.valueOf(System.currentTimeMillis());
            subject.setId("sub-" + codeSlug);
        }

        if (subject.getChapters() != null) {
            for (ChapterEntity ch : subject.getChapters()) {
                ch.setSubject(subject);
                if (ch.getId() == null || ch.getId().trim().isEmpty()) {
                    ch.setId("ch-" + subject.getId() + "-" + System.currentTimeMillis());
                }
            }
        }
        return subjectRepository.save(subject);
    }

    public SubjectEntity updateSubject(String id, SubjectEntity updated) {
        return subjectRepository.findById(id).map(subj -> {
            if (updated.getName() != null) subj.setName(updated.getName());
            if (updated.getCode() != null) subj.setCode(updated.getCode());
            return subjectRepository.save(subj);
        }).orElseThrow(() -> new RuntimeException("Subject not found with id: " + id));
    }

    public ChapterEntity addChapter(String subjectId, ChapterEntity chapter) {
        SubjectEntity subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + subjectId));

        if (chapter.getId() == null || chapter.getId().trim().isEmpty()) {
            chapter.setId("ch-" + subjectId + "-" + System.currentTimeMillis());
        }
        chapter.setSubject(subject);
        subject.getChapters().add(chapter);
        subjectRepository.save(subject);
        return chapter;
    }

    public void deleteSubject(String id) {
        subjectRepository.deleteById(id);
    }
}
