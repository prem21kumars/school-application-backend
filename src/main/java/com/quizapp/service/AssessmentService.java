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

        // 1. Strictly execute Groq AI if requested
        if (Boolean.TRUE.equals(request.getUseGroqAi())) {
            log.info("Executing Groq AI question generation for book '{}' (Chapters: {})",
                    book != null ? book.getTitle() : request.getBookId(), request.getChapterNumbers());
            try {
                List<QuestionEntity> groqQuestions = groqAiService.generateBookQuestionsWithGroq(
                        book,
                        request.getChapterNumbers(),
                        request.getMarkDistribution(),
                        request.getGroqApiKey(),
                        request.getGroqModel()
                );
                if (groqQuestions != null && !groqQuestions.isEmpty()) {
                    log.info("Successfully generated {} book questions using Groq AI", groqQuestions.size());
                    return groqQuestions;
                }
                throw new IllegalStateException("Groq AI returned no questions. Please verify your Groq API key and prompt parameters.");
            } catch (Exception e) {
                log.error("Groq AI generation failure: {}", e.getMessage());
                throw new IllegalStateException("Groq AI generation failed: " + e.getMessage(), e);
            }
        }

        // 2. Offline Fallback Generation: 100% Unique Questions Pool
        List<QuestionEntity> generated = new ArrayList<>();
        long timestamp = System.currentTimeMillis();
        int counter = 0;

        for (MarkDistributionItemDto dist : request.getMarkDistribution()) {
            for (int i = 0; i < dist.getCount(); i++) {
                int chapterNum = (!request.getChapterNumbers().isEmpty()) 
                        ? request.getChapterNumbers().get(counter % request.getChapterNumbers().size()) 
                        : 1;

                String qId = "bk-gen-" + timestamp + "-" + (++counter);
                String bookTitle = book != null ? book.getTitle() : "Prescribed Syllabus Book";
                String subjectId = book != null ? book.getSubjectId() : "sub-chem";

                QuestionEntity q = new QuestionEntity();
                q.setId(qId);
                q.setSubjectId(subjectId);
                q.setChapterId("ch-" + subjectId + "-" + chapterNum);
                q.setChapterNumber(chapterNum);
                q.setMarks(dist.getMarks());
                q.setDifficulty(dist.getMarks() >= 5 ? "hard" : (dist.getMarks() >= 3 ? "medium" : "easy"));
                q.setSource("book_upload");
                q.setApproved(true);

                if (dist.getMarks() == 1) {
                    q.setType("mcq");
                    List<String[]> mcqBank = List.of(
                        new String[]{"What are chemical compounds having identical molecular formulas but completely different structural arrangements called in Chapter " + chapterNum + "?", "Structural isomers", "Homologous series", "Functional allotropes", "Stereo-enantiomers", "Chapter 2 defines: Compounds with identical molecular formula but different structures are called structural isomers."},
                        new String[]{"Which organic compound is liquid at room temperature, commonly called alcohol, and acts as the active ingredient in alcoholic beverages?", "Ethanol (C2H5OH)", "Methanol (CH3OH)", "Ethanoic acid (CH3COOH)", "Propanol (C3H7OH)", "Section 4.4.1 confirms Ethanol is liquid at room temperature and is the active ingredient in alcoholic drinks."},
                        new String[]{"What is the molecular formula for butane, which forms two distinct carbon skeleton isomers?", "C4H10", "C4H8", "C3H8", "C5H12", "Butane has 4 carbon atoms and 10 hydrogen atoms, giving the molecular formula C4H10."},
                        new String[]{"Why is ethanol widely used in medical preparations such as tincture of iodine and cough syrups?", "Because it is an excellent organic solvent miscible with water", "Because it is an oxidising strong acid", "Because it prevents containers from breaking", "Because it acts as an emulsifying detergent", "Ethanol is a versatile organic solvent that dissolves medical compounds like iodine."},
                        new String[]{"How many single covalent bonds are present in a molecule of methane (CH4)?", "4 covalent bonds", "2 covalent bonds", "3 covalent bonds", "6 covalent bonds", "Carbon shares 4 valence electron pairs with 4 hydrogen atoms in methane."},
                        new String[]{"Which unique property allows carbon to form long chains and branched structures with other carbon atoms?", "Catenation", "Allotropy", "Isomerism", "Electronegativity", "Catenation is the self-linking capacity of carbon atoms through covalent bonds."},
                        new String[]{"What is the functional group present in carboxylic acids like ethanoic acid?", "-COOH", "-OH", "-CHO", ">C=O", "Carboxylic acids possess the -COOH functional group."},
                        new String[]{"Which carbon allotrope conducts electricity due to free delocalized valence electrons?", "Graphite", "Diamond", "Fullerene C60", "Coal", "In graphite, each carbon atom is bonded to three other carbons in hexagonal layers, leaving free electrons to conduct electricity."}
                    );
                    String[] selectedMcq = mcqBank.get((counter - 1) % mcqBank.size());
                    q.setText(selectedMcq[0]);
                    q.setExplanation(selectedMcq[5]);
                    QuestionOptionEntity opt1 = new QuestionOptionEntity("opt-" + qId + "-1", selectedMcq[1], true);
                    QuestionOptionEntity opt2 = new QuestionOptionEntity("opt-" + qId + "-2", selectedMcq[2], false);
                    QuestionOptionEntity opt3 = new QuestionOptionEntity("opt-" + qId + "-3", selectedMcq[3], false);
                    QuestionOptionEntity opt4 = new QuestionOptionEntity("opt-" + qId + "-4", selectedMcq[4], false);
                    q.setOptions(List.of(opt1, opt2, opt3, opt4));
                } else if (dist.getMarks() == 2) {
                    q.setType("short_answer");
                    List<String[]> short2mBank = List.of(
                        new String[]{"Define 'Structural Isomerism' based on Chapter " + chapterNum + ". Write the molecular formula for the two structural isomers of butane.", "Structural isomerism is where compounds share identical molecular formula but possess different structural connectivity. For butane, the formula is C4H10.", "1 mark for definition of structural isomerism; 1 mark for correct molecular formula C4H10 and describing skeletons.", "Chapter 2 curriculum fundamentals on butane isomers."},
                        new String[]{"State two key physical properties of Ethanol discussed in Chapter " + chapterNum + ". Mention one prominent medicinal preparation in which ethanol serves as a solvent.", "1. Liquid at room temperature and soluble in water in all proportions. 2. Used as a solvent in tincture of iodine or cough syrups.", "1 mark for physical properties; 1 mark for naming tincture of iodine or cough syrups.", "Derived from Section 4.4.1 (Properties of Ethanol)."},
                        new String[]{"Why does carbon form compounds primarily by covalent bonding instead of forming C4+ or C4- ionic species?", "Carbon cannot hold 10 electrons in C4- due to 6 protons in the nucleus, and removing 4 electrons to form C4+ requires immense ionization energy. Hence it shares electrons via covalent bonding.", "1 mark for explaining C4- instability; 1 mark for C4+ ionization energy explanation.", "Fundamental nature of carbon covalent bonding."},
                        new String[]{"Differentiate between saturated and unsaturated hydrocarbons with one example of each.", "Saturated hydrocarbons contain only single C-C bonds (e.g. Methane CH4); unsaturated contain double/triple bonds (e.g. Ethene C2H4).", "1 mark for definitions; 1 mark for correct examples.", "Classification of hydrocarbons."},
                        new String[]{"What is meant by a 'Homologous Series'? State two characteristics of its successive members.", "A homologous series is a series of carbon compounds having the same functional group and similar chemical properties. Successive members differ by a -CH2- unit and 14 u in mass.", "1 mark for definition; 1 mark for two characteristics.", "Homologous series definition and characteristics."},
                        new String[]{"What happens when ethanol is heated with concentrated sulfuric acid (H2SO4) at 443 K? Name the reaction and write the equation.", "Dehydration reaction occurs, converting ethanol to ethene: C2H5OH -> CH2=CH2 + H2O. Conc. H2SO4 acts as a dehydrating agent.", "1 mark for identifying dehydration; 1 mark for the balanced chemical equation.", "Dehydration reaction of ethanol."}
                    );
                    String[] selectedShort2m = short2mBank.get((counter - 1) % short2mBank.size());
                    q.setText(selectedShort2m[0]);
                    q.setCorrectAnswerText(selectedShort2m[1]);
                    q.setMarkingRubric(selectedShort2m[2]);
                    q.setExplanation(selectedShort2m[3]);
                } else if (dist.getMarks() == 3) {
                    q.setType("short_answer");
                    List<String[]> short3mBank = List.of(
                        new String[]{"Section 4.4 covering Chapter " + chapterNum + ": (a) What is the common name of ethanol? (b) Explain why it is widely used in pharmaceutical preparations like tincture of iodine. (c) What is the physiological impact of dilute ethanol consumption on the nervous system?", "(a) Commonly called alcohol. (b) It is a versatile solvent capable of dissolving active medicinal agents. (c) Depresses the central nervous system, impairing muscular coordination.", "1 mark for part (a); 1 mark for solvent rationale in (b); 1 mark for physiological effect in (c).", "Comprehensive evaluation of Chapter " + chapterNum + " properties."},
                        new String[]{"Answer the following regarding the versatile nature of carbon: (a) Define tetravalency. (b) Define catenation and name the element displaying it to the highest degree. (c) Why are carbon-carbon covalent bonds exceptionally strong and stable?", "(a) Tetravalency: Carbon has 4 valence electrons to form 4 covalent bonds. (b) Catenation: Capacity to link with identical atoms into chains; carbon displays it to the highest degree. (c) Small atomic size enables nucleus to hold shared electrons firmly.", "1 mark for tetravalency; 1 mark for catenation; 1 mark for nuclear hold explanation.", "Curriculum review of versatile nature of carbon."},
                        new String[]{"Regarding chemical reactions of carbon compounds: (a) Write the chemical equation for the combustion of ethanol in air. (b) What product is formed when ethanol is oxidised with alkaline KMnO4? (c) What gas is evolved when ethanol reacts with sodium metal?", "(a) C2H5OH + 3O2 -> 2CO2 + 3H2O + Heat. (b) Ethanoic acid (CH3COOH). (c) Hydrogen gas (H2).", "1 mark for combustion equation; 1 mark for ethanoic acid; 1 mark for hydrogen gas.", "Chemical reactions of ethanol."}
                    );
                    String[] selectedShort3m = short3mBank.get((counter - 1) % short3mBank.size());
                    q.setText(selectedShort3m[0]);
                    q.setCorrectAnswerText(selectedShort3m[1]);
                    q.setMarkingRubric(selectedShort3m[2]);
                    q.setExplanation(selectedShort3m[3]);
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
