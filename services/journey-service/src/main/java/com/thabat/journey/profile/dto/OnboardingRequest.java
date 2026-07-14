package com.thabat.journey.profile.dto;

import com.thabat.journey.profile.ExperienceMode;
import com.thabat.journey.profile.JourneyGoal;
import com.thabat.journey.profile.PrayerLevel;
import com.thabat.journey.profile.QuranLevel;
import com.thabat.journey.profile.ReminderPreference;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record OnboardingRequest(

        @NotNull(message = "Experience mode is required")
        ExperienceMode experienceMode,

        @NotEmpty(message = "Select at least one goal")
        Set<@NotNull JourneyGoal> selectedGoals,

        @NotNull(message = "Prayer level is required")
        PrayerLevel prayerLevel,

        @NotNull(message = "Quran level is required")
        QuranLevel quranLevel,

        @NotNull(message = "Reminder preference is required")
        ReminderPreference reminderPreference
) {
}
