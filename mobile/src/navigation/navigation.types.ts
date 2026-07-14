import type { NativeStackScreenProps } from "@react-navigation/native-stack";

import type { PrayerAssessment } from "../features/prayer/types/prayer.types";

export type AuthStackParamList = {
    Splash: undefined;
    Login: { email?: string } | undefined;
    Register: undefined;
};

export type OnboardingStackParamList = {
    WelcomeOnboarding: undefined;
    GoalsOnboarding: undefined;
    LevelsOnboarding: undefined;
    PreferencesOnboarding: undefined;
};

export type AppStackParamList = {
    Home: undefined;
    Notifications: undefined;
    PrayerAssessment: undefined;
    PrayerAssessmentResult: {
        assessment: PrayerAssessment;
    };
    PrayerProgress: undefined;
    RecoveryHistory: undefined;
    QuranTracker: undefined;
    QuranDailyGoal: undefined;
    QuranToday: undefined;
    QuranPageDetail: {
        pageNumber: number;
    };
    QuranSectionDetail: {
        sectionType: "SURAH" | "JUZ" | "HIZB";
        sectionNumber: number;
    };
};

export type AuthStackScreenProps<T extends keyof AuthStackParamList> =
    NativeStackScreenProps<AuthStackParamList, T>;

export type OnboardingStackScreenProps<T extends keyof OnboardingStackParamList> =
    NativeStackScreenProps<OnboardingStackParamList, T>;

export type AppStackScreenProps<T extends keyof AppStackParamList> =
    NativeStackScreenProps<AppStackParamList, T>;

/** @deprecated Prefer AuthStackParamList / AppStackParamList */
export type RootStackParamList = AuthStackParamList;

export type RootStackScreenProps<T extends keyof RootStackParamList> =
    AuthStackScreenProps<T>;
