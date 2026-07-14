package com.thabat.notification;

import com.thabat.notification.domain.Notification;
import com.thabat.notification.domain.NotificationRepository;
import com.thabat.notification.domain.NotificationType;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationRestIntegrationTest {

    private static final String SECRET =
            "test-secret-key-must-be-at-least-32-bytes-long!!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        userId = UUID.randomUUID();
    }

    @Test
    void missingJwt_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void markRead_forAnotherUsersNotification_returnsNotFound() throws Exception {
        UUID otherUser = UUID.randomUUID();
        Notification ownedByOther = saveNotification(otherUser, UUID.randomUUID(), false);

        mockMvc.perform(patch("/api/v1/notifications/" + ownedByOther.getId() + "/read")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void markRead_isIdempotent() throws Exception {
        Notification notification = saveNotification(userId, UUID.randomUUID(), false);

        mockMvc.perform(patch("/api/v1/notifications/" + notification.getId() + "/read")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));

        mockMvc.perform(patch("/api/v1/notifications/" + notification.getId() + "/read")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true))
                .andExpect(jsonPath("$.id").value(notification.getId().toString()));
    }

    @Test
    void unreadCount_andList_reflectUserIsolation() throws Exception {
        saveNotification(userId, UUID.randomUUID(), false);
        saveNotification(userId, UUID.randomUUID(), true);
        saveNotification(UUID.randomUUID(), UUID.randomUUID(), false);

        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(1));

        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void markAllRead_clearsUnreadCount() throws Exception {
        saveNotification(userId, UUID.randomUUID(), false);
        saveNotification(userId, UUID.randomUUID(), false);

        mockMvc.perform(patch("/api/v1/notifications/read-all")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header("Authorization", "Bearer " + tokenFor(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(0));
    }

    private Notification saveNotification(UUID ownerId, UUID eventId, boolean read) {
        Notification notification = Notification.builder()
                .userId(ownerId)
                .eventId(eventId)
                .type(NotificationType.QURAN_TASK_COMPLETED)
                .title("Quran task completed")
                .message("You completed page 1. Keep going at your own pace.")
                .read(read)
                .readAt(read ? Instant.now() : null)
                .build();
        return notificationRepository.saveAndFlush(notification);
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
