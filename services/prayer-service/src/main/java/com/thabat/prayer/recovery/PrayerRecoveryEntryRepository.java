package com.thabat.prayer.recovery;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PrayerRecoveryEntryRepository extends JpaRepository<PrayerRecoveryEntry, UUID> {

    long countByUserIdAndAssessmentId(UUID userId, UUID assessmentId);

    List<PrayerRecoveryEntry> findByUserIdAndAssessmentIdOrderBySequenceNumberDesc(
            UUID userId,
            UUID assessmentId
    );

    Optional<PrayerRecoveryEntry> findFirstByUserIdAndAssessmentIdOrderBySequenceNumberDesc(
            UUID userId,
            UUID assessmentId
    );
}
