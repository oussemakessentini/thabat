package com.thabat.journey.profile;

import com.thabat.journey.common.entity.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "journey_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JourneyProfile extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_mode", nullable = false, length = 20)
    private ExperienceMode experienceMode;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "journey_profile_goals",
            joinColumns = @JoinColumn(name = "profile_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "goal", nullable = false, length = 50)
    private Set<JourneyGoal> selectedGoals = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "prayer_level", nullable = false, length = 40)
    private PrayerLevel prayerLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "quran_level", nullable = false, length = 40)
    private QuranLevel quranLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_preference", nullable = false, length = 20)
    private ReminderPreference reminderPreference;

    @Builder.Default
    @Column(name = "onboarding_completed", nullable = false)
    private boolean onboardingCompleted = false;
}
