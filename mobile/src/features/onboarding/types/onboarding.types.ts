export type ExperienceMode = "ADULT" | "KIDS";

export type JourneyGoal =
    | "PRAYER_CONSISTENCY"
    | "PRAYER_RECOVERY"
    | "QURAN_READING"
    | "QURAN_MEMORIZATION"
    | "QURAN_REVISION"
    | "DHIKR"
    | "ISLAMIC_KNOWLEDGE";

export type PrayerLevel =
    | "NOT_PRAYING"
    | "SOMETIMES"
    | "MOST_PRAYERS"
    | "FIVE_DAILY_PRAYERS";

export type QuranLevel =
    | "BEGINNER"
    | "BASIC"
    | "INTERMEDIATE"
    | "ADVANCED"
    | "HAFIZ";

export type ReminderPreference = "MORNING" | "EVENING" | "BOTH" | "NONE";

export type OnboardingRequest = {
    experienceMode: ExperienceMode;
    selectedGoals: JourneyGoal[];
    prayerLevel: PrayerLevel;
    quranLevel: QuranLevel;
    reminderPreference: ReminderPreference;
};

export type JourneyProfile = {
    id: string;
    userId: string;
    experienceMode: ExperienceMode;
    selectedGoals: JourneyGoal[];
    prayerLevel: PrayerLevel;
    quranLevel: QuranLevel;
    reminderPreference: ReminderPreference;
    onboardingCompleted: boolean;
    createdAt: string;
    updatedAt: string;
};

export type OnboardingDraft = {
    experienceMode: ExperienceMode | null;
    selectedGoals: JourneyGoal[];
    prayerLevel: PrayerLevel | null;
    quranLevel: QuranLevel | null;
    reminderPreference: ReminderPreference | null;
};
