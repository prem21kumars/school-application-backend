package com.quizapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizapp.dto.AIQuestionGenRequest;
import com.quizapp.entity.ChapterEntity;
import com.quizapp.entity.QuestionEntity;
import com.quizapp.entity.QuestionOptionEntity;
import com.quizapp.entity.SubjectEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.*;

/**
 * Service for generating curriculum questions via Groq's high-speed LLM API.
 * Designed to be strictly optional with graceful offline fallback.
 */
@Service
public class GroqAiService {

    private static final Logger log = LoggerFactory.getLogger(GroqAiService.class);

    @Value("${groq.api.enabled:true}")
    private boolean enabled;

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    @Value("${groq.api.model:openai/gpt-oss-120b}")
    private String model;

    @Value("${groq.api.timeout-seconds:30}")
    private int timeoutSeconds;

    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public GroqAiService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(30));
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.USER_AGENT, "QuizApp-Backend/1.0")
                .build();
    }

    public boolean isAvailable() {
        return enabled && apiKey != null && !apiKey.trim().isEmpty() && !apiKey.contains("YOUR_KEY");
    }

    public String getModel() {
        return model;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Generates structured questions using Groq LLM API.
     * Returns null if generation fails or API is unavailable.
     */
    public List<QuestionEntity> generateQuestions(SubjectEntity subject, ChapterEntity chapter, AIQuestionGenRequest request) {
        if (!isAvailable()) {
            log.debug("Groq AI is disabled or API key is not configured.");
            return null;
        }

        try {
            String prompt = buildPrompt(subject, chapter, request);
            log.info("Sending question generation request to Groq LLM (Model: {}, Count: {}, Difficulty: {})",
                    model, request.getCount(), request.getDifficulty());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("temperature", 0.4);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                    "role", "system",
                    "content", "You are an elite academic curriculum exam creator and teacher. You generate rigorous, curriculum-aligned questions with clear options, explanations, and marking rubrics. You MUST respond with strictly valid JSON only matching the requested schema."
            ));
            messages.add(Map.of(
                    "role", "user",
                    "content", prompt
            ));
            requestBody.put("messages", messages);
            requestBody.put("response_format", Map.of("type", "json_object"));

            String responseJson = restClient.post()
                    .uri(apiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.trim())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            if (responseJson == null || responseJson.trim().isEmpty()) {
                log.warn("Empty response received from Groq AI API");
                return null;
            }

            return parseGroqResponse(responseJson, subject, chapter, request);
        } catch (Exception e) {
            log.warn("Groq AI generation call failed: {}", e.getMessage());
            return null;
        }
    }

    private String buildPrompt(SubjectEntity subject, ChapterEntity chapter, AIQuestionGenRequest request) {
        String subjectName = subject != null ? subject.getName() : "General Science";
        String chapterTitle = chapter != null ? chapter.getTitle() : "Curriculum Core";
        int chapterNum = chapter != null && chapter.getNumber() != null ? chapter.getNumber() : 1;

        List<String> topics = (chapter != null && chapter.getTopics() != null && !chapter.getTopics().isEmpty())
                ? chapter.getTopics()
                : List.of("Fundamental Principles", "Application Problems", "Formulas");

        List<String> types = (request.getQuestionTypes() != null && !request.getQuestionTypes().isEmpty())
                ? request.getQuestionTypes()
                : List.of("mcq", "true_false", "key_in");

        StringBuilder sb = new StringBuilder();
        sb.append("Generate ").append(request.getCount()).append(" educational exam questions for:\n");
        sb.append("- Subject: ").append(subjectName).append("\n");
        sb.append("- Chapter ").append(chapterNum).append(": ").append(chapterTitle).append("\n");
        sb.append("- Curriculum Topics: ").append(String.join(", ", topics)).append("\n");
        sb.append("- Difficulty Level: ").append(request.getDifficulty()).append("\n");
        sb.append("- Default Marks Per Question: ").append(request.getMarks()).append("\n");
        sb.append("- Allowed Question Types: ").append(String.join(", ", types)).append("\n");

        if (request.getCustomPrompt() != null && !request.getCustomPrompt().trim().isEmpty()) {
            sb.append("- Specific Teacher Instructions: ").append(request.getCustomPrompt().trim()).append("\n");
        }

        sb.append("\nYou MUST return a JSON object with a single key 'questions' containing an array of question objects.\n");
        sb.append("JSON Schema required:\n");
        sb.append("{\n");
        sb.append("  \"questions\": [\n");
        sb.append("    {\n");
        sb.append("      \"text\": \"Question text here\",\n");
        sb.append("      \"type\": \"mcq\" | \"true_false\" | \"key_in\" | \"short_answer\" | \"long_answer\",\n");
        sb.append("      \"marks\": ").append(request.getMarks()).append(",\n");
        sb.append("      \"difficulty\": \"").append(request.getDifficulty()).append("\",\n");
        sb.append("      \"options\": [\n");
        sb.append("        {\"text\": \"Option text\", \"isCorrect\": true},\n");
        sb.append("        {\"text\": \"Distractor option\", \"isCorrect\": false}\n");
        sb.append("      ],\n");
        sb.append("      \"correctAnswerText\": \"Precise answer or model solution\",\n");
        sb.append("      \"explanation\": \"Pedagogical concept explanation\",\n");
        sb.append("      \"markingRubric\": \"Criteria to award full/partial marks\"\n");
        sb.append("    }\n");
        sb.append("  ]\n");
        sb.append("}\n");
        sb.append("\nRequirements:\n");
        sb.append("1. For 'mcq', provide exactly 4 distinct options with exactly one having isCorrect: true.\n");
        sb.append("2. For 'true_false', provide 2 options: 'True' and 'False'.\n");
        sb.append("3. For 'key_in', 'short_answer', and 'long_answer', options can be empty; provide 'correctAnswerText' and 'markingRubric'.\n");
        sb.append("4. Do not wrap output in backticks or markdown; return raw valid JSON object only.");

        return sb.toString();
    }

    private List<QuestionEntity> parseGroqResponse(String responseJson, SubjectEntity subject, ChapterEntity chapter, AIQuestionGenRequest request) {
        List<QuestionEntity> list = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                log.warn("Groq response has no choices array");
                return null;
            }

            String content = choices.get(0).path("message").path("content").asText();
            if (content == null || content.trim().isEmpty()) {
                log.warn("Groq choice message has empty content");
                return null;
            }

            // Parse inner JSON from LLM
            JsonNode parsedContent = objectMapper.readTree(content);
            JsonNode questionsNode = parsedContent.path("questions");
            if (!questionsNode.isArray()) {
                // If LLM returned array directly or different root
                if (parsedContent.isArray()) {
                    questionsNode = parsedContent;
                } else {
                    log.warn("Unable to find 'questions' array in Groq LLM output");
                    return null;
                }
            }

            long timestamp = System.currentTimeMillis();
            int index = 0;
            String subjectId = subject != null ? subject.getId() : request.getSubjectId();
            String chapterId = chapter != null ? chapter.getId() : request.getChapterId();
            int chapterNum = chapter != null && chapter.getNumber() != null ? chapter.getNumber() : 1;

            for (JsonNode qNode : questionsNode) {
                index++;
                String qId = "groq-" + timestamp + "-" + index;
                String text = qNode.path("text").asText("Question " + index);
                String type = qNode.path("type").asText("mcq").toLowerCase();
                int marks = qNode.has("marks") ? qNode.path("marks").asInt(request.getMarks()) : request.getMarks();
                String diff = qNode.path("difficulty").asText(request.getDifficulty());
                String explanation = qNode.path("explanation").asText("");
                String correctAnswer = qNode.path("correctAnswerText").asText("");
                String rubric = qNode.path("markingRubric").asText("");

                QuestionEntity q = new QuestionEntity();
                q.setId(qId);
                q.setText(text);
                q.setType(type);
                q.setSubjectId(subjectId);
                q.setChapterId(chapterId);
                q.setChapterNumber(chapterNum);
                q.setMarks(marks);
                q.setDifficulty(diff);
                q.setExplanation(explanation);
                q.setCorrectAnswerText(correctAnswer);
                q.setMarkingRubric(rubric);
                q.setSource("groq_ai");
                q.setApproved(false);

                JsonNode optionsNode = qNode.path("options");
                if (optionsNode.isArray()) {
                    int optIndex = 1;
                    for (JsonNode optNode : optionsNode) {
                        String optText = optNode.path("text").asText("");
                        boolean isCorrect = optNode.path("isCorrect").asBoolean(false);
                        if (!optText.trim().isEmpty()) {
                            QuestionOptionEntity opt = new QuestionOptionEntity("opt-" + qId + "-" + optIndex++, optText, isCorrect);
                            q.addOption(opt);
                        }
                    }
                }

                // If MCQ had no options or missed correct answer text, fix it up
                if ("mcq".equals(type) && q.getOptions().isEmpty()) {
                    q.addOption(new QuestionOptionEntity("opt-" + qId + "-1", "Option A", true));
                    q.addOption(new QuestionOptionEntity("opt-" + qId + "-2", "Option B", false));
                    q.addOption(new QuestionOptionEntity("opt-" + qId + "-3", "Option C", false));
                    q.addOption(new QuestionOptionEntity("opt-" + qId + "-4", "Option D", false));
                }

                list.add(q);
            }

            log.info("Successfully parsed {} questions from Groq LLM", list.size());
            return list;
        } catch (Exception e) {
            log.error("Failed to parse Groq response JSON: {}", e.getMessage(), e);
            return null;
        }
    }
}
