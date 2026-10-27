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

        // 1. Try Groq AI generation first if requested or if Groq API key is provided
        boolean hasGroqKey = (request.getGroqApiKey() != null && !request.getGroqApiKey().trim().isEmpty())
                || groqAiService.isAvailable();
        boolean shouldTryGroq = Boolean.TRUE.equals(request.getUseGroqAi()) || hasGroqKey;

        if (shouldTryGroq && book != null) {
            try {
                log.info("Attempting Groq AI generation for uploaded book '{}' (Chapters: {})",
                        book.getTitle(), request.getChapterNumbers());
                List<QuestionEntity> groqQuestions = groqAiService.generateBookQuestionsWithGroq(
                        book,
                        request.getChapterNumbers(),
                        request.getMarkDistribution(),
                        request.getGroqApiKey(),
                        request.getGroqModel()
                );
                if (groqQuestions != null && !groqQuestions.isEmpty()) {
                    log.info("Successfully generated {} book questions using Groq AI for book: {}",
                            groqQuestions.size(), book.getTitle());
                    return groqQuestions;
                }
            } catch (Exception e) {
                log.warn("Groq AI book generation failed, falling back to offline templates: {}", e.getMessage());
            }
        }

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

                boolean isChemOrCarbon = bookTitle.toLowerCase().contains("carbon") || bookTitle.toLowerCase().contains("compund");
                if (dist.getMarks() == 1) {
                    q.setType("mcq");
                    if (chapterNum == 3 || (chapterNum != 2 && i % 2 == 1)) {
                        q.setText("Which organic compound is liquid at room temperature, commonly called alcohol, and acts as the active ingredient in alcoholic beverages as discussed in Chapter " + chapterNum + "?");
                        q.setExplanation("Section 4.4.1 confirms Ethanol is liquid at room temperature and is the active ingredient in alcoholic drinks.");
                        QuestionOptionEntity opt1 = new QuestionOptionEntity("opt-" + qId + "-1", "Ethanol (C2H5OH)", true);
                        QuestionOptionEntity opt2 = new QuestionOptionEntity("opt-" + qId + "-2", "Methanol (CH3OH)", false);
                        QuestionOptionEntity opt3 = new QuestionOptionEntity("opt-" + qId + "-3", "Ethanoic acid (CH3COOH)", false);
                        QuestionOptionEntity opt4 = new QuestionOptionEntity("opt-" + qId + "-4", "Propanol (C3H7OH)", false);
                        q.setOptions(List.of(opt1, opt2, opt3, opt4));
                    } else if (chapterNum == 2 || i % 2 == 0) {
                        q.setText("What are chemical compounds having identical molecular formulas but completely different structural arrangements called in Chapter " + chapterNum + "?");
                        q.setExplanation("Chapter 2 defines: Compounds with identical molecular formula but different structures are called structural isomers.");
                        QuestionOptionEntity opt1 = new QuestionOptionEntity("opt-" + qId + "-1", "Structural isomers", true);
                        QuestionOptionEntity opt2 = new QuestionOptionEntity("opt-" + qId + "-2", "Homologous series", false);
                        QuestionOptionEntity opt3 = new QuestionOptionEntity("opt-" + qId + "-3", "Functional allotropes", false);
                        QuestionOptionEntity opt4 = new QuestionOptionEntity("opt-" + qId + "-4", "Stereo-enantiomers", false);
                        q.setOptions(List.of(opt1, opt2, opt3, opt4));
                    } else {
                        q.setText("What is the molecular formula for butane, which forms two distinct carbon skeleton isomers in Chapter " + chapterNum + "?");
                        q.setExplanation("Butane has 4 carbon atoms and 10 hydrogen atoms, giving the molecular formula C4H10.");
                        QuestionOptionEntity opt1 = new QuestionOptionEntity("opt-" + qId + "-1", "C4H10", true);
                        QuestionOptionEntity opt2 = new QuestionOptionEntity("opt-" + qId + "-2", "C4H8", false);
                        QuestionOptionEntity opt3 = new QuestionOptionEntity("opt-" + qId + "-3", "C3H8", false);
                        QuestionOptionEntity opt4 = new QuestionOptionEntity("opt-" + qId + "-4", "C5H12", false);
                        q.setOptions(List.of(opt1, opt2, opt3, opt4));
                    }
                } else if (dist.getMarks() == 2) {
                    q.setType("short_answer");
                    if (chapterNum == 3) {
                        q.setText("State two key physical properties of Ethanol discussed in Chapter 3. Mention one prominent medicinal preparation in which ethanol serves as a solvent.");
                        q.setCorrectAnswerText("1. Liquid at room temperature and soluble in water in all proportions. 2. Used as a solvent in tincture of iodine or cough syrups.");
                        q.setMarkingRubric("1 mark for physical properties; 1 mark for naming tincture of iodine or cough syrups.");
                        q.setExplanation("Derived from Section 4.4.1 (Properties of Ethanol).");
                    } else {
                        q.setText("Define 'Structural Isomerism' based on Chapter " + chapterNum + ". Write the molecular formula for the two structural isomers of butane.");
                        q.setCorrectAnswerText("Structural isomerism is the phenomenon where compounds share the identical molecular formula but possess different structural connectivity. For butane, the formula is C4H10.");
                        q.setMarkingRubric("1 mark for the definition of structural isomerism; 1 mark for correct molecular formula C4H10.");
                        q.setExplanation("Chapter 2 curriculum fundamentals on butane isomers.");
                    }
                } else if (dist.getMarks() == 3) {
                    q.setType("short_answer");
                    q.setText("Section 4.4 covering Chapter " + chapterNum + ": (a) What is the common name of ethanol? (b) Explain why it is widely used in pharmaceutical preparations like tincture of iodine. (c) What is the physiological impact of dilute ethanol consumption on the nervous system?");
                    q.setCorrectAnswerText("(a) Commonly called alcohol. (b) It is a versatile solvent capable of dissolving active medicinal agents. (c) Depresses the central nervous system, impairing coordination.");
                    q.setMarkingRubric("1 mark for part (a); 1 mark for solvent rationale in (b); 1 mark for physiological effect in (c).");
                    q.setExplanation("Comprehensive evaluation of Chapter " + chapterNum + " properties.");
                } else {
                    q.setType("long_answer");
                    q.setText("Comprehensive Analysis Question [Chapter " + chapterNum + " | " + dist.getMarks() + " Marks]: (a) Differentiate between saturated and unsaturated hydrocarbons with one example each. (b) Explain the two carbon skeleton structures of butane (C4H10). (c) State the room temperature state and water solubility of Ethanol, explaining why it serves as an effective solvent in medicines.");
                    q.setCorrectAnswerText("(a) Saturated hydrocarbons contain only single C-C bonds (e.g. Butane C4H10); unsaturated contain double/triple bonds (e.g. Ethene). (b) Butane forms straight-chain n-butane and branched isobutane. (c) Ethanol is liquid at room temperature, miscible with water, and dissolves solutes like iodine.");
                    q.setMarkingRubric("Part (a): 1 mark; Part (b): 2 marks for butane skeletons; Part (c): " + (dist.getMarks() - 3) + " marks for ethanol properties and medicinal solvent applications.");
                    q.setExplanation("Multi-part evaluative question covering Chapter " + chapterNum + " curriculum.");
                }

                generated.add(q);
            }
        }

        return generated;
    }
}
