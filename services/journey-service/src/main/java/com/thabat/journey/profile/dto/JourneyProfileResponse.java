package com.thabat.journey.profile.dto;

import com.thabat.journey.profile.ExperienceMode;
import com.thabat.journey.profile.JourneyGoal;
import com.thabat.journey.profile.PrayerLevel;
import com.thabat.journey.profile.QuranLevel;
import com.thabat.journey.profile.ReminderPreference;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record JourneyProfileResponse(
        UUID id,
        UUID userId,
        ExperienceMode experienceMode,
        Set<JourneyGoal> selectedGoals,
        PrayerLevel prayerLevel,
        QuranLevel quranLevel,
        ReminderPreference reminderPreference,
        boolean onboardingCompleted,
        Instant createdAt,
        Instant updatedAt
) {
}
