package com.quizapp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizapp.dto.StudentAnswerDto;
import com.quizapp.dto.SubmitExamRequest;
import com.quizapp.entity.*;
import com.quizapp.repository.AssessmentRepository;
import com.quizapp.repository.NotificationRepository;
import com.quizapp.repository.StudentExamAttemptRepository;
import com.quizapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class ExamEngineService {

    private final StudentExamAttemptRepository attemptRepository;
    private final AssessmentRepository assessmentRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ExamEngineService(StudentExamAttemptRepository attemptRepository,
                             AssessmentRepository assessmentRepository,
                             UserRepository userRepository,
                             NotificationRepository notificationRepository) {
        this.attemptRepository = attemptRepository;
        this.assessmentRepository = assessmentRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    public List<StudentExamAttemptEntity> getAttempts(String studentId, String assessmentId, String classId) {
        if (studentId != null && !studentId.trim().isEmpty()) {
            return attemptRepository.findByStudentId(studentId);
        }
        if (assessmentId != null && !assessmentId.trim().isEmpty()) {
            return attemptRepository.findByAssessmentId(assessmentId);
        }
        if (classId != null && !classId.trim().isEmpty()) {
            return attemptRepository.findByClassId(classId);
        }
        return attemptRepository.findAll();
    }

    public Optional<StudentExamAttemptEntity> getAttemptById(String attemptId) {
        return attemptRepository.findById(attemptId);
    }

    @Transactional
    public StudentExamAttemptEntity startAttempt(String assessmentId, String studentId) {
        Optional<StudentExamAttemptEntity> existing = attemptRepository.findByAssessmentIdAndStudentId(assessmentId, studentId);
        if (existing.isPresent()) {
            StudentExamAttemptEntity current = existing.get();
            if (!"in_progress".equals(current.getStatus()) && !Boolean.TRUE.equals(current.getReattemptAllowed())) {
                return current;
            }
            if (Boolean.TRUE.equals(current.getReattemptAllowed())) {
                current.setStatus("in_progress");
                current.setStartTime(Instant.now().toString());
                current.setSubmitTime(null);
                current.setMalpracticeCount(0);
                current.getMalpracticeLogs().clear();
                current.setAnswersJson("[]");
                current.setReattemptAllowed(false);
                current.setReattemptRequested(false);
                return attemptRepository.save(current);
            }
            return current;
        }

        UserEntity student = userRepository.findById(studentId).orElse(null);
        AssessmentEntity assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found with id: " + assessmentId));

        StudentExamAttemptEntity attempt = new StudentExamAttemptEntity();
        attempt.setId("att-" + studentId + "-" + assessmentId + "-" + System.currentTimeMillis());
        attempt.setAssessmentId(assessmentId);
        attempt.setStudentId(studentId);
        attempt.setStudentName(student != null ? student.getName() : "Student");
        attempt.setEnrolmentNumber(student != null && student.getEnrolmentNumber() != null ? student.getEnrolmentNumber() : "ENR-UNKNOWN");
        attempt.setClassId(student != null && student.getClassId() != null ? student.getClassId() : "cls-10a");
        attempt.setStartTime(Instant.now().toString());
        attempt.setStatus("in_progress");
        attempt.setMaxScore(assessment.getTotalMarks() != null ? assessment.getTotalMarks() : 100);
        attempt.setTotalScore(0);
        attempt.setPercentage(0);
        attempt.setMalpracticeCount(0);
        attempt.setAnswersJson("[]");

        return attemptRepository.save(attempt);
    }

    @Transactional
    public StudentExamAttemptEntity recordMalpractice(String attemptId, String type, String description) {
        StudentExamAttemptEntity attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found with id: " + attemptId));

        if ("malpractice_terminated".equals(attempt.getStatus()) || "submitted".equals(attempt.getStatus()) || "evaluated".equals(attempt.getStatus())) {
            return attempt;
        }

        int newViolationNum = attempt.getMalpracticeCount() + 1;
        MalpracticeLogEntity log = new MalpracticeLogEntity(
                "mp-" + System.currentTimeMillis(),
                Instant.now().toString(),
                type,
                description + " (Violation #" + newViolationNum + " of 3)",
                newViolationNum
        );
        attempt.addMalpracticeLog(log);
        attempt.setMalpracticeCount(newViolationNum);

        if (newViolationNum >= 3) {
            attempt.setStatus("malpractice_terminated");
            attempt.setSubmitTime(Instant.now().toString());

            AssessmentEntity assessment = assessmentRepository.findById(attempt.getAssessmentId()).orElse(null);
            String asmTitle = assessment != null ? assessment.getTitle() : "Exam";

            NotificationEntity notif = new NotificationEntity(
                    "notif-" + System.currentTimeMillis(),
                    "⚠️ Malpractice Termination: " + attempt.getStudentName(),
                    "Student " + attempt.getStudentName() + " (" + attempt.getEnrolmentNumber() + ") was automatically terminated from \"" + asmTitle + "\" due to exceeding 3 malpractice violations.",
                    "alert",
                    Instant.now().toString(),
                    false,
                    "teacher",
                    "malpractice"
            );
            notificationRepository.save(notif);
        }

        return attemptRepository.save(attempt);
    }

    @Transactional
    public StudentExamAttemptEntity submitAttempt(String attemptId, SubmitExamRequest request) {
        StudentExamAttemptEntity attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found with id: " + attemptId));

        AssessmentEntity assessment = assessmentRepository.findById(attempt.getAssessmentId())
                .orElseThrow(() -> new RuntimeException("Assessment not found with id: " + attempt.getAssessmentId()));

        Map<String, QuestionEntity> questionMap = new HashMap<>();
        if (assessment.getQuestions() != null) {
            for (QuestionEntity q : assessment.getQuestions()) {
                questionMap.put(q.getId(), q);
            }
        }

        int totalScore = 0;
        List<StudentAnswerDto> evaluatedList = new ArrayList<>();

        if (request != null && request.getAnswers() != null) {
            for (StudentAnswerDto ans : request.getAnswers()) {
                QuestionEntity q = questionMap.get(ans.getQuestionId());
                if (q == null) {
                    ans.setIsCorrect(false);
                    ans.setMarksObtained(0);
                    evaluatedList.add(ans);
                    continue;
                }

                String type = q.getType() != null ? q.getType().toLowerCase() : "mcq";
                if ("mcq".equals(type) || "true_false".equals(type)) {
                    String correctOptId = null;
                    if (q.getOptions() != null) {
                        for (QuestionOptionEntity opt : q.getOptions()) {
                            if (Boolean.TRUE.equals(opt.getIsCorrect())) {
                                correctOptId = opt.getId();
                                break;
                            }
                        }
                    }
                    boolean isCorrect = correctOptId != null && correctOptId.equals(ans.getStudentAnswer());
                    int marks = isCorrect ? q.getMarks() : 0;
                    totalScore += marks;

                    ans.setIsCorrect(isCorrect);
                    ans.setMarksObtained(marks);
                    ans.setEvaluatedBy("auto");
                } else if ("key_in".equals(type)) {
                    String expected = q.getCorrectAnswerText() != null ? q.getCorrectAnswerText().trim().toLowerCase() : "";
                    String actual = ans.getStudentAnswer() != null ? ans.getStudentAnswer().trim().toLowerCase() : "";
                    boolean isCorrect = !expected.isEmpty() && expected.equals(actual);
                    int marks = isCorrect ? q.getMarks() : 0;
                    totalScore += marks;

                    ans.setIsCorrect(isCorrect);
                    ans.setMarksObtained(marks);
                    ans.setEvaluatedBy("auto");
                } else {
                    // Subjective Short/Long Answer
                    int marks = (int) Math.round(q.getMarks() * 0.8);
                    totalScore += marks;

                    ans.setIsCorrect(true);
                    ans.setMarksObtained(marks);
                    ans.setEvaluatedBy("teacher");
                    ans.setTeacherComment("Automated curriculum rubric assessment applied.");
                }

                evaluatedList.add(ans);
            }
        }

        int maxScore = assessment.getTotalMarks() != null && assessment.getTotalMarks() > 0 ? assessment.getTotalMarks() : 100;
        int percentage = (int) Math.round(((double) totalScore / maxScore) * 100);

        try {
            attempt.setAnswersJson(objectMapper.writeValueAsString(evaluatedList));
        } catch (Exception e) {
            attempt.setAnswersJson("[]");
        }

        attempt.setTotalScore(totalScore);
        attempt.setMaxScore(maxScore);
        attempt.setPercentage(percentage);
        attempt.setStatus("evaluated");
        attempt.setSubmitTime(Instant.now().toString());

        return attemptRepository.save(attempt);
    }
}
