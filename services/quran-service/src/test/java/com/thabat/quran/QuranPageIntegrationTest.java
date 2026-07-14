package com.thabat.quran;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thabat.quran.page.QuranPageProgress;
import com.thabat.quran.page.QuranPageProgressRepository;
import com.thabat.quran.page.QuranPageStatus;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuranPageIntegrationTest {

    private static final String SECRET =
            "test-secret-key-must-be-at-least-32-bytes-long!!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuranPageProgressRepository repository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        userId = UUID.randomUUID();
    }

    @Test
    void page1And604_areAccepted() throws Exception {
        mockMvc.perform(get("/api/v1/quran/pages/1")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.status").value("NOT_STARTED"));

        mockMvc.perform(get("/api/v1/quran/pages/604")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(604))
                .andExpect(jsonPath("$.status").value("NOT_STARTED"));
    }

    @Test
    void page0And605_returnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/quran/pages/0")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/quran/pages/605")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingJwt_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/quran/pages/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCannotAccessAnotherUsersPageProgress() throws Exception {
        UUID otherUser = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/quran/pages/10")
                        .header("Authorization", "Bearer " + tokenFor(otherUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "status", "MEMORIZED",
                                "confidenceLevel", 4
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/quran/pages/10")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NOT_STARTED"));

        assertThat(repository.findByUserIdAndPageNumber(userId, 10)).isEmpty();
        assertThat(repository.findByUserIdAndPageNumber(otherUser, 10)).isPresent();
    }

    @Test
    void missingPageRecord_returnsNotStarted() throws Exception {
        mockMvc.perform(get("/api/v1/quran/pages/77")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(77))
                .andExpect(jsonPath("$.status").value("NOT_STARTED"))
                .andExpect(jsonPath("$.memorizedAt").value(nullValue()))
                .andExpect(jsonPath("$.successfulReviewCount").value(0));

        assertThat(repository.count()).isZero();
    }

    @Test
    void updatingPage_createsOneRecord_andDoesNotDuplicate() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "status", "LEARNING",
                "confidenceLevel", 2,
                "notes", "Started this mushaf page"
        ));

        mockMvc.perform(put("/api/v1/quran/pages/12")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LEARNING"));

        mockMvc.perform(put("/api/v1/quran/pages/12")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "status", "MEMORIZED",
                                "confidenceLevel", 3
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MEMORIZED"))
                .andExpect(jsonPath("$.memorizedAt").isNotEmpty());

        assertThat(repository.findByUserIdOrderByPageNumberAsc(userId)).hasSize(1);
    }

    @Test
    void memorizedStatus_setsMemorizedAtWhenAbsent() throws Exception {
        mockMvc.perform(put("/api/v1/quran/pages/5")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "MEMORIZED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MEMORIZED"))
                .andExpect(jsonPath("$.memorizedAt").value(LocalDate.now().toString()));
    }

    @Test
    void futureDates_returnBadRequest() throws Exception {
        String future = LocalDate.now().plusDays(2).toString();

        mockMvc.perform(put("/api/v1/quran/pages/3")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "status", "MEMORIZED",
                                "memorizedAt", future
                        ))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/quran/pages/3/reviews")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "reviewedAt", future,
                                "successful", true
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void successfulReview_incrementsCount_unsuccessfulDoesNot() throws Exception {
        mockMvc.perform(put("/api/v1/quran/pages/20")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "MEMORIZED"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/quran/pages/20/reviews")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "reviewedAt", LocalDate.now().toString(),
                                "successful", true,
                                "confidenceLevel", 4,
                                "newStatus", "STRONG"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successfulReviewCount").value(1))
                .andExpect(jsonPath("$.status").value("STRONG"));

        mockMvc.perform(post("/api/v1/quran/pages/20/reviews")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "reviewedAt", LocalDate.now().toString(),
                                "successful", false,
                                "newStatus", "NEEDS_REVISION"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successfulReviewCount").value(1))
                .andExpect(jsonPath("$.status").value("NEEDS_REVISION"));
    }

    @Test
    void progressTotals_equal604_andPercentageRounded() throws Exception {
        seedPages(userId, QuranPageStatus.MEMORIZED, 50);
        seedPages(userId, QuranPageStatus.NEEDS_REVISION, 20);
        seedPages(userId, QuranPageStatus.STRONG, 14);
        seedPages(userId, QuranPageStatus.LEARNING, 20);

        mockMvc.perform(get("/api/v1/quran/progress")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value(604))
                .andExpect(jsonPath("$.notStartedPages").value(500))
                .andExpect(jsonPath("$.learningPages").value(20))
                .andExpect(jsonPath("$.memorizedPages").value(50))
                .andExpect(jsonPath("$.needsRevisionPages").value(20))
                .andExpect(jsonPath("$.strongPages").value(14))
                .andExpect(jsonPath("$.completedPages").value(84))
                .andExpect(jsonPath("$.completionPercentage").value(13.91));
    }

    @Test
    void listPages_returnsAll604WithoutPersistingDefaults() throws Exception {
        mockMvc.perform(put("/api/v1/quran/pages/100")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "LEARNING"))))
                .andExpect(status().isOk());

        String payload = mockMvc.perform(get("/api/v1/quran/pages")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<?> pages = objectMapper.readValue(payload, List.class);
        assertThat(pages).hasSize(604);
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void uniqueUserPageConstraint_isEnforced() {
        QuranPageProgress first = new QuranPageProgress();
        first.setUserId(userId);
        first.setPageNumber(42);
        first.setStatus(QuranPageStatus.LEARNING);
        repository.saveAndFlush(first);

        QuranPageProgress duplicate = new QuranPageProgress();
        duplicate.setUserId(userId);
        duplicate.setPageNumber(42);
        duplicate.setStatus(QuranPageStatus.MEMORIZED);

        assertThatThrownBy(() -> repository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void seedPages(UUID owner, QuranPageStatus status, int count) {
        int start = (int) repository.count() + 1;
        for (int i = 0; i < count; i++) {
            QuranPageProgress page = new QuranPageProgress();
            page.setUserId(owner);
            page.setPageNumber(start + i);
            page.setStatus(status);
            if (status == QuranPageStatus.MEMORIZED
                    || status == QuranPageStatus.NEEDS_REVISION
                    || status == QuranPageStatus.STRONG) {
                page.setMemorizedAt(LocalDate.now());
            }
            repository.save(page);
        }
        repository.flush();
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
