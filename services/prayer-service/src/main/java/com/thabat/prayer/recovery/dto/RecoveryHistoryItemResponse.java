package com.thabat.prayer.recovery.dto;

import com.thabat.prayer.recovery.PrayerType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RecoveryHistoryItemResponse(
        UUID id,
        PrayerType prayerType,
        long sequenceNumber,
        LocalDate completedOn,
        Instant createdAt
) {
}
