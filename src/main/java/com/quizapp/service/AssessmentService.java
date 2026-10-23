package com.quizapp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizapp.dto.*;
import com.quizapp.entity.*;
import com.quizapp.repository.AssessmentRepository;
import com.quizapp.repository.BookSourceRepository;
import com.quizapp.repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class AssessmentService {

    private static final Logger log = LoggerFactory.getLogger(AssessmentService.class);

    private final AssessmentRepository assessmentRepository;
    private final SubjectRepository subjectRepository;
    private final BookSourceRepository bookSourceRepository;
    private final GroqAiService groqAiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public AssessmentService(AssessmentRepository assessmentRepository,
                             SubjectRepository subjectRepository,
                             BookSourceRepository bookSourceRepository,
                             GroqAiService groqAiService) {
        this.assessmentRepository = assessmentRepository;
        this.subjectRepository = subjectRepository;
        this.bookSourceRepository = bookSourceRepository;
        this.groqAiService = groqAiService;
    }


    public List<AssessmentEntity> getAllAssessments() {
        return assessmentRepository.findAll();
    }

    public Optional<AssessmentEntity> getAssessmentById(String id) {
        return assessmentRepository.findById(id);
    }

    public AssessmentEntity createAssessment(AssessmentEntity assessment) {
        if (assessment.getId() == null || assessment.getId().trim().isEmpty()) {
            assessment.setId("asm-" + System.currentTimeMillis());
        }
        if (assessment.getCreatedAt() == null) {
            assessment.setCreatedAt(Instant.now().toString());
        }
        if (assessment.getStatus() == null) {
            assessment.setStatus("draft");
        }

        // Link questions and options
        if (assessment.getQuestions() != null) {
            for (QuestionEntity q : assessment.getQuestions()) {
                q.setAssessment(assessment);
                if (q.getId() == null || q.getId().trim().isEmpty()) {
                    q.setId("q-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 5));
                }
                if (q.getOptions() != null) {
                    for (QuestionOptionEntity opt : q.getOptions()) {
                        opt.setQuestion(q);
                        if (opt.getId() == null || opt.getId().trim().isEmpty()) {
                            opt.setId("opt-" + q.getId() + "-" + UUID.randomUUID().toString().substring(0, 4));
                        }
                    }
                }
            }
        }

        return assessmentRepository.save(assessment);
    }

    public AssessmentEntity updateAssessment(String id, AssessmentEntity updated) {
        return assessmentRepository.findById(id).map(asm -> {
            if (updated.getTitle() != null) asm.setTitle(updated.getTitle());
            if (updated.getType() != null) asm.setType(updated.getType());
            if (updated.getSubjectId() != null) asm.setSubjectId(updated.getSubjectId());
            if (updated.getClassIds() != null) asm.setClassIds(updated.getClassIds());
            if (updated.getMode() != null) asm.setMode(updated.getMode());
            if (updated.getDurationMinutes() != null) asm.setDurationMinutes(updated.getDurationMinutes());
            if (updated.getTotalMarks() != null) asm.setTotalMarks(updated.getTotalMarks());
            if (updated.getPassingMarks() != null) asm.setPassingMarks(updated.getPassingMarks());
            if (updated.getStartDate() != null) asm.setStartDate(updated.getStartDate());
            if (updated.getEndDate() != null) asm.setEndDate(updated.getEndDate());
            if (updated.getTargetStudentIds() != null) asm.setTargetStudentIds(updated.getTargetStudentIds());
            if (updated.getRandomizeOrder() != null) asm.setRandomizeOrder(updated.getRandomizeOrder());
            if (updated.getShowAnswersAfterSubmission() != null) asm.setShowAnswersAfterSubmission(updated.getShowAnswersAfterSubmission());
            if (updated.getInstructions() != null) asm.setInstructions(updated.getInstructions());
            if (updated.getSetsCount() != null) asm.setSetsCount(updated.getSetsCount());
            if (updated.getStatus() != null) asm.setStatus(updated.getStatus());
            if (updated.getQuestions() != null && !updated.getQuestions().isEmpty()) {
                asm.setQuestions(updated.getQuestions());
            }
            return assessmentRepository.save(asm);
        }).orElseThrow(() -> new RuntimeException("Assessment not found with id: " + id));
    }

    public void deleteAssessment(String id) {
        assessmentRepository.deleteById(id);
    }

    public List<QuestionPaperSetDto> generateRandomizedSets(String assessmentId, int count) {
        AssessmentEntity assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found with id: " + assessmentId));

        SubjectEntity subject = subjectRepository.findById(assessment.getSubjectId()).orElse(null);
        String subjectName = subject != null ? subject.getName() : "General Subject";

        List<QuestionPaperSetDto> sets = new ArrayList<>();
        String[] setNames = {"Set A", "Set B", "Set C", "Set D", "Set E"};

        for (int i = 0; i < count; i++) {
            String setName = setNames[i % setNames.length];
            List<QuestionEntity> questionsCopy = new ArrayList<>(assessment.getQuestions());

            if (Boolean.TRUE.equals(assessment.getRandomizeOrder())) {
                // Group by marks
                Map<Integer, List<QuestionEntity>> grouped = new TreeMap<>();
                for (QuestionEntity q : questionsCopy) {
                    grouped.computeIfAbsent(q.getMarks(), k -> new ArrayList<>()).add(q);
                }

                List<QuestionEntity> shuffledGroups = new ArrayList<>();
                for (List<QuestionEntity> group : grouped.values()) {
                    List<QuestionEntity> rotated = new ArrayList<>(group);
                    if (!rotated.isEmpty()) {
                        int shift = i % rotated.size();
                        Collections.rotate(rotated, -shift);
                    }
                    shuffledGroups.addAll(rotated);
                }
                questionsCopy = shuffledGroups;
            }

            List<AnswerKeyItemDto> answerKeys = new ArrayList<>();
            for (int idx = 0; idx < questionsCopy.size(); idx++) {
                QuestionEntity q = questionsCopy.get(idx);
                String correct = q.getCorrectAnswerText() != null ? q.getCorrectAnswerText() : "";
                if ("mcq".equalsIgnoreCase(q.getType()) || "true_false".equalsIgnoreCase(q.getType())) {
                    if (q.getOptions() != null) {
                        for (QuestionOptionEntity opt : q.getOptions()) {
                            if (Boolean.TRUE.equals(opt.getIsCorrect())) {
                                correct = opt.getText();
                                break;
                            }
                        }
                    }
                }

                answerKeys.add(new AnswerKeyItemDto(
                        idx + 1,
                        q.getId(),
                        correct,
                        q.getMarks(),
                        q.getExplanation(),
                        q.getMarkingRubric()
                ));
            }

            QuestionPaperSetDto setDto = new QuestionPaperSetDto();
            setDto.setSetId("set-" + assessment.getId() + "-" + (i + 1));
            setDto.setSetName(setName);
            setDto.setAssessmentId(assessment.getId());
            setDto.setAssessmentTitle(assessment.getTitle());
            setDto.setSubjectName(subjectName);
            setDto.setClassName("Grade 10 (Secondary)");
            setDto.setDurationMinutes(assessment.getDurationMinutes());
            setDto.setTotalMarks(assessment.getTotalMarks());
            setDto.setInstructions(assessment.getInstructions());
            setDto.setQuestions(questionsCopy);
            setDto.setAnswerKey(answerKeys);

            sets.add(setDto);
        }

        return sets;
    }

    public List<QuestionEntity> generateAIQuestions(AIQuestionGenRequest request) {
        SubjectEntity subject = subjectRepository.findById(request.getSubjectId()).orElse(null);
        ChapterEntity chapter = null;
        if (subject != null && subject.getChapters() != null) {
            for (ChapterEntity ch : subject.getChapters()) {
                if (ch.getId().equals(request.getChapterId())) {
                    chapter = ch;
                    break;
                }
            }
        }

        List<String> topics = (chapter != null && chapter.getTopics() != null && !chapter.getTopics().isEmpty())
                ? chapter.getTopics()
                : List.of("Core concepts", "Application", "Formulas");

        // 1. Try Groq AI external LLM first if enabled and configured
        if (groqAiService.isAvailable()) {
            try {
                List<QuestionEntity> groqQuestions = groqAiService.generateQuestions(subject, chapter, request);
                if (groqQuestions != null && !groqQuestions.isEmpty()) {
                    log.info("Successfully generated {} curriculum questions via Groq LLM ({})",
                            groqQuestions.size(), groqAiService.getModel());
                    return groqQuestions;
                }
            } catch (Exception e) {
                log.warn("Groq AI question generation encountered error, falling back to offline template engine: {}", e.getMessage());
            }
        }

        // 2. Fallback to offline rule-based curriculum question generator
        List<String> types = (request.getQuestionTypes() != null && !request.getQuestionTypes().isEmpty())
                ? request.getQuestionTypes()
                : List.of("mcq");

        List<QuestionEntity> generated = new ArrayList<>();
        long timestamp = System.currentTimeMillis();


        for (int i = 0; i < request.getCount(); i++) {
            String qType = types.get(i % types.size());
            String topic = topics.get(i % topics.size());
            String qId = "ai-gen-" + timestamp + "-" + i;
            String chapterTitle = chapter != null ? chapter.getTitle() : "Curriculum Module";
            int chapterNum = chapter != null && chapter.getNumber() != null ? chapter.getNumber() : 1;

            QuestionEntity q = new QuestionEntity();
            q.setId(qId);
            q.setType(qType);
            q.setSubjectId(request.getSubjectId());
            q.setChapterId(request.getChapterId());
            q.setChapterNumber(chapterNum);
            q.setMarks(request.getMarks() != null ? request.getMarks() : 2);
            q.setDifficulty(request.getDifficulty() != null ? request.getDifficulty() : "medium");
            q.setSource("ai_generated");
            q.setApproved(false);

            if ("mcq".equalsIgnoreCase(qType)) {
                q.setText("[AI Generated] Regarding " + topic + " in " + chapterTitle + ", which of the following statements represents the correct physical/mathematical principle?");
                q.setExplanation("Theoretical basis: " + topic + " operates under strict conservation rules described in standard textbook curriculum.");
                
                QuestionOptionEntity opt1 = new QuestionOptionEntity("opt-" + qId + "-1", "The value remains directly invariant under standard conditions.", true);
                QuestionOptionEntity opt2 = new QuestionOptionEntity("opt-" + qId + "-2", "The magnitude increases quadratically with inverse parameters.", false);
                QuestionOptionEntity opt3 = new QuestionOptionEntity("opt-" + qId + "-3", "Energy is dissipated uniformly without thermodynamic loss.", false);
                QuestionOptionEntity opt4 = new QuestionOptionEntity("opt-" + qId + "-4", "The derivative with respect to time vanishes entirely.", false);
                q.setOptions(List.of(opt1, opt2, opt3, opt4));
            } else if ("true_false".equalsIgnoreCase(qType)) {
                q.setText("[AI Generated] True or False: In " + topic + ", the resultant vector/scalar value remains strictly positive under all boundary conditions.");
                q.setExplanation("Consistent with fundamental axioms of " + chapterTitle + ".");
                
                QuestionOptionEntity opt1 = new QuestionOptionEntity("opt-" + qId + "-1", "True", true);
                QuestionOptionEntity opt2 = new QuestionOptionEntity("opt-" + qId + "-2", "False", false);
                q.setOptions(List.of(opt1, opt2));
            } else if ("key_in".equalsIgnoreCase(qType)) {
                q.setText("[AI Generated] Calculate the numerical quantity for " + topic + " when initial input is 24 units and rate multiplier is 2.5:");
                q.setCorrectAnswerText("60");
                q.setExplanation("Direct substitution formula: 24 * 2.5 = 60 units.");
                q.setMarkingRubric("Full marks for exact answer 60 or step derivation shown.");
            } else {
                q.setText("[AI Generated] Discuss in detail the significance of " + topic + " in relation to " + chapterTitle + ". Provide a mathematical formulation and real-world example.");
                q.setType(q.getMarks() >= 5 ? "long_answer" : "short_answer");
                q.setCorrectAnswerText("Comprehensive definition of " + topic + ", followed by governing equations and applicable industrial/natural phenomenon example.");
                q.setMarkingRubric("Definition (2 marks), Equation & Diagram (2 marks), Practical Application (1 mark).");
            }

            generated.add(q);
        }

        return generated;
    }

    public List<QuestionEntity> generateBookQuestions(BookQuestionGenRequest request) {
        BookSourceEntity book = bookSourceRepository.findById(request.getBookId()).orElse(null);
        List<QuestionEntity> generated = new ArrayList<>();
        long timestamp = System.currentTimeMillis();
        int counter = 1;

        for (MarkDistributionItemDto dist : request.getMarkDistribution()) {
            for (int i = 0; i < dist.getCount(); i++) {
                int chapterNum = (!request.getChapterNumbers().isEmpty()) 
                        ? request.getChapterNumbers().get(i % request.getChapterNumbers().size()) 
                        : 1;

                String qId = "bk-gen-" + timestamp + "-" + (counter++);
                String bookTitle = book != null ? book.getTitle() : "Prescribed Syllabus Book";
                String subjectId = book != null ? book.getSubjectId() : "sub-phy";

                QuestionEntity q = new QuestionEntity();
                q.setId(qId);
                q.setSubjectId(subjectId);
                q.setChapterId("ch-" + subjectId + "-" + chapterNum);
                q.setChapterNumber(chapterNum);
                q.setMarks(dist.getMarks());
                q.setDifficulty(dist.getMarks() >= 5 ? "hard" : (dist.getMarks() >= 3 ? "medium" : "easy"));
                q.setSource("book_upload");
                q.setApproved(false);

                if (dist.getMarks() == 1) {
                    q.setType("mcq");
                    q.setText("[Book Source: " + bookTitle + "] Identify the fundamental term defined in Chapter " + chapterNum + " regarding core concepts.");
                    q.setExplanation("Extracted directly from Chapter " + chapterNum + ".");
                    QuestionOptionEntity opt1 = new QuestionOptionEntity("opt-" + qId + "-1", "Principle definition as stated in textbook", true);
                    QuestionOptionEntity opt2 = new QuestionOptionEntity("opt-" + qId + "-2", "Alternative secondary corollary", false);
                    QuestionOptionEntity opt3 = new QuestionOptionEntity("opt-" + qId + "-3", "Empirical estimation rule", false);
                    QuestionOptionEntity opt4 = new QuestionOptionEntity("opt-" + qId + "-4", "None of the above", false);
                    q.setOptions(List.of(opt1, opt2, opt3, opt4));
                } else if (dist.getMarks() == 2) {
                    q.setType("short_answer");
                    q.setText("[Book Source: Ch " + chapterNum + "] Define the core rule governing Chapter " + chapterNum + " in " + bookTitle + ". Write its standard units.");
                    q.setCorrectAnswerText("Standard textbook definition and appropriate SI units.");
                    q.setMarkingRubric("1 mark for definition, 1 mark for correct unit.");
                } else {
                    q.setType("long_answer");
                    q.setText("[Book Source: Ch " + chapterNum + "] Elaborate on the core concepts in Chapter " + chapterNum + " with complete derivation, formulas, and diagrams.");
                    q.setCorrectAnswerText("Detailed explanation including mathematical formulation and graphical breakdown.");
                    q.setMarkingRubric("2 marks for theoretical explanation, 2 marks for derivation, 1 mark for diagram.");
                }

                generated.add(q);
            }
        }

        return generated;
    }
}
