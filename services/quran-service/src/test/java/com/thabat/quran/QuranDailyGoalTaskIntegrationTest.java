package com.thabat.quran;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thabat.quran.goal.QuranDailyGoalRepository;
import com.thabat.quran.page.QuranPageProgress;
import com.thabat.quran.page.QuranPageProgressRepository;
import com.thabat.quran.page.QuranPageStatus;
import com.thabat.quran.task.QuranDailyTaskRepository;
import com.thabat.quran.task.QuranTaskStatus;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuranDailyGoalTaskIntegrationTest {

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

    private UUID userId;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        goalRepository.deleteAll();
        pageRepository.deleteAll();
        userId = UUID.randomUUID();
    }

    @Test
    void createDailyGoal_andUpdateSameActiveGoal() throws Exception {
        mockMvc.perform(put("/api/v1/quran/goals/daily")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "memorizationPagesPerDay", 1,
                                "revisionPagesPerDay", 3,
                                "preferredStartPage", 20
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memorizationPagesPerDay").value(1))
                .andExpect(jsonPath("$.revisionPagesPerDay").value(3))
                .andExpect(jsonPath("$.preferredStartPage").value(20))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(put("/api/v1/quran/goals/daily")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "memorizationPagesPerDay", 2,
                                "revisionPagesPerDay", 1
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memorizationPagesPerDay").value(2))
                .andExpect(jsonPath("$.revisionPagesPerDay").value(1));

        assertThat(goalRepository.count()).isEqualTo(1);

        mockMvc.perform(get("/api/v1/quran/goals/daily")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memorizationPagesPerDay").value(2));
    }

    @Test
    void rejectTwoZeroTargets_andInvalidPageRanges() throws Exception {
        mockMvc.perform(put("/api/v1/quran/goals/daily")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "memorizationPagesPerDay", 0,
                                "revisionPagesPerDay", 0
                        ))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/v1/quran/goals/daily")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "memorizationPagesPerDay", 1,
                                "revisionPagesPerDay", 0,
                                "preferredStartPage", 0
                        ))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/v1/quran/goals/daily")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "memorizationPagesPerDay", 1,
                                "revisionPagesPerDay", 0,
                                "preferredStartPage", 605
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getActiveGoal_missing_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/quran/goals/daily")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void generateTasksOncePerDay_selectsNotStarted_andPrioritizesNeedsRevision() throws Exception {
        saveGoal(1, 2, 5);
        seedPage(5, QuranPageStatus.NOT_STARTED, null, 0);
        seedPage(6, QuranPageStatus.LEARNING, null, 0);
        seedPage(1, QuranPageStatus.NEEDS_REVISION, null, 0);
        seedPage(2, QuranPageStatus.MEMORIZED, LocalDate.of(2026, 1, 1), 1);
        seedPage(3, QuranPageStatus.STRONG, LocalDate.of(2026, 1, 2), 2);

        mockMvc.perform(get("/api/v1/quran/tasks/today")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memorizationTasks", hasSize(1)))
                .andExpect(jsonPath("$.memorizationTasks[0].pageNumber").value(5))
                .andExpect(jsonPath("$.revisionTasks", hasSize(2)))
                .andExpect(jsonPath("$.revisionTasks[0].pageNumber").value(1))
                .andExpect(jsonPath("$.revisionTasks[1].pageNumber").value(2))
                .andExpect(jsonPath("$.totalTasks").value(3))
                .andExpect(jsonPath("$.pendingTasks").value(3))
                .andExpect(jsonPath("$.completedTasks").value(0))
                .andExpect(jsonPath("$.completionPercentage").value(0.00));

        long countAfterFirst = taskRepository.count();

        mockMvc.perform(get("/api/v1/quran/tasks/today")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(3));

        assertThat(taskRepository.count()).isEqualTo(countAfterFirst);
    }

    @Test
    void generate_avoidsDuplicatePagesAcrossTaskTypes() throws Exception {
        saveGoal(1, 2, 1);
        seedPage(1, QuranPageStatus.LEARNING, null, 0);
        seedPage(2, QuranPageStatus.NEEDS_REVISION, null, 0);
        seedPage(3, QuranPageStatus.MEMORIZED, null, 0);

        MvcResult result = mockMvc.perform(get("/api/v1/quran/tasks/today")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        int memoPage = body.get("memorizationTasks").get(0).get("pageNumber").asInt();
        for (JsonNode rev : body.get("revisionTasks")) {
            assertThat(rev.get("pageNumber").asInt()).isNotEqualTo(memoPage);
        }
    }

    @Test
    void completeMemorization_updatesPageProgress_andSummaryPercentage() throws Exception {
        saveGoal(1, 0, 10);
        seedPage(10, QuranPageStatus.LEARNING, null, 0);

        MvcResult today = mockMvc.perform(get("/api/v1/quran/tasks/today")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memorizationTasks", hasSize(1)))
                .andReturn();

        String taskId = objectMapper.readTree(today.getResponse().getContentAsString())
                .get("memorizationTasks").get(0).get("id").asText();

        mockMvc.perform(post("/api/v1/quran/tasks/" + taskId + "/complete")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "confidenceLevel", 4,
                                "successful", true
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.task.status").value("COMPLETED"))
                .andExpect(jsonPath("$.today.completedTasks").value(1))
                .andExpect(jsonPath("$.today.totalTasks").value(1))
                .andExpect(jsonPath("$.today.completionPercentage").value(100.00));

        QuranPageProgress page = pageRepository.findByUserIdAndPageNumber(userId, 10).orElseThrow();
        assertThat(page.getStatus()).isEqualTo(QuranPageStatus.MEMORIZED);
        assertThat(page.getMemorizedAt()).isNotNull();
        assertThat(page.getConfidenceLevel()).isEqualTo(4);
    }

    @Test
    void successfulRevision_incrementsReviewCount_unsuccessful_doesNot() throws Exception {
        saveGoal(0, 2, null);
        seedPage(1, QuranPageStatus.MEMORIZED, LocalDate.of(2026, 1, 1), 1);
        seedPage(2, QuranPageStatus.MEMORIZED, LocalDate.of(2026, 1, 2), 0);

        MvcResult today = mockMvc.perform(get("/api/v1/quran/tasks/today")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revisionTasks", hasSize(2)))
                .andReturn();

        JsonNode revisions = objectMapper.readTree(today.getResponse().getContentAsString())
                .get("revisionTasks");
        String successfulTaskId = revisions.get(0).get("id").asText();
        int successfulPage = revisions.get(0).get("pageNumber").asInt();
        String unsuccessfulTaskId = revisions.get(1).get("id").asText();
        int unsuccessfulPage = revisions.get(1).get("pageNumber").asInt();

        int beforeSuccessful = pageRepository.findByUserIdAndPageNumber(userId, successfulPage)
                .orElseThrow()
                .getSuccessfulReviewCount();
        int beforeUnsuccessful = pageRepository.findByUserIdAndPageNumber(userId, unsuccessfulPage)
                .orElseThrow()
                .getSuccessfulReviewCount();

        mockMvc.perform(post("/api/v1/quran/tasks/" + successfulTaskId + "/complete")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("successful", true))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/quran/tasks/" + unsuccessfulTaskId + "/complete")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("successful", false))))
                .andExpect(status().isOk());

        assertThat(pageRepository.findByUserIdAndPageNumber(userId, successfulPage)
                .orElseThrow()
                .getSuccessfulReviewCount()).isEqualTo(beforeSuccessful + 1);

        QuranPageProgress unsuccessful = pageRepository
                .findByUserIdAndPageNumber(userId, unsuccessfulPage)
                .orElseThrow();
        assertThat(unsuccessful.getSuccessfulReviewCount()).isEqualTo(beforeUnsuccessful);
        assertThat(unsuccessful.getStatus()).isEqualTo(QuranPageStatus.NEEDS_REVISION);
    }

    @Test
    void skip_doesNotModifyPageStatus() throws Exception {
        saveGoal(1, 0, 8);
        seedPage(8, QuranPageStatus.LEARNING, null, 0);

        MvcResult today = mockMvc.perform(get("/api/v1/quran/tasks/today")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andReturn();

        String taskId = objectMapper.readTree(today.getResponse().getContentAsString())
                .get("memorizationTasks").get(0).get("id").asText();

        mockMvc.perform(post("/api/v1/quran/tasks/" + taskId + "/skip")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.task.status").value("SKIPPED"))
                .andExpect(jsonPath("$.today.skippedTasks").value(1));

        assertThat(pageRepository.findByUserIdAndPageNumber(userId, 8).orElseThrow().getStatus())
                .isEqualTo(QuranPageStatus.LEARNING);
        assertThat(taskRepository.findById(UUID.fromString(taskId)).orElseThrow().getStatus())
                .isEqualTo(QuranTaskStatus.SKIPPED);
    }

    @Test
    void userCannotAccessAnotherUsersTask_andMissingJwtReturns401() throws Exception {
        UUID otherUser = UUID.randomUUID();
        saveGoalFor(otherUser, 1, 0, 1);
        seedPageFor(otherUser, 1, QuranPageStatus.NOT_STARTED, null, 0);

        MvcResult today = mockMvc.perform(get("/api/v1/quran/tasks/today")
                        .header("Authorization", "Bearer " + tokenFor(otherUser)))
                .andExpect(status().isOk())
                .andReturn();

        String taskId = objectMapper.readTree(today.getResponse().getContentAsString())
                .get("memorizationTasks").get(0).get("id").asText();

        mockMvc.perform(post("/api/v1/quran/tasks/" + taskId + "/complete")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/quran/tasks/today"))
                .andExpect(status().isUnauthorized());
    }

    private void saveGoal(int mem, int rev, Integer preferred) throws Exception {
        saveGoalFor(userId, mem, rev, preferred);
    }

    private void saveGoalFor(UUID owner, int mem, int rev, Integer preferred) throws Exception {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("memorizationPagesPerDay", mem);
        body.put("revisionPagesPerDay", rev);
        if (preferred != null) {
            body.put("preferredStartPage", preferred);
        }
        mockMvc.perform(put("/api/v1/quran/goals/daily")
                        .header("Authorization", "Bearer " + tokenFor(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    private void seedPage(
            int pageNumber,
            QuranPageStatus status,
            LocalDate lastReviewedAt,
            int successfulReviews
    ) {
        seedPageFor(userId, pageNumber, status, lastReviewedAt, successfulReviews);
    }

    private void seedPageFor(
            UUID owner,
            int pageNumber,
            QuranPageStatus status,
            LocalDate lastReviewedAt,
            int successfulReviews
    ) {
        QuranPageProgress page = new QuranPageProgress();
        page.setUserId(owner);
        page.setPageNumber(pageNumber);
        page.setStatus(status);
        page.setLastReviewedAt(lastReviewedAt);
        page.setSuccessfulReviewCount(successfulReviews);
        if (status == QuranPageStatus.MEMORIZED
                || status == QuranPageStatus.NEEDS_REVISION
                || status == QuranPageStatus.STRONG) {
            page.setMemorizedAt(LocalDate.now());
        }
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
