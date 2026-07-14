package com.thabat.quran;

import com.thabat.quran.page.QuranPageProgressRepository;
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
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuranAggregateIntegrationTest {

    private static final String SECRET =
            "test-secret-key-must-be-at-least-32-bytes-long!!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuranPageProgressRepository repository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        userId = UUID.randomUUID();
    }

    @Test
    void listSurahs_returnsMetadataAndAggregates() throws Exception {
        mockMvc.perform(put("/api/v1/quran/pages/1")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"MEMORIZED\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/quran/surahs")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(114)))
                .andExpect(jsonPath("$[0].surahNumber").value(1))
                .andExpect(jsonPath("$[0].nameArabic").isNotEmpty())
                .andExpect(jsonPath("$[0].nameEnglish").isNotEmpty())
                .andExpect(jsonPath("$[0].transliteration").value("Al-Fatihah"))
                .andExpect(jsonPath("$[0].revelationType").value("MECCAN"))
                .andExpect(jsonPath("$[0].totalPages").value(1))
                .andExpect(jsonPath("$[0].completedPages").value(1))
                .andExpect(jsonPath("$[0].memorizedPages").value(1))
                .andExpect(jsonPath("$[0].notStartedPages").value(0))
                .andExpect(jsonPath("$[0].completionPercentage").value(100.0));
    }

    @Test
    void surahDetail_includesAyahRangesAndStatuses() throws Exception {
        mockMvc.perform(put("/api/v1/quran/pages/2")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"MEMORIZED\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/quran/surahs/2")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectionType").value("SURAH"))
                .andExpect(jsonPath("$.sectionNumber").value(2))
                .andExpect(jsonPath("$.transliteration").value("Al-Baqarah"))
                .andExpect(jsonPath("$.revelationType").value("MEDINAN"))
                .andExpect(jsonPath("$.pages[0].pageNumber").value(2))
                .andExpect(jsonPath("$.pages[0].startAyah").value(1))
                .andExpect(jsonPath("$.pages[0].endAyah").value(5))
                .andExpect(jsonPath("$.pages[0].status").value("MEMORIZED"));
    }

    @Test
    void listJuzAndHizbs_returnReferenceCounts() throws Exception {
        mockMvc.perform(get("/api/v1/quran/juz")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(30)))
                .andExpect(jsonPath("$[0].juzNumber").value(1))
                .andExpect(jsonPath("$[0].totalPages").value(21))
                .andExpect(jsonPath("$[0].notStartedPages").value(21))
                .andExpect(jsonPath("$[0].completedPages").value(0));

        mockMvc.perform(get("/api/v1/quran/hizbs")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(60)))
                .andExpect(jsonPath("$[0].hizbNumber").value(1))
                .andExpect(jsonPath("$[0].totalPages").value(10));
    }

    @Test
    void sectionDetail_listsIncludedPagesAndStatuses() throws Exception {
        mockMvc.perform(put("/api/v1/quran/pages/1")
                        .header("Authorization", "Bearer " + tokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"LEARNING\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/quran/juz/1")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectionType").value("JUZ"))
                .andExpect(jsonPath("$.sectionNumber").value(1))
                .andExpect(jsonPath("$.totalPages").value(21))
                .andExpect(jsonPath("$.learningPages").value(1))
                .andExpect(jsonPath("$.pages", hasSize(21)))
                .andExpect(jsonPath("$.pages[0].pageNumber").value(1))
                .andExpect(jsonPath("$.pages[0].status").value("LEARNING"))
                .andExpect(jsonPath("$.pages[1].status").value("NOT_STARTED"));
    }

    @Test
    void invalidSectionNumbers_returnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/quran/surahs/0")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/quran/juz/31")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/quran/hizbs/61")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingJwt_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/quran/surahs"))
                .andExpect(status().isUnauthorized());
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
