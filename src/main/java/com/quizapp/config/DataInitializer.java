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

        // 5. Assessments, attempts, and sheets start clean without mock/dummy data.
    }
}
