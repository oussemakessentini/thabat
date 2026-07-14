package com.thabat.prayer.assessment;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PrayerAssessmentRepository extends JpaRepository<PrayerAssessment, UUID> {

    Optional<PrayerAssessment> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<PrayerAssessment> findByIdAndUserId(UUID id, UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a FROM PrayerAssessment a
            WHERE a.id = :id AND a.userId = :userId
            """)
    Optional<PrayerAssessment> findByIdAndUserIdForUpdate(
            @Param("id") UUID id,
            @Param("userId") UUID userId
    );
}
