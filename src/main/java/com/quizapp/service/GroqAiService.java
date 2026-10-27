package com.quizapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizapp.dto.AIQuestionGenRequest;
import com.quizapp.dto.MarkDistributionItemDto;
import com.quizapp.entity.*;
import com.quizapp.repository.SystemSettingRepository;
import jakarta.annotation.PostConstruct;
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
 * Supports dynamic API key management (stored in MySQL SystemSettingRepository or provided per-request).
 */
@Service
public class GroqAiService {

    private static final Logger log = LoggerFactory.getLogger(GroqAiService.class);

    @Value("${groq.api.enabled:true}")
    private boolean enabled;

    @Value("${groq.api.key:}")
    private String defaultApiKey;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    @Value("${groq.api.model:llama-3.3-70b-versatile}")
    private String defaultModel;

    @Value("${groq.api.timeout-seconds:35}")
    private int timeoutSeconds;

    private String activeApiKey;
    private String activeModel;

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final SystemSettingRepository systemSettingRepository;

    public GroqAiService(ObjectMapper objectMapper, SystemSettingRepository systemSettingRepository) {
        this.objectMapper = objectMapper;
        this.systemSettingRepository = systemSettingRepository;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(45));
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.USER_AGENT, "QuizApp-Backend/1.0")
                .build();
    }

    @PostConstruct
    public void init() {
        this.activeApiKey = defaultApiKey;
        this.activeModel = defaultModel != null && !defaultModel.isBlank() ? defaultModel : "llama-3.3-70b-versatile";
        reloadConfigFromDatabase();
    }

    public synchronized void reloadConfigFromDatabase() {
        try {
            systemSettingRepository.findById("groq.api.key").ifPresent(setting -> {
                if (setting.getValue() != null && !setting.getValue().isBlank()) {
                    this.activeApiKey = setting.getValue().trim();
                    log.info("Loaded custom Groq API key from MySQL system_settings: {}", maskApiKey(this.activeApiKey));
                }
            });
            systemSettingRepository.findById("groq.api.model").ifPresent(setting -> {
                if (setting.getValue() != null && !setting.getValue().isBlank()) {
                    this.activeModel = setting.getValue().trim();
                    log.info("Loaded custom Groq Model from MySQL system_settings: {}", this.activeModel);
                }
            });
        } catch (Exception e) {
            log.warn("Could not load Groq settings from DB: {}", e.getMessage());
        }
    }

    public synchronized Map<String, Object> saveGroqConfig(String apiKey, String model) {
        Map<String, Object> res = new HashMap<>();
        try {
            if (apiKey != null && !apiKey.isBlank()) {
                this.activeApiKey = apiKey.trim();
                systemSettingRepository.save(new SystemSettingEntity("groq.api.key", this.activeApiKey));
            }
            if (model != null && !model.isBlank()) {
                this.activeModel = model.trim();
                systemSettingRepository.save(new SystemSettingEntity("groq.api.model", this.activeModel));
            }
            res.put("success", true);
            res.put("message", "Groq AI API settings saved to MySQL database successfully!");
            res.put("configured", isAvailable());
            res.put("model", this.activeModel);
            res.put("maskedKey", maskApiKey(this.activeApiKey));
            log.info("Saved and activated new Groq AI config in backend (Model: {}, Key: {})", this.activeModel, maskApiKey(this.activeApiKey));
        } catch (Exception e) {
            log.error("Failed to save Groq AI config: {}", e.getMessage(), e);
            res.put("success", false);
            res.put("message", "Failed to save Groq AI settings: " + e.getMessage());
        }
        return res;
    }

    public synchronized Map<String, Object> getGroqConfig() {
        Map<String, Object> map = new HashMap<>();
        map.put("enabled", this.enabled);
        map.put("configured", isAvailable());
        map.put("model", this.activeModel != null ? this.activeModel : "llama-3.3-70b-versatile");
        map.put("maskedKey", maskApiKey(this.activeApiKey));
        return map;
    }

    public boolean isAvailable() {
        return enabled && activeApiKey != null && !activeApiKey.trim().isEmpty() && !activeApiKey.contains("YOUR_KEY");
    }

    public String getModel() {
        return activeModel != null ? activeModel : "llama-3.3-70b-versatile";
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getActiveApiKey() {
        return activeApiKey;
    }

    public void setActiveApiKey(String key) {
        this.activeApiKey = key;
    }

    public void setActiveModel(String model) {
        this.activeModel = model;
    }

    /**
     * Generates structured questions using Groq LLM API from curriculum topic/subject.
     */
    public List<QuestionEntity> generateQuestions(SubjectEntity subject, ChapterEntity chapter, AIQuestionGenRequest request) {
        String effectiveKey = (request.getGroqApiKey() != null && !request.getGroqApiKey().trim().isEmpty())
                ? request.getGroqApiKey().trim()
                : this.activeApiKey;

        String effectiveModel = (request.getGroqModel() != null && !request.getGroqModel().trim().isEmpty())
                ? request.getGroqModel().trim()
                : this.activeModel;

        if (effectiveKey == null || effectiveKey.trim().isEmpty()) {
            log.debug("Groq AI is disabled or API key is not configured.");
            return null;
        }

        try {
            String prompt = buildPrompt(subject, chapter, request);
            log.info("Sending question generation request to Groq LLM (Model: {}, Count: {}, Difficulty: {})",
                    effectiveModel, request.getCount(), request.getDifficulty());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", effectiveModel);
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
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + effectiveKey)
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

    /**
     * Generates questions directly from an uploaded Book / Document using Groq LLM.
     */
    public List<QuestionEntity> generateBookQuestionsWithGroq(
            BookSourceEntity book,
            List<Integer> chapterNumbers,
            List<MarkDistributionItemDto> markDistribution,
            String overrideApiKey,
            String overrideModel
    ) {
        String effectiveKey = (overrideApiKey != null && !overrideApiKey.trim().isEmpty())
                ? overrideApiKey.trim()
                : this.activeApiKey;

        String effectiveModel = (overrideModel != null && !overrideModel.trim().isEmpty())
                ? overrideModel.trim()
                : this.activeModel;

        if (effectiveKey == null || effectiveKey.trim().isEmpty()) {
            log.warn("No Groq API key available for Book Question Generation");
            return null;
        }

        try {
            // Build document excerpt strictly for selected chapters
            StringBuilder documentExcerpt = new StringBuilder();
            String bookTitle = book != null ? book.getTitle() : "Uploaded Syllabus Document";
            String subjectId = book != null ? book.getSubjectId() : "general";

            if (book != null && book.getChaptersJson() != null && !book.getChaptersJson().isBlank()) {
                JsonNode chArray = objectMapper.readTree(book.getChaptersJson());
                if (chArray.isArray()) {
                    for (JsonNode chNode : chArray) {
                        int chNum = chNode.path("chapterNumber").asInt(1);
                        if (chapterNumbers == null || chapterNumbers.isEmpty() || chapterNumbers.contains(chNum)) {
                            String title = chNode.path("title").asText("");
                            String summary = chNode.path("summary").asText("");
                            String sampleText = chNode.path("sampleText").asText("");
                            documentExcerpt.append("--- Chapter ").append(chNum).append(": ").append(title).append(" ---\n");
                            if (!summary.isBlank()) {
                                documentExcerpt.append("Summary: ").append(summary).append("\n");
                            }
                            if (!sampleText.isBlank()) {
                                // Cap to reasonable token limit per chapter
                                String bounded = sampleText.length() > 4000 ? sampleText.substring(0, 4000) + "..." : sampleText;
                                documentExcerpt.append("Text Excerpt: ").append(bounded).append("\n\n");
                            }
                        }
                    }
                }
            }

            if (documentExcerpt.length() == 0) {
                documentExcerpt.append("Title: ").append(bookTitle).append(". Core subject content for ").append(subjectId);
            }

            // Build Mark Distribution prompt breakdown
            StringBuilder distPrompt = new StringBuilder();
            int totalQuestions = 0;
            for (MarkDistributionItemDto dist : markDistribution) {
                if (dist.getCount() > 0) {
                    totalQuestions += dist.getCount();
                    String expectedType = dist.getMarks() == 1 ? "mcq" : (dist.getMarks() <= 3 ? "short_answer" : "long_answer");
                    distPrompt.append("- ").append(dist.getCount()).append(" question(s) of ")
                            .append(dist.getMarks()).append(" marks (Format: ").append(expectedType).append(")\n");
                }
            }

            if (totalQuestions == 0) {
                totalQuestions = 5;
                distPrompt.append("- 5 questions of 2 marks (Format: short_answer)\n");
            }

            StringBuilder userPrompt = new StringBuilder();
            userPrompt.append("You are an expert exam creator. Generate exactly ").append(totalQuestions)
                    .append(" examination questions based ONLY on the following textbook excerpt.\n\n");
            userPrompt.append("DOCUMENT EXCERPT:\n");
            userPrompt.append(documentExcerpt.toString());
            userPrompt.append("\n\nREQUIRED QUESTION DISTRIBUTION:\n");
            userPrompt.append(distPrompt.toString());
            userPrompt.append("""
\nRULES:
1. All questions MUST be strictly derived from facts, definitions, and topics in the excerpt above.
2. For 1-mark questions, use type 'mcq' and provide exactly 4 distinct options with exactly one having isCorrect: true.
3. For 2-mark and 3-mark questions, use type 'short_answer' with accurate correctAnswerText and markingRubric.
4. For 4-mark, 5-mark, and 10-mark questions, use type 'long_answer' with a comprehensive correctAnswerText and markingRubric.
5. Specify 'chapterNumber' as an integer matching the chapter from the excerpt.
6. Return strictly a JSON object matching this schema:
{
  "questions": [
    {
      "text": "Question text here",
      "type": "mcq",
      "marks": 1,
      "chapterNumber": 1,
      "difficulty": "easy",
      "options": [
        {"text": "Option A", "isCorrect": true},
        {"text": "Option B", "isCorrect": false},
        {"text": "Option C", "isCorrect": false},
        {"text": "Option D", "isCorrect": false}
      ],
      "correctAnswerText": "Model solution",
      "explanation": "Rationale citing textbook",
      "markingRubric": "Criteria for scoring marks"
    }
  ]
}
""");

            log.info("Calling Groq LLM (Model: {}) to generate {} book questions for book '{}'",
                    effectiveModel, totalQuestions, bookTitle);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", effectiveModel);
            requestBody.put("temperature", 0.3);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                    "role", "system",
                    "content", "You are an elite academic curriculum examiner and question author. You craft precise, fact-checked questions exclusively from the provided curriculum text. Respond ONLY in valid JSON matching the requested schema."
            ));
            messages.add(Map.of(
                    "role", "user",
                    "content", userPrompt.toString()
            ));
            requestBody.put("messages", messages);
            requestBody.put("response_format", Map.of("type", "json_object"));

            String responseJson = restClient.post()
                    .uri(apiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + effectiveKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            if (responseJson == null || responseJson.trim().isEmpty()) {
                log.warn("Empty response received from Groq LLM for book questions");
                return null;
            }

            return parseBookGroqResponse(responseJson, book, chapterNumbers);
        } catch (Exception e) {
            log.error("Failed to generate book questions via Groq AI: {}", e.getMessage(), e);
            return null;
        }
    }

    private List<QuestionEntity> parseBookGroqResponse(String responseJson, BookSourceEntity book, List<Integer> requestedChapters) {
        List<QuestionEntity> list = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                log.warn("Groq book response has no choices");
                return null;
            }

            String content = choices.get(0).path("message").path("content").asText();
            if (content == null || content.isBlank()) {
                log.warn("Groq choice message has empty content");
                return null;
            }

            JsonNode parsedContent = objectMapper.readTree(content);
            JsonNode questionsNode = parsedContent.path("questions");
            if (!questionsNode.isArray()) {
                if (parsedContent.isArray()) {
                    questionsNode = parsedContent;
                } else {
                    log.warn("Groq response missing 'questions' array in book generator");
                    return null;
                }
            }

            long timestamp = System.currentTimeMillis();
            int index = 0;
            String subjectId = book != null ? book.getSubjectId() : "general";

            for (JsonNode qNode : questionsNode) {
                index++;
                String qId = "groq-bk-" + timestamp + "-" + index;
                String text = qNode.path("text").asText("Book Question " + index);
                String type = qNode.path("type").asText("short_answer").toLowerCase();
                int marks = qNode.has("marks") ? qNode.path("marks").asInt(2) : 2;
                int chNum = qNode.has("chapterNumber") ? qNode.path("chapterNumber").asInt(1) : 1;
                
                // If user selected specific chapters and this chapter wasn't one of them, map to first requested
                if (requestedChapters != null && !requestedChapters.isEmpty() && !requestedChapters.contains(chNum)) {
                    chNum = requestedChapters.get(0);
                }

                String diff = qNode.path("difficulty").asText(marks >= 5 ? "hard" : (marks >= 3 ? "medium" : "easy"));
                String explanation = qNode.path("explanation").asText("");
                String correctAnswer = qNode.path("correctAnswerText").asText("");
                String rubric = qNode.path("markingRubric").asText("");

                QuestionEntity q = new QuestionEntity();
                q.setId(qId);
                q.setText(text);
                q.setType(type);
                q.setSubjectId(subjectId);
                q.setChapterId("ch-" + subjectId + "-" + chNum);
                q.setChapterNumber(chNum);
                q.setMarks(marks);
                q.setDifficulty(diff);
                q.setExplanation(explanation);
                q.setCorrectAnswerText(correctAnswer);
                q.setMarkingRubric(rubric);
                q.setSource("book_groq_ai");
                q.setApproved(false);

                JsonNode optionsNode = qNode.path("options");
                if (optionsNode.isArray()) {
                    int optIndex = 1;
                    for (JsonNode optNode : optionsNode) {
                        String optText = optNode.path("text").asText("");
                        boolean isCorrect = optNode.path("isCorrect").asBoolean(false);
                        if (!optText.isBlank()) {
                            QuestionOptionEntity opt = new QuestionOptionEntity("opt-" + qId + "-" + optIndex++, optText, isCorrect);
                            q.addOption(opt);
                        }
                    }
                }

                if ("mcq".equals(type) && q.getOptions().isEmpty()) {
                    q.addOption(new QuestionOptionEntity("opt-" + qId + "-1", "Core principle stated in textbook", true));
                    q.addOption(new QuestionOptionEntity("opt-" + qId + "-2", "Secondary condition", false));
                    q.addOption(new QuestionOptionEntity("opt-" + qId + "-3", "Inverse proportion rule", false));
                    q.addOption(new QuestionOptionEntity("opt-" + qId + "-4", "None of the above", false));
                }

                list.add(q);
            }

            log.info("Successfully parsed {} questions from Groq Book AI generation", list.size());
            return list;
        } catch (Exception e) {
            log.error("Failed to parse Groq Book response JSON: {}", e.getMessage(), e);
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

            JsonNode parsedContent = objectMapper.readTree(content);
            JsonNode questionsNode = parsedContent.path("questions");
            if (!questionsNode.isArray()) {
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

    private String maskApiKey(String key) {
        if (key == null || key.isBlank()) return "";
        String trimmed = key.trim();
        if (trimmed.length() <= 8) return "••••••••";
        return trimmed.substring(0, 4) + "••••••••" + trimmed.substring(trimmed.length() - 4);
    }
}
