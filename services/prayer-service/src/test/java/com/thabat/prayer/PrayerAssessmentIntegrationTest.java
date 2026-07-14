package com.thabat.prayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thabat.prayer.assessment.PrayerAssessmentRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PrayerAssessmentIntegrationTest {

    private static final String SECRET =
            "test-secret-key-must-be-at-least-32-bytes-long!!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PrayerAssessmentRepository prayerAssessmentRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        prayerAssessmentRepository.deleteAll();
        userId = UUID.randomUUID();
    }

    @Test
    void createAssessment_calculatesAndSavesWithJwtUserId() throws Exception {
        mockMvc.perform(post("/api/v1/prayers/assessments")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(2, 3, 10, 5)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalEstimatedDays").value(830))
                .andExpect(jsonPath("$.remainingByPrayer.fajr").value(830))
                .andExpect(jsonPath("$.totalRemainingPrayers").value(4150))
                .andExpect(jsonPath("$.estimatedCompletionDays").value(830))
                .andExpect(jsonPath("$.dailyRecoveryTarget").value(5));

        assertThat(prayerAssessmentRepository.findAll()).hasSize(1);
        assertThat(prayerAssessmentRepository.findAll().getFirst().getUserId())
                .isEqualTo(userId);
    }

    @Test
    void allZeroDuration_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/prayers/assessments")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(0, 0, 0, 5)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void negativeValue_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/prayers/assessments")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(-1, 0, 0, 5)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void moreThan120Years_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/prayers/assessments")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(121, 0, 0, 5)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dailyTargetBelowOne_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/prayers/assessments")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(1, 0, 0, 0)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dailyTargetAbove100_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/prayers/assessments")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(1, 0, 0, 101)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/prayers/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(1, 0, 0, 5)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getLatest_returnsAuthenticatedUserAssessment() throws Exception {
        mockMvc.perform(post("/api/v1/prayers/assessments")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(1, 0, 0, 5)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/prayers/assessments/latest")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missedYears").value(1));
    }

    @Test
    void getLatest_isScopedToAuthenticatedUser() throws Exception {
        mockMvc.perform(post("/api/v1/prayers/assessments")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(1, 0, 0, 5)))
                .andExpect(status().isCreated());

        UUID otherUser = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/prayers/assessments/latest")
                        .header("Authorization", "Bearer " + tokenFor(otherUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLatest_missing_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/prayers/assessments/latest")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void completionDays_usesCeiling() throws Exception {
        mockMvc.perform(post("/api/v1/prayers/assessments")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(0, 0, 1, 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalRemainingPrayers").value(5))
                .andExpect(jsonPath("$.estimatedCompletionDays").value(3));
    }

    private String validBody(int years, int months, int days, int target) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "missedYears", years,
                "missedMonths", months,
                "missedDays", days,
                "dailyRecoveryTarget", target
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
