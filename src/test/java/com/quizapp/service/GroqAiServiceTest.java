package com.quizapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizapp.dto.AIQuestionGenRequest;
import com.quizapp.entity.ChapterEntity;
import com.quizapp.entity.QuestionEntity;
import com.quizapp.entity.SubjectEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GroqAiServiceTest {

    @Test
    @DisplayName("Groq is not available when key is blank")
    public void testIsAvailableWithBlankKey() {
        GroqAiService service = new GroqAiService(new ObjectMapper());
        ReflectionTestUtils.setField(service, "enabled", true);
        ReflectionTestUtils.setField(service, "apiKey", "");
        assertFalse(service.isAvailable());
    }

    @Test
    @DisplayName("Groq is not available when enabled is false")
    public void testIsAvailableWhenDisabled() {
        GroqAiService service = new GroqAiService(new ObjectMapper());
        ReflectionTestUtils.setField(service, "enabled", false);
        ReflectionTestUtils.setField(service, "apiKey", "gsk_test");
        assertFalse(service.isAvailable());
    }

    @Test
    @DisplayName("Groq is available when enabled and key is present")
    public void testIsAvailableWhenConfigured() {
        GroqAiService service = new GroqAiService(new ObjectMapper());
        ReflectionTestUtils.setField(service, "enabled", true);
        ReflectionTestUtils.setField(service, "apiKey", "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");
        assertTrue(service.isAvailable());
    }

    @Test
    @DisplayName("Live Groq AI generation test with provided key")
    public void testLiveGroqGeneration() {
        GroqAiService service = new GroqAiService(new ObjectMapper());
        ReflectionTestUtils.setField(service, "enabled", true);
        ReflectionTestUtils.setField(service, "apiKey", "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");
        ReflectionTestUtils.setField(service, "apiUrl", "https://api.groq.com/openai/v1/chat/completions");
        ReflectionTestUtils.setField(service, "model", "openai/gpt-oss-120b");
        ReflectionTestUtils.setField(service, "timeoutSeconds", 30);

        SubjectEntity subject = new SubjectEntity("sub-phy", "Physics", "PHYS-10");
        ChapterEntity chapter = new ChapterEntity("ch-phy-3", 3, "Electricity & Circuits", List.of("Ohm's Law", "Resistivity"), "Core electrical fundamentals", null);

        AIQuestionGenRequest req = new AIQuestionGenRequest();
        req.setSubjectId("sub-phy");
        req.setChapterId("ch-phy-3");
        req.setCount(2);
        req.setMarks(2);
        req.setDifficulty("medium");
        req.setQuestionTypes(List.of("mcq", "key_in"));

        List<QuestionEntity> questions = service.generateQuestions(subject, chapter, req);
        assertNotNull(questions, "Questions list should not be null");
        assertFalse(questions.isEmpty(), "Questions list should not be empty");
        assertEquals("groq_ai", questions.get(0).getSource());
        assertNotNull(questions.get(0).getText());
        System.out.println("Generated question 1: " + questions.get(0).getText());
    }
}
