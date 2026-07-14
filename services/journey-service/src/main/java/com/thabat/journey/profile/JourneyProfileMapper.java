package com.thabat.journey.profile;

import com.thabat.journey.profile.dto.JourneyProfileResponse;
import com.thabat.journey.profile.dto.OnboardingRequest;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.UUID;

@Component
public class JourneyProfileMapper {

    public JourneyProfile toNewProfile(UUID userId, OnboardingRequest request) {
        return JourneyProfile.builder()
                .userId(userId)
                .experienceMode(request.experienceMode())
                .selectedGoals(new LinkedHashSet<>(request.selectedGoals()))
                .prayerLevel(request.prayerLevel())
                .quranLevel(request.quranLevel())
                .reminderPreference(request.reminderPreference())
                .onboardingCompleted(true)
                .build();
    }

    public void applyOnboarding(JourneyProfile profile, OnboardingRequest request) {
        profile.setExperienceMode(request.experienceMode());
        profile.setSelectedGoals(new LinkedHashSet<>(request.selectedGoals()));
        profile.setPrayerLevel(request.prayerLevel());
        profile.setQuranLevel(request.quranLevel());
        profile.setReminderPreference(request.reminderPreference());
        profile.setOnboardingCompleted(true);
    }

    public JourneyProfileResponse toResponse(JourneyProfile profile) {
        return new JourneyProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getExperienceMode(),
                new LinkedHashSet<>(profile.getSelectedGoals()),
                profile.getPrayerLevel(),
                profile.getQuranLevel(),
                profile.getReminderPreference(),
                profile.isOnboardingCompleted(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
