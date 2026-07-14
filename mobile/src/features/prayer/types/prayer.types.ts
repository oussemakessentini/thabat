export type RemainingByPrayer = {
    fajr: number;
    dhuhr: number;
    asr: number;
    maghrib: number;
    isha: number;
};

export type PrayerType = "FAJR" | "DHUHR" | "ASR" | "MAGHRIB" | "ISHA";

export type CyclePrayerStatus = "COMPLETED" | "NEXT" | "LOCKED";

export type CreatePrayerAssessmentRequest = {
    missedYears: number;
    missedMonths: number;
    missedDays: number;
    dailyRecoveryTarget: number;
};

export type PrayerAssessment = {
    id: string;
    missedYears: number;
    missedMonths: number;
    missedDays: number;
    totalEstimatedDays: number;
    remainingByPrayer: RemainingByPrayer;
    totalRemainingPrayers: number;
    dailyRecoveryTarget: number;
    estimatedCompletionDays: number;
    createdAt: string;
};

export type PrayerProgress = {
    assessmentId: string;
    totalRecoveryCycles: number;
    completedCycles: number;
    currentCycleNumber: number;
    completedPrayersInCurrentCycle: number;
    currentCycle: {
        fajr: CyclePrayerStatus;
        dhuhr: CyclePrayerStatus;
        asr: CyclePrayerStatus;
        maghrib: CyclePrayerStatus;
        isha: CyclePrayerStatus;
    };
    nextPrayer: PrayerType | null;
    totalCompletedPrayers: number;
    totalRemainingPrayers: number;
    progressPercentage: number;
    dailyRecoveryTarget: number;
    estimatedRemainingDays: number;
};

export type CompleteNextPrayerRequest = {
    assessmentId: string;
    completedOn: string;
};

export type CompleteNextPrayerResponse = {
    completedPrayer: PrayerType;
    nextPrayer: PrayerType | null;
    completedCycles: number;
    completedPrayersInCurrentCycle: number;
    totalCompletedPrayers: number;
    totalRemainingPrayers: number;
    totalRecoveryCycles: number;
    currentCycleNumber: number;
    progressPercentage: number;
};

export type RecoveryHistoryItem = {
    id: string;
    prayerType: PrayerType;
    sequenceNumber: number;
    completedOn: string;
    createdAt: string;
};
