package com.thabat.journey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thabat.journey.profile.ExperienceMode;
import com.thabat.journey.profile.JourneyGoal;
import com.thabat.journey.profile.JourneyProfileRepository;
import com.thabat.journey.profile.PrayerLevel;
import com.thabat.journey.profile.QuranLevel;
import com.thabat.journey.profile.ReminderPreference;
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
class JourneyOnboardingIntegrationTest {

    private static final String SECRET =
            "test-secret-key-must-be-at-least-32-bytes-long!!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JourneyProfileRepository journeyProfileRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        journeyProfileRepository.deleteAll();
        userId = UUID.randomUUID();
    }

    @Test
    void createOnboardingProfile_returnsCreated() throws Exception {
        mockMvc.perform(post("/api/v1/journey/onboarding")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.onboardingCompleted").value(true))
                .andExpect(jsonPath("$.experienceMode").value("ADULT"));

        assertThat(journeyProfileRepository.findByUserId(userId)).isPresent();
    }

    @Test
    void updateOnboardingProfile_isIdempotent() throws Exception {
        mockMvc.perform(post("/api/v1/journey/onboarding")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isCreated());

        String updateBody = objectMapper.writeValueAsString(Map.of(
                "experienceMode", ExperienceMode.KIDS.name(),
                "selectedGoals", List.of(JourneyGoal.DHIKR.name()),
                "prayerLevel", PrayerLevel.SOMETIMES.name(),
                "quranLevel", QuranLevel.BEGINNER.name(),
                "reminderPreference", ReminderPreference.MORNING.name()
        ));

        mockMvc.perform(post("/api/v1/journey/onboarding")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.experienceMode").value("KIDS"))
                .andExpect(jsonPath("$.selectedGoals[0]").value("DHIKR"));

        assertThat(journeyProfileRepository.findAll()).hasSize(1);
    }

    @Test
    void missingToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/journey/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void emptySelectedGoals_returnsBadRequest() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "experienceMode", ExperienceMode.ADULT.name(),
                "selectedGoals", List.of(),
                "prayerLevel", PrayerLevel.MOST_PRAYERS.name(),
                "quranLevel", QuranLevel.INTERMEDIATE.name(),
                "reminderPreference", ReminderPreference.EVENING.name()
        ));

        mockMvc.perform(post("/api/v1/journey/onboarding")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProfile_returnsAuthenticatedUserProfile() throws Exception {
        mockMvc.perform(post("/api/v1/journey/onboarding")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/journey/profile")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    void getProfile_cannotAccessAnotherUser() throws Exception {
        mockMvc.perform(post("/api/v1/journey/onboarding")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isCreated());

        UUID otherUser = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/journey/profile")
                        .header("Authorization", "Bearer " + tokenFor(otherUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProfile_missingProfile_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/journey/profile")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isNotFound());
    }

    private String validBody() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "experienceMode", ExperienceMode.ADULT.name(),
                "selectedGoals", List.of(
                        JourneyGoal.PRAYER_CONSISTENCY.name(),
                        JourneyGoal.QURAN_MEMORIZATION.name()
                ),
                "prayerLevel", PrayerLevel.MOST_PRAYERS.name(),
                "quranLevel", QuranLevel.INTERMEDIATE.name(),
                "reminderPreference", ReminderPreference.EVENING.name()
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
