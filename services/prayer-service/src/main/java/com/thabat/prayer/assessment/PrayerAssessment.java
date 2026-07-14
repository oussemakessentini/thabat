package com.thabat.prayer.assessment;

import com.thabat.prayer.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "prayer_assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrayerAssessment extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "missed_years", nullable = false)
    private int missedYears;

    @Column(name = "missed_months", nullable = false)
    private int missedMonths;

    @Column(name = "missed_days", nullable = false)
    private int missedDays;

    @Column(name = "total_estimated_days", nullable = false)
    private long totalEstimatedDays;

    @Column(name = "fajr_remaining", nullable = false)
    private long fajrRemaining;

    @Column(name = "dhuhr_remaining", nullable = false)
    private long dhuhrRemaining;

    @Column(name = "asr_remaining", nullable = false)
    private long asrRemaining;

    @Column(name = "maghrib_remaining", nullable = false)
    private long maghribRemaining;

    @Column(name = "isha_remaining", nullable = false)
    private long ishaRemaining;

    @Column(name = "total_remaining_prayers", nullable = false)
    private long totalRemainingPrayers;

    @Column(name = "daily_recovery_target", nullable = false)
    private int dailyRecoveryTarget;
}
