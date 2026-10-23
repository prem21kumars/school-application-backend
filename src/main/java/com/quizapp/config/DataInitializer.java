package com.quizapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizapp.entity.*;
import com.quizapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SubjectRepository subjectRepository;
    private final BookSourceRepository bookSourceRepository;
    private final AssessmentRepository assessmentRepository;
    private final StudentExamAttemptRepository attemptRepository;
    private final OfflineAnswerSheetRepository offlineSheetRepository;
    private final ReattemptRequestRepository reattemptRequestRepository;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public DataInitializer(UserRepository userRepository,
                           SchoolClassRepository schoolClassRepository,
                           SubjectRepository subjectRepository,
                           BookSourceRepository bookSourceRepository,
                           AssessmentRepository assessmentRepository,
                           StudentExamAttemptRepository attemptRepository,
                           OfflineAnswerSheetRepository offlineSheetRepository,
                           ReattemptRequestRepository reattemptRequestRepository,
                           NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.subjectRepository = subjectRepository;
        this.bookSourceRepository = bookSourceRepository;
        this.assessmentRepository = assessmentRepository;
        this.attemptRepository = attemptRepository;
        this.offlineSheetRepository = offlineSheetRepository;
        this.reattemptRequestRepository = reattemptRequestRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seedDatabase();
        }
    }

    @Transactional
    public void seedDatabase() {
        // Clear old
        notificationRepository.deleteAll();
        reattemptRequestRepository.deleteAll();
        offlineSheetRepository.deleteAll();
        attemptRepository.deleteAll();
        assessmentRepository.deleteAll();
        bookSourceRepository.deleteAll();
        subjectRepository.deleteAll();
        schoolClassRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Seed Classes
        SchoolClassEntity cls10a = new SchoolClassEntity("cls-10a", "Grade 10", "A", "2026-2027", 32, List.of("sub-phy", "sub-math", "sub-chem", "sub-bio"));
        SchoolClassEntity cls10b = new SchoolClassEntity("cls-10b", "Grade 10", "B", "2026-2027", 30, List.of("sub-phy", "sub-math", "sub-chem"));
        SchoolClassEntity cls9a = new SchoolClassEntity("cls-9a", "Grade 9", "A", "2026-2027", 28, List.of("sub-phy", "sub-math", "sub-eng"));
        SchoolClassEntity cls9b = new SchoolClassEntity("cls-9b", "Grade 9", "B", "2026-2027", 29, List.of("sub-math", "sub-eng"));
        SchoolClassEntity cls11sci = new SchoolClassEntity("cls-11sci", "Grade 11", "Science", "2026-2027", 35, List.of("sub-phy", "sub-chem", "sub-math"));
        schoolClassRepository.saveAll(List.of(cls10a, cls10b, cls9a, cls9b, cls11sci));

        // 2. Seed Users
        UserEntity principal = new UserEntity("usr-principal", "Dr. Arthur Vance", "principal@oakridge.edu", "principal",
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80",
                null, null, null, "active", "admin", null, null);

        UserEntity teacherSarah = new UserEntity("usr-teacher-sarah", "Dr. Sarah Jenkins", "sarah.jenkins@oakridge.edu", "teacher",
                "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150&auto=format&fit=crop&q=80",
                null, null, null, "active", "teach", List.of("cls-10a", "cls-10b", "cls-11sci"), List.of("sub-phy"));

        UserEntity teacherAlan = new UserEntity("usr-teacher-alan", "Prof. Alan Turing", "alan.turing@oakridge.edu", "teacher",
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80",
                null, null, null, "active", "teach", List.of("cls-10a", "cls-10b", "cls-9a"), List.of("sub-math"));

        UserEntity teacherElena = new UserEntity("usr-teacher-elena", "Mrs. Elena Rostova", "elena.rostova@oakridge.edu", "teacher",
                "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150&auto=format&fit=crop&q=80",
                null, null, null, "active", "teach", List.of("cls-10a", "cls-9b"), List.of("sub-chem"));

        UserEntity stdAlex = new UserEntity("usr-std-alex", "Alex Rivera", "alex.rivera@student.oakridge.edu", "student",
                "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=150&auto=format&fit=crop&q=80",
                "ENR-2026-101", "cls-10a", "A", "active", "pass", null, null);

        UserEntity stdMaya = new UserEntity("usr-std-maya", "Maya Patel", "maya.patel@student.oakridge.edu", "student",
                "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150&auto=format&fit=crop&q=80",
                "ENR-2026-102", "cls-10a", "A", "active", "pass", null, null);

        UserEntity stdDavid = new UserEntity("usr-std-david", "David Kim", "david.kim@student.oakridge.edu", "student",
                "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&auto=format&fit=crop&q=80",
                "ENR-2026-103", "cls-10a", "A", "active", "pass", null, null);

        UserEntity stdSophia = new UserEntity("usr-std-sophia", "Sophia Chen", "sophia.chen@student.oakridge.edu", "student",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=150&auto=format&fit=crop&q=80",
                "ENR-2026-104", "cls-10b", "B", "active", "pass", null, null);

        UserEntity stdLucas = new UserEntity("usr-std-lucas", "Lucas Miller", "lucas.miller@student.oakridge.edu", "student",
                "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=150&auto=format&fit=crop&q=80",
                "ENR-2026-105", "cls-10b", "B", "active", "pass", null, null);

        UserEntity parentAlex = new UserEntity("usr-parent-alex", "Carlos & Maria Rivera", "carlos.rivera@gmail.com", "parent",
                "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80",
                "ENR-2026-101", null, null, "active", "pass", null, null);

        userRepository.saveAll(List.of(principal, teacherSarah, teacherAlan, teacherElena, stdAlex, stdMaya, stdDavid, stdSophia, stdLucas, parentAlex));

        // 3. Seed Subjects & Chapters
        SubjectEntity subPhy = new SubjectEntity("sub-phy", "Physics", "PHY-101");
        ChapterEntity chPhy1 = new ChapterEntity("ch-phy-1", 1, "Light - Reflection and Refraction",
                List.of("Spherical Mirrors", "Mirror Formula & Magnification", "Refraction of Light", "Refractive Index", "Lens Formula & Power"),
                "Fundamental study of geometrical optics, focal length calculations, ray diagrams, Snell's law, and optical power.",
                "Light travels along straight lines in a homogeneous medium. Reflection follows the laws where the angle of incidence equals angle of reflection. Concave mirrors form real/inverted images except when the object is between pole and focus. The mirror formula is 1/f = 1/v + 1/u. Refraction occurs due to change in speed of light across media.");
        ChapterEntity chPhy2 = new ChapterEntity("ch-phy-2", 2, "Human Eye and Colourful World",
                List.of("Structure of Human Eye", "Defects of Vision (Myopia, Hypermetropia)", "Dispersion through Prism", "Atmospheric Refraction", "Tyndall Effect & Scattering"),
                "Optical physiology of the human eye, corrective lenses, rainbow formation, twinkling of stars, and blue sky scattering.",
                "The human eye lens accommodates focus through ciliary muscles. Myopia is corrected with concave lenses, whereas Hypermetropia is corrected with convex lenses. Sunlight disperses into VIBGYOR through a glass prism.");
        ChapterEntity chPhy3 = new ChapterEntity("ch-phy-3", 3, "Electricity & Circuits",
                List.of("Electric Current & Potential Difference", "Ohm's Law & Resistance", "Factors Affecting Resistance", "Series & Parallel Resistors", "Joule's Heating Effect", "Electric Power"),
                "Quantitative electrical physics, circuit analysis, resistivity formulas, thermal dissipation, and kilowatt-hour metering.",
                "Electric current I = Q/t. Ohm's law establishes V = IR at constant temperature. Resistance R = rho(L/A). In series, R = R1 + R2 + R3. In parallel, 1/R = 1/R1 + 1/R2 + 1/R3. Heating H = I^2 R t.");
        ChapterEntity chPhy4 = new ChapterEntity("ch-phy-4", 4, "Magnetic Effects of Electric Current",
                List.of("Magnetic Field Lines", "Right-Hand Thumb Rule", "Force on Current-Carrying Conductor", "Fleming's Left-Hand Rule", "Electromagnetic Induction", "Electric Generator & Motor"),
                "Electromagnetism principles, magnetic dipole moments, Lorentz force interactions, and induced electromotive force.",
                "Hans Christian Oersted discovered that current generates a magnetic field. Fleming's Left Hand Rule gives the direction of magnetic force on a current conductor. Faraday's law describes electromagnetic induction.");
        subPhy.addChapter(chPhy1);
        subPhy.addChapter(chPhy2);
        subPhy.addChapter(chPhy3);
        subPhy.addChapter(chPhy4);

        SubjectEntity subMath = new SubjectEntity("sub-math", "Mathematics", "MTH-102");
        ChapterEntity chMath1 = new ChapterEntity("ch-math-1", 1, "Real Numbers & Fundamental Arithmetic",
                List.of("Euclid's Division Lemma", "Prime Factorisation", "HCF & LCM Proofs", "Irrationality Proofs (root 2, root 3)"),
                "Number theory fundamentals, prime factorization properties, and rational/irrational number proofs.",
                "Every composite number can be expressed uniquely as a product of prime numbers. HCF(a,b) * LCM(a,b) = a * b.");
        ChapterEntity chMath2 = new ChapterEntity("ch-math-2", 2, "Polynomials & Algebraic Equations",
                List.of("Zeroes of a Polynomial", "Relationship between Zeroes and Coefficients", "Quadratic & Cubic Polynomials", "Division Algorithm"),
                "Degree of polynomials, parabolic graphing of quadratic curves, sum and product of zeroes.",
                "For quadratic polynomial ax^2 + bx + c with roots alpha and beta: alpha + beta = -b/a, and alpha*beta = c/a.");
        ChapterEntity chMath3 = new ChapterEntity("ch-math-3", 3, "Quadratic Equations & Roots",
                List.of("Standard Form", "Factorisation Method", "Completing the Square", "Quadratic Formula & Nature of Roots"),
                "Solving second-degree polynomials, nature of roots based on discriminant D.",
                "Quadratic formula x = (-b +- sqrt(b^2 - 4ac)) / (2a). If D > 0: two distinct real roots. If D = 0: two equal real roots. If D < 0: no real roots.");
        ChapterEntity chMath4 = new ChapterEntity("ch-math-4", 4, "Arithmetic Progressions",
                List.of("nth Term of an AP", "Sum of First n Terms", "Applications in Daily Life"),
                "Linear sequences with constant difference, summation algorithms, and recurrence formulas.",
                "The nth term an = a + (n - 1)d. The sum of first n terms Sn = n/2 * [2a + (n - 1)d].");
        subMath.addChapter(chMath1);
        subMath.addChapter(chMath2);
        subMath.addChapter(chMath3);
        subMath.addChapter(chMath4);

        SubjectEntity subChem = new SubjectEntity("sub-chem", "Chemistry", "CHM-103");
        ChapterEntity chChem1 = new ChapterEntity("ch-chem-1", 1, "Chemical Reactions and Equations",
                List.of("Balancing Equations", "Types: Combination, Decomposition, Displacement", "Oxidation and Reduction", "Corrosion and Rancidity"),
                "Stoichiometry, law of conservation of mass, redox chemistry, and reaction classifications.",
                "Chemical equations must be balanced to satisfy conservation of mass. Decomposition requires energy in the form of heat, light, or electricity.");
        ChapterEntity chChem2 = new ChapterEntity("ch-chem-2", 2, "Acids, Bases and Salts",
                List.of("pH Scale & Indicators", "Chemical Properties of Acids/Bases", "Bleaching Powder, Baking Soda, Plaster of Paris"),
                "Proton transfer, neutralization reactions, universal pH scales, and industrial chemical manufacture.",
                "Acids produce H+(aq) ions while bases produce OH-(aq) ions. pH = -log[H+]. Neutralization: Acid + Base -> Salt + Water.");
        subChem.addChapter(chChem1);
        subChem.addChapter(chChem2);

        subjectRepository.saveAll(List.of(subPhy, subMath, subChem));

        // 4. Seed Books
        String phyBookChapters = "[{\"chapterNumber\":1,\"title\":\"Geometrical Optics & Refraction Theory\",\"pageRange\":\"pp. 12-48\",\"summary\":\"Covers spherical concave and convex mirrors, ray diagrams, lens maker principles, and numerical problems on magnification.\",\"sampleText\":\"When a ray of light passes from an optically rarer medium to a denser medium, it bends towards the normal. The ratio of sine of angle of incidence to sine of angle of refraction is constant, known as Snell's law. Focal length of a spherical mirror is half of its radius of curvature (f = R/2).\"},{\"chapterNumber\":2,\"title\":\"Atmospheric Optics & Human Vision\",\"pageRange\":\"pp. 49-76\",\"summary\":\"Biological optics, ciliary accommodation, focal defects, prisms, dispersion, and atmospheric scattering effects.\",\"sampleText\":\"The power of accommodation is the ability of the eye lens to adjust its focal length. The least distance of distinct vision for a normal young adult is approximately 25 cm. Atmospheric refraction causes stars to twinkle and the sun to appear 2 minutes earlier at sunrise.\"},{\"chapterNumber\":3,\"title\":\"Current Electricity & Circuit Dynamics\",\"pageRange\":\"pp. 77-115\",\"summary\":\"Electric fields, drift velocity, Ohm's law, resistor networks, Kirchhoff fundamentals, and power dissipation.\",\"sampleText\":\"Ohm's Law states that current is directly proportional to potential difference across a conductor at constant temperature. 1 Joule per Coulomb equals 1 Volt. Resistors in parallel share equal voltage and divide total current.\"},{\"chapterNumber\":4,\"title\":\"Electromagnetism & Induction Principles\",\"pageRange\":\"pp. 116-154\",\"summary\":\"Magnetic fields around solenoids, Lorentz force, Faraday induction, Lenz's law, AC vs DC generators.\",\"sampleText\":\"A current-carrying conductor in a magnetic field experiences a force perpendicular to both current and field, determined by Fleming's Left Hand Rule: F = BIL sin(theta).\"}]";
        BookSourceEntity bkPhy = new BookSourceEntity("bk-phy-10", "Standard Physics: Concepts & Applications (Grade 10)", "sub-phy", "Grade 10", "Dr. Katherine Thorne, Ph.D.", null, phyBookChapters);

        String mathBookChapters = "[{\"chapterNumber\":1,\"title\":\"Number Systems & Fundamental Arithmetic\",\"pageRange\":\"pp. 1-28\",\"summary\":\"Proofs of irrational numbers, prime factor trees, decimal expansions of rational numbers.\",\"sampleText\":\"Let p be a prime number. If p divides a^2, then p divides a, where a is a positive integer. This theorem proves sqrt(2) is irrational.\"},{\"chapterNumber\":3,\"title\":\"Quadratic Equations & Optimization\",\"pageRange\":\"pp. 60-92\",\"summary\":\"Discriminant analysis, graphical parabolas, solving real-world kinematic and geometric problems.\",\"sampleText\":\"For equation ax^2 + bx + c = 0, roots are given by x = (-b +- sqrt(b^2 - 4ac)) / 2a. Real roots exist only when b^2 - 4ac >= 0.\"}]";
        BookSourceEntity bkMath = new BookSourceEntity("bk-math-10", "Modern Mathematics for Secondary Schools", "sub-math", "Grade 10", "Prof. Marcus Vance", null, mathBookChapters);

        bookSourceRepository.saveAll(List.of(bkPhy, bkMath));

        // 5. Seed Assessments
        AssessmentEntity asmPhy = new AssessmentEntity();
        asmPhy.setId("asm-phy-term1");
        asmPhy.setTitle("Physics Mid-Term Assessment 2026");
        asmPhy.setType("exam");
        asmPhy.setSubjectId("sub-phy");
        asmPhy.setClassIds(List.of("cls-10a", "cls-10b"));
        asmPhy.setMode("online");
        asmPhy.setDurationMinutes(45);
        asmPhy.setTotalMarks(25);
        asmPhy.setPassingMarks(10);
        asmPhy.setStartDate("2026-09-20T09:00:00Z");
        asmPhy.setEndDate("2026-09-30T18:00:00Z");
        asmPhy.setRandomizeOrder(true);
        asmPhy.setShowAnswersAfterSubmission(true);
        asmPhy.setStatus("published");
        asmPhy.setCreatedByTeacherId("usr-teacher-sarah");
        asmPhy.setCreatedAt("2026-09-15T10:00:00Z");
        asmPhy.setInstructions(List.of(
                "Ensure a stable internet connection throughout the test.",
                "Switching browser tabs or exiting fullscreen mode will trigger a proctoring violation warning.",
                "Accumulating 3 malpractice violations will terminate the exam automatically."
        ));

        // Questions for asmPhy
        QuestionEntity q1 = new QuestionEntity("q-phy-1", "What is the focal length of a spherical concave mirror whose radius of curvature is 30 cm?",
                "mcq", "sub-phy", "ch-phy-1", 1, 2, "easy", null,
                "For a concave mirror, focal length f = R/2 = -30/2 = -15 cm according to Cartesian sign convention.", null, "manual", true);
        q1.addOption(new QuestionOptionEntity("opt-1", "+30 cm", false));
        q1.addOption(new QuestionOptionEntity("opt-2", "-15 cm", true));
        q1.addOption(new QuestionOptionEntity("opt-3", "+15 cm", false));
        q1.addOption(new QuestionOptionEntity("opt-4", "-60 cm", false));

        QuestionEntity q2 = new QuestionEntity("q-phy-2", "The twinkling of stars at night is primarily caused by which optical phenomenon?",
                "mcq", "sub-phy", "ch-phy-2", 2, 2, "easy", null,
                "Light from stars continuously refracts through atmospheric layers of varying temperature and density, causing apparent position shifts.", null, "book_upload", true);
        q2.addOption(new QuestionOptionEntity("opt-5", "Total internal reflection", false));
        q2.addOption(new QuestionOptionEntity("opt-6", "Atmospheric refraction through changing air densities", true));
        q2.addOption(new QuestionOptionEntity("opt-7", "Scattering of light by cosmic dust", false));
        q2.addOption(new QuestionOptionEntity("opt-8", "Dispersion of light through rain droplets", false));

        QuestionEntity q3 = new QuestionEntity("q-phy-3", "State whether true or false: The power of a diverging (concave) lens is always positive (+D).",
                "true_false", "sub-phy", "ch-phy-1", 1, 1, "easy", null,
                "Power P = 1/f (in meters). Since the focal length of a concave lens is negative, its power is always negative.", null, "manual", true);
        q3.addOption(new QuestionOptionEntity("opt-9", "True", false));
        q3.addOption(new QuestionOptionEntity("opt-10", "False", true));

        QuestionEntity q4 = new QuestionEntity("q-phy-4", "A wire of resistance 20 Ohm is drawn out so that its length is doubled. What is its new resistance in Ohms?",
                "key_in", "sub-phy", "ch-phy-3", 3, 5, "medium", "80",
                "Since volume is constant (V = A * L), doubling length halves cross-sectional area (A' = A/2). New R' = 4 * R = 80 Ohm.",
                "2 marks for volume conservation concept, 2 marks for ratio calculation, 1 mark for correct numerical answer (80).", "ai_generated", true);

        QuestionEntity q5 = new QuestionEntity("q-phy-5", "Explain Joule's Law of Heating. Derive the formula H = I^2 R t and name two electrical appliances based on this principle.",
                "long_answer", "sub-phy", "ch-phy-3", 3, 10, "hard",
                "Joule's law states that heat generated in a resistor is directly proportional to square of current, resistance, and time duration (H = I^2 R t). Appliances include electric iron, toaster, water heater, and electric fuse.",
                "3 marks for statement of law, 4 marks for mathematical derivation, 3 marks for naming and explaining two appliances.", "book_upload", true);

        QuestionEntity q6 = new QuestionEntity("q-phy-6", "State Fleming's Left Hand Rule and specify what the thumb, forefinger, and middle finger represent.",
                "short_answer", "sub-phy", "ch-phy-4", 4, 5, "medium",
                "Stretch thumb, forefinger, and middle finger mutually perpendicular. Forefinger represents Magnetic Field, Middle finger represents Current, and Thumb represents Force/Motion of conductor.",
                "2 marks for mutual perpendicular alignment rule, 3 marks for correctly mapping each of the three fingers.", "manual", true);

        asmPhy.addQuestion(q1);
        asmPhy.addQuestion(q2);
        asmPhy.addQuestion(q3);
        asmPhy.addQuestion(q4);
        asmPhy.addQuestion(q5);
        asmPhy.addQuestion(q6);

        AssessmentEntity asmMath = new AssessmentEntity();
        asmMath.setId("asm-math-offline");
        asmMath.setTitle("Grade 10 Mathematics Term 1 Examination (Offline 3-Sets)");
        asmMath.setType("term_exam");
        asmMath.setSubjectId("sub-math");
        asmMath.setClassIds(List.of("cls-10a", "cls-10b"));
        asmMath.setMode("offline");
        asmMath.setDurationMinutes(90);
        asmMath.setTotalMarks(50);
        asmMath.setPassingMarks(20);
        asmMath.setStartDate("2026-09-28T09:00:00Z");
        asmMath.setEndDate("2026-09-28T10:30:00Z");
        asmMath.setRandomizeOrder(true);
        asmMath.setShowAnswersAfterSubmission(false);
        asmMath.setSetsCount(3);
        asmMath.setStatus("published");
        asmMath.setCreatedByTeacherId("usr-teacher-alan");
        asmMath.setCreatedAt("2026-09-22T14:30:00Z");
        asmMath.setInstructions(List.of(
                "Write your Enrolment Number and Set Code on the top of the answer booklet.",
                "All questions are compulsory. Internal choices are provided.",
                "Use of calculators is strictly prohibited."
        ));

        QuestionEntity qm1 = new QuestionEntity("q-mth-1", "If HCF(306, 657) = 9, find the LCM(306, 657).",
                "key_in", "sub-math", "ch-math-1", 1, 2, "easy", "22338",
                "HCF * LCM = Product of numbers => LCM = (306 * 657) / 9 = 22338.", null, "manual", true);

        QuestionEntity qm2 = new QuestionEntity("q-mth-2", "Find the discriminant of the quadratic equation 2x^2 - 4x + 3 = 0 and hence describe the nature of its roots.",
                "short_answer", "sub-math", "ch-math-3", 3, 3, "medium",
                "D = b^2 - 4ac = (-4)^2 - 4(2)(3) = 16 - 24 = -8. Since D < 0, the equation has no real roots.",
                "1.5 marks for calculating D = -8, 1.5 marks for deducing no real roots.", "book_upload", true);

        QuestionEntity qm3 = new QuestionEntity("q-mth-3", "Prove that sqrt(5) is an irrational number.",
                "long_answer", "sub-math", "ch-math-1", 1, 5, "hard",
                "Assume sqrt(5) = a/b where a,b are co-prime integers. 5b^2 = a^2 => 5 divides a. Let a = 5k => 5b^2 = 25k^2 => b^2 = 5k^2 => 5 divides b. Contradicts co-prime assumption.",
                "1 mark for contradiction assumption, 2 marks for showing 5 divides a, 1 mark for showing 5 divides b, 1 mark for concluding contradiction.", "book_upload", true);

        QuestionEntity qm4 = new QuestionEntity("q-mth-4", "How many terms of the AP: 9, 17, 25, ... must be taken to give a sum of 636?",
                "long_answer", "sub-math", "ch-math-4", 4, 5, "hard",
                "a = 9, d = 8. Sn = n/2 [2(9) + (n-1)8] = 636 => 4n^2 + 5n - 636 = 0 => (4n + 53)(n - 12) = 0. Since n must be positive integer, n = 12 terms.",
                "2 marks for setting up quadratic equation, 2 marks for factorisation, 1 mark for rejecting non-integer root and getting n=12.", "ai_generated", true);

        asmMath.addQuestion(qm1);
        asmMath.addQuestion(qm2);
        asmMath.addQuestion(qm3);
        asmMath.addQuestion(qm4);

        assessmentRepository.saveAll(List.of(asmPhy, asmMath));

        // 6. Seed Student Attempts
        StudentExamAttemptEntity attAlex = new StudentExamAttemptEntity();
        attAlex.setId("att-alex-phy");
        attAlex.setAssessmentId("asm-phy-term1");
        attAlex.setStudentId("usr-std-alex");
        attAlex.setStudentName("Alex Rivera");
        attAlex.setEnrolmentNumber("ENR-2026-101");
        attAlex.setClassId("cls-10a");
        attAlex.setStartTime("2026-09-24T10:00:00Z");
        attAlex.setSubmitTime("2026-09-24T10:38:00Z");
        attAlex.setStatus("evaluated");
        attAlex.setTotalScore(23);
        attAlex.setMaxScore(25);
        attAlex.setPercentage(92);
        attAlex.setMalpracticeCount(0);
        attAlex.setAnswersJson("[{\"questionId\":\"q-phy-1\",\"studentAnswer\":\"opt-2\",\"isCorrect\":true,\"marksObtained\":2,\"evaluatedBy\":\"auto\"},{\"questionId\":\"q-phy-2\",\"studentAnswer\":\"opt-6\",\"isCorrect\":true,\"marksObtained\":2,\"evaluatedBy\":\"auto\"},{\"questionId\":\"q-phy-3\",\"studentAnswer\":\"opt-10\",\"isCorrect\":true,\"marksObtained\":1,\"evaluatedBy\":\"auto\"},{\"questionId\":\"q-phy-4\",\"studentAnswer\":\"80\",\"isCorrect\":true,\"marksObtained\":5,\"evaluatedBy\":\"auto\"},{\"questionId\":\"q-phy-5\",\"studentAnswer\":\"Joule's law states that heat is proportional to square of current, resistance, and time: H = I^2 R t. Derived using W = V*Q and V = I*R. Examples: Electric toaster and water geyser.\",\"isCorrect\":true,\"marksObtained\":9,\"evaluatedBy\":\"teacher\",\"teacherComment\":\"Excellent derivation, comprehensive explanation.\"},{\"questionId\":\"q-phy-6\",\"studentAnswer\":\"Fleming left hand rule: Forefinger is Magnetic Field, Middle is Current, Thumb is Force direction.\",\"isCorrect\":true,\"marksObtained\":4,\"evaluatedBy\":\"teacher\",\"teacherComment\":\"Good summary, could have mentioned perpendicular alignment.\"}]");

        StudentExamAttemptEntity attDavid = new StudentExamAttemptEntity();
        attDavid.setId("att-david-phy");
        attDavid.setAssessmentId("asm-phy-term1");
        attDavid.setStudentId("usr-std-david");
        attDavid.setStudentName("David Kim");
        attDavid.setEnrolmentNumber("ENR-2026-103");
        attDavid.setClassId("cls-10a");
        attDavid.setStartTime("2026-09-24T11:15:00Z");
        attDavid.setSubmitTime("2026-09-24T11:22:00Z");
        attDavid.setStatus("malpractice_terminated");
        attDavid.setTotalScore(0);
        attDavid.setMaxScore(25);
        attDavid.setPercentage(0);
        attDavid.setMalpracticeCount(3);
        attDavid.setReattemptRequested(true);
        attDavid.setAnswersJson("[{\"questionId\":\"q-phy-1\",\"studentAnswer\":\"opt-2\",\"isCorrect\":true,\"marksObtained\":0,\"evaluatedBy\":\"auto\"}]");

        attDavid.addMalpracticeLog(new MalpracticeLogEntity("mp-1", "2026-09-24T11:18:12Z", "tab_switch", "Switched browser tab away from exam portal to external tab.", 1));
        attDavid.addMalpracticeLog(new MalpracticeLogEntity("mp-2", "2026-09-24T11:20:45Z", "key_press", "Prohibited keystroke combination detected (Ctrl+C / Clipboard Copy).", 2));
        attDavid.addMalpracticeLog(new MalpracticeLogEntity("mp-3", "2026-09-24T11:22:04Z", "tab_switch", "Window focus lost repeatedly (Tab switch #2). Threshold exceeded (3/3). Exam auto-terminated.", 3));

        attemptRepository.saveAll(List.of(attAlex, attDavid));

        // 7. Seed Reattempt Request
        ReattemptRequestEntity req1 = new ReattemptRequestEntity("req-david-1", "att-david-phy", "usr-std-david", "David Kim",
                "ENR-2026-103", "asm-phy-term1", "Physics Mid-Term Assessment 2026", "usr-teacher-sarah",
                "My browser accidentally switched window due to an antivirus popup. I kindly request permission to retake the test under supervision.",
                "2026-09-24T11:25:00Z", "pending");
        reattemptRequestRepository.save(req1);

        // 8. Seed Offline Sheet
        String offlineSheetEvals = "[{\"questionId\":\"q-mth-1\",\"questionNumber\":1,\"maxMarks\":2,\"extractedText\":\"HCF * LCM = 306 * 657\\nLCM = (306 * 657) / 9 = 34 * 657 = 22338\",\"aiProposedMarks\":2,\"aiConfidence\":98,\"aiReasoning\":\"Exact calculation steps shown and correct final answer 22338 obtained.\",\"teacherMarks\":2,\"isFlaggedForReview\":false},{\"questionId\":\"q-mth-2\",\"questionNumber\":2,\"maxMarks\":3,\"extractedText\":\"2x^2 - 4x + 3 = 0\\nD = b^2 - 4ac = (-4)^2 - 4(2)(3) = 16 - 24 = -8\\nSince D < 0, there are no real roots.\",\"aiProposedMarks\":3,\"aiConfidence\":96,\"aiReasoning\":\"Discriminant calculated accurately as -8. Correct conclusion stating no real roots.\",\"teacherMarks\":3,\"isFlaggedForReview\":false},{\"questionId\":\"q-mth-3\",\"questionNumber\":3,\"maxMarks\":5,\"extractedText\":\"Let root 5 be rational = a/b where a,b coprime.\\n5b^2 = a^2 => 5 divides a^2, so 5 divides a.\\nLet a = 5k => 5b^2 = 25k^2 => b^2 = 5k^2 => 5 divides b.\\nHence a, b have common factor 5, contradiction. Thus root 5 is irrational.\",\"aiProposedMarks\":5,\"aiConfidence\":92,\"aiReasoning\":\"Complete theorem proof by contradiction following standard Euclid algebra with correct inference steps.\",\"teacherMarks\":5,\"isFlaggedForReview\":false},{\"questionId\":\"q-mth-4\",\"questionNumber\":4,\"maxMarks\":5,\"extractedText\":\"a=9, d=8, Sn=636\\n636 = n/2 [18 + (n-1)8] => 636 = n(9 + 4n - 4) = 4n^2 + 5n\\n4n^2 + 5n - 636 = 0\\n(n - 12)(4n + 53) = 0 => n = 12 or n = -53/4 (rejected).\\nTotal terms = 12.\",\"aiProposedMarks\":4,\"aiConfidence\":78,\"aiReasoning\":\"Handwriting slightly faint on the quadratic formula step, but factorization (n-12)(4n+53) is mathematically sound. Awarded 4/5 for review.\",\"teacherMarks\":4,\"teacherFeedback\":\"Verified handwriting: step is fully correct. Can be upgraded to 5/5 if teacher approves.\",\"isFlaggedForReview\":true}]";
        OfflineAnswerSheetEntity sheetAlex = new OfflineAnswerSheetEntity("sheet-alex-math", "asm-math-offline", "usr-std-alex", "Alex Rivera",
                "ENR-2026-101", "cls-10a",
                "https://images.unsplash.com/photo-1586075010923-2dd4570fb338?w=800&auto=format&fit=crop&q=80",
                "2026-09-24T15:00:00Z", "ai_evaluated", offlineSheetEvals, 14, 15, 93);
        offlineSheetRepository.save(sheetAlex);

        // 9. Seed Notifications
        NotificationEntity n1 = new NotificationEntity("notif-1", "Malpractice Auto-Termination Alert",
                "Student David Kim (ENR-2026-103) examination in \"Physics Mid-Term Assessment 2026\" was terminated due to 3 recorded malpractice violations.",
                "alert", "2026-09-24T11:22:05Z", false, "teacher", "malpractice");
        NotificationEntity n2 = new NotificationEntity("notif-2", "New Re-attempt Request",
                "David Kim has requested permission to re-attempt \"Physics Mid-Term Assessment 2026\".",
                "warning", "2026-09-24T11:25:30Z", false, "teacher", "malpractice");
        NotificationEntity n3 = new NotificationEntity("notif-3", "AI Offline Scanned Sheet Ready for Review",
                "AI grading completed for Alex Rivera's answer sheet (Math Term 1). 1 question flagged for review.",
                "info", "2026-09-24T15:02:00Z", false, "teacher", "offline_eval");
        notificationRepository.saveAll(List.of(n1, n2, n3));
    }
}
