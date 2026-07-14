package com.thabat.prayer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thabat.prayer.assessment.PrayerAssessmentRepository;
import com.thabat.prayer.recovery.PrayerRecoveryEntryRepository;
import com.thabat.prayer.recovery.PrayerType;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PrayerRecoveryIntegrationTest {

    private static final String SECRET =
            "test-secret-key-must-be-at-least-32-bytes-long!!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PrayerAssessmentRepository assessmentRepository;

    @Autowired
    private PrayerRecoveryEntryRepository recoveryEntryRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        recoveryEntryRepository.deleteAll();
        assessmentRepository.deleteAll();
        userId = UUID.randomUUID();
    }

    @Test
    void completionsFollowFixedPrayerOrder() throws Exception {
        UUID assessmentId = createAssessment(userId, 1, 0, 0);

        assertCompletedPrayer(assessmentId, PrayerType.FAJR);
        assertCompletedPrayer(assessmentId, PrayerType.DHUHR);
        assertCompletedPrayer(assessmentId, PrayerType.ASR);
        assertCompletedPrayer(assessmentId, PrayerType.MAGHRIB);
        assertCompletedPrayer(assessmentId, PrayerType.ISHA);
        assertCompletedPrayer(assessmentId, PrayerType.FAJR);
    }

    @Test
    void futureDate_returnsBadRequest() throws Exception {
        UUID assessmentId = createAssessment(userId, 1, 0, 0);

        mockMvc.perform(post("/api/v1/prayers/recovery/complete-next")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody(assessmentId, LocalDate.now().plusDays(1).toString())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anotherUserCannotUseAssessment() throws Exception {
        UUID assessmentId = createAssessment(userId, 1, 0, 0);
        UUID other = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/prayers/recovery/complete-next")
                        .header("Authorization", "Bearer " + tokenFor(other))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody(assessmentId, LocalDate.now().toString())))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingJwt_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/prayers/recovery/complete-next")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void completionBeyondTotal_isRejected() throws Exception {
        UUID assessmentId = createAssessment(userId, 0, 0, 1); // 5 prayers total

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/prayers/recovery/complete-next")
                            .header("Authorization", "Bearer " + tokenFor(userId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(completeBody(assessmentId, LocalDate.now().toString())))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(post("/api/v1/prayers/recovery/complete-next")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody(assessmentId, LocalDate.now().toString())))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/prayers/progress")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextPrayer").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.totalCompletedPrayers").value(5))
                .andExpect(jsonPath("$.totalRemainingPrayers").value(0))
                .andExpect(jsonPath("$.currentCycle.fajr").value("COMPLETED"))
                .andExpect(jsonPath("$.currentCycle.isha").value("COMPLETED"));
    }

    @Test
    void undoLatest_removesOnlyLatestAndUpdatesNext() throws Exception {
        UUID assessmentId = createAssessment(userId, 1, 0, 0);

        mockMvc.perform(post("/api/v1/prayers/recovery/complete-next")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody(assessmentId, LocalDate.now().toString())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.completedPrayer").value("FAJR"))
                .andExpect(jsonPath("$.nextPrayer").value("DHUHR"));

        mockMvc.perform(post("/api/v1/prayers/recovery/complete-next")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody(assessmentId, LocalDate.now().toString())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.completedPrayer").value("DHUHR"))
                .andExpect(jsonPath("$.nextPrayer").value("ASR"));

        mockMvc.perform(delete("/api/v1/prayers/recovery/latest")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "assessmentId", assessmentId.toString()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextPrayer").value("DHUHR"))
                .andExpect(jsonPath("$.totalCompletedPrayers").value(1))
                .andExpect(jsonPath("$.currentCycle.fajr").value("COMPLETED"))
                .andExpect(jsonPath("$.currentCycle.dhuhr").value("NEXT"));

        assertThat(recoveryEntryRepository.countByUserIdAndAssessmentId(userId, assessmentId))
                .isEqualTo(1);
    }

    @Test
    void progress_cycleCountsAndPercentage() throws Exception {
        UUID assessmentId = createAssessment(userId, 0, 0, 2); // 10 prayers, 2 cycles

        for (int i = 0; i < 7; i++) {
            mockMvc.perform(post("/api/v1/prayers/recovery/complete-next")
                            .header("Authorization", "Bearer " + tokenFor(userId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(completeBody(assessmentId, LocalDate.now().toString())))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/v1/prayers/progress")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecoveryCycles").value(2))
                .andExpect(jsonPath("$.completedCycles").value(1))
                .andExpect(jsonPath("$.completedPrayersInCurrentCycle").value(2))
                .andExpect(jsonPath("$.currentCycleNumber").value(2))
                .andExpect(jsonPath("$.totalCompletedPrayers").value(7))
                .andExpect(jsonPath("$.totalRemainingPrayers").value(3))
                .andExpect(jsonPath("$.nextPrayer").value("ASR"))
                .andExpect(jsonPath("$.progressPercentage").value(70.0))
                .andExpect(jsonPath("$.estimatedRemainingDays").value(1));
    }

    @Test
    void concurrentComplete_doesNotCreateDuplicateSequences() throws Exception {
        UUID assessmentId = createAssessment(userId, 1, 0, 0);
        String token = tokenFor(userId);
        String body = completeBody(assessmentId, LocalDate.now().toString());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger created = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            futures.add(executor.submit(() -> {
                try {
                    ready.countDown();
                    go.await(5, TimeUnit.SECONDS);
                    MvcResult result = mockMvc.perform(post("/api/v1/prayers/recovery/complete-next")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(body))
                            .andReturn();
                    if (result.getResponse().getStatus() == 201) {
                        created.incrementAndGet();
                    }
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            }));
        }

        ready.await(5, TimeUnit.SECONDS);
        go.countDown();
        for (Future<?> future : futures) {
            future.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();

        long count = recoveryEntryRepository.countByUserIdAndAssessmentId(userId, assessmentId);
        assertThat(count).isBetween(1L, 2L);
        assertThat(created.get()).isEqualTo((int) count);

        var sequences = recoveryEntryRepository
                .findByUserIdAndAssessmentIdOrderBySequenceNumberDesc(userId, assessmentId)
                .stream()
                .map(entry -> entry.getSequenceNumber())
                .toList();
        assertThat(sequences).doesNotHaveDuplicates();
    }

    @Test
    void history_returnsNewestFirst() throws Exception {
        UUID assessmentId = createAssessment(userId, 1, 0, 0);
        complete(assessmentId);
        complete(assessmentId);

        MvcResult result = mockMvc.perform(get("/api/v1/prayers/recovery/history")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode array = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(array.get(0).get("sequenceNumber").asLong()).isEqualTo(2L);
        assertThat(array.get(1).get("sequenceNumber").asLong()).isEqualTo(1L);
    }

    private void assertCompletedPrayer(UUID assessmentId, PrayerType expected) throws Exception {
        mockMvc.perform(post("/api/v1/prayers/recovery/complete-next")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody(assessmentId, LocalDate.now().toString())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.completedPrayer").value(expected.name()));
    }

    private void complete(UUID assessmentId) throws Exception {
        mockMvc.perform(post("/api/v1/prayers/recovery/complete-next")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody(assessmentId, LocalDate.now().toString())))
                .andExpect(status().isCreated());
    }

    private UUID createAssessment(UUID owner, int years, int months, int days) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/prayers/assessments")
                        .header("Authorization", "Bearer " + tokenFor(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "missedYears", years,
                                "missedMonths", months,
                                "missedDays", days,
                                "dailyRecoveryTarget", 5
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        return UUID.fromString(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText()
        );
    }

    private String completeBody(UUID assessmentId, String completedOn) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "assessmentId", assessmentId.toString(),
                "completedOn", completedOn
        ));
    }

    private String tokenFor(UUID subject) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(subject.toString())
                .claim("email", "user@example.com")
                .claim("roles", List.of("ROLE_USER"))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(600)))
                .signWith(key)
                .compact();
    }
}
