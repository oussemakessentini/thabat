package com.thabat.prayer.assessment;

import com.thabat.prayer.assessment.dto.CreatePrayerAssessmentRequest;
import com.thabat.prayer.assessment.dto.PrayerAssessmentResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PrayerAssessmentMapper {

    public PrayerAssessment toEntity(
            UUID userId,
            CreatePrayerAssessmentRequest request,
            MissedPrayerCalculator.AssessmentResult result
    ) {
        return PrayerAssessment.builder()
                .userId(userId)
                .missedYears(request.missedYears())
                .missedMonths(request.missedMonths())
                .missedDays(request.missedDays())
                .totalEstimatedDays(result.totalEstimatedDays())
                .fajrRemaining(result.fajrRemaining())
                .dhuhrRemaining(result.dhuhrRemaining())
                .asrRemaining(result.asrRemaining())
                .maghribRemaining(result.maghribRemaining())
                .ishaRemaining(result.ishaRemaining())
                .totalRemainingPrayers(result.totalRemainingPrayers())
                .dailyRecoveryTarget(request.dailyRecoveryTarget())
                .build();
    }

    public PrayerAssessmentResponse toResponse(PrayerAssessment assessment) {
        long estimatedCompletionDays = MissedPrayerCalculator.ceilingDivide(
                assessment.getTotalRemainingPrayers(),
                assessment.getDailyRecoveryTarget()
        );

        return new PrayerAssessmentResponse(
                assessment.getId(),
                assessment.getMissedYears(),
                assessment.getMissedMonths(),
                assessment.getMissedDays(),
                assessment.getTotalEstimatedDays(),
                new PrayerAssessmentResponse.RemainingByPrayer(
                        assessment.getFajrRemaining(),
                        assessment.getDhuhrRemaining(),
                        assessment.getAsrRemaining(),
                        assessment.getMaghribRemaining(),
                        assessment.getIshaRemaining()
                ),
                assessment.getTotalRemainingPrayers(),
                assessment.getDailyRecoveryTarget(),
                estimatedCompletionDays,
                assessment.getCreatedAt()
        );
    }
}
