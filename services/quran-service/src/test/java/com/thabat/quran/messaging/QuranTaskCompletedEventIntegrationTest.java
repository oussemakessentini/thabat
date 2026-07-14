package com.thabat.quran.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thabat.quran.goal.QuranDailyGoalRepository;
import com.thabat.quran.page.QuranPageProgress;
import com.thabat.quran.page.QuranPageProgressRepository;
import com.thabat.quran.page.QuranPageStatus;
import com.thabat.quran.task.QuranDailyTaskRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@RecordApplicationEvents
class QuranTaskCompletedEventIntegrationTest {

    private static final String SECRET =
            "test-secret-key-must-be-at-least-32-bytes-long!!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuranDailyGoalRepository goalRepository;

    @Autowired
    private QuranDailyTaskRepository taskRepository;

    @Autowired
    private QuranPageProgressRepository pageRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    private UUID userId;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        goalRepository.deleteAll();
        pageRepository.deleteAll();
        userId = UUID.randomUUID();
        applicationEvents.clear();
    }

    @Test
    void completingTask_emitsOneDomainEvent() throws Exception {
        seedGoalAndPendingTask();

        MvcResult today = mockMvc.perform(get("/api/v1/quran/tasks/today")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andReturn();

        String taskId = objectMapper.readTree(today.getResponse().getContentAsString())
                .get("memorizationTasks").get(0).get("id").asText();

        applicationEvents.clear();

        mockMvc.perform(post("/api/v1/quran/tasks/" + taskId + "/complete")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "confidenceLevel", 4,
                                "successful", true
                        ))))
                .andExpect(status().isOk());

        List<QuranTaskCompletedDomainEvent> events = applicationEvents.stream(
                QuranTaskCompletedDomainEvent.class
        ).toList();

        assertThat(events).hasSize(1);
        assertThat(events.getFirst().userId()).isEqualTo(userId);
        assertThat(events.getFirst().pageNumber()).isEqualTo(10);
        assertThat(events.getFirst().taskType()).isEqualTo("MEMORIZATION");
    }

    @Test
    void alreadyCompletedTask_doesNotEmitAnotherEvent() throws Exception {
        seedGoalAndPendingTask();

        MvcResult today = mockMvc.perform(get("/api/v1/quran/tasks/today")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andReturn();

        String taskId = objectMapper.readTree(today.getResponse().getContentAsString())
                .get("memorizationTasks").get(0).get("id").asText();

        mockMvc.perform(post("/api/v1/quran/tasks/" + taskId + "/complete")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        applicationEvents.clear();

        mockMvc.perform(post("/api/v1/quran/tasks/" + taskId + "/complete")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        assertThat(applicationEvents.stream(QuranTaskCompletedDomainEvent.class).count())
                .isZero();
    }

    private void seedGoalAndPendingTask() throws Exception {
        mockMvc.perform(put("/api/v1/quran/goals/daily")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "memorizationPagesPerDay", 1,
                                "revisionPagesPerDay", 0,
                                "preferredStartPage", 10
                        ))))
                .andExpect(status().isOk());

        QuranPageProgress page = new QuranPageProgress();
        page.setUserId(userId);
        page.setPageNumber(10);
        page.setStatus(QuranPageStatus.LEARNING);
        page.setSuccessfulReviewCount(0);
        pageRepository.save(page);
    }

    private String tokenFor(UUID subject) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject.toString())
                .claim("email", "user@example.com")
                .claim("roles", List.of("ROLE_USER"))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(key)
                .compact();
    }
}
