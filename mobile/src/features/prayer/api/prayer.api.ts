import { apiClient } from "../../../shared/api/apiClient";

import type {
    CompleteNextPrayerRequest,
    CompleteNextPrayerResponse,
    CreatePrayerAssessmentRequest,
    PrayerAssessment,
    PrayerProgress,
    RecoveryHistoryItem,
} from "../types/prayer.types";

export async function createPrayerAssessment(
    payload: CreatePrayerAssessmentRequest,
): Promise<PrayerAssessment> {
    const { data } = await apiClient.post<PrayerAssessment>(
        "/api/v1/prayers/assessments",
        payload,
    );
    return data;
}

export async function getLatestPrayerAssessment(): Promise<PrayerAssessment> {
    const { data } = await apiClient.get<PrayerAssessment>(
        "/api/v1/prayers/assessments/latest",
    );
    return data;
}

export async function getPrayerProgress(): Promise<PrayerProgress> {
    const { data } = await apiClient.get<PrayerProgress>(
        "/api/v1/prayers/progress",
    );
    return data;
}

export async function completeNextPrayer(
    payload: CompleteNextPrayerRequest,
): Promise<CompleteNextPrayerResponse> {
    const { data } = await apiClient.post<CompleteNextPrayerResponse>(
        "/api/v1/prayers/recovery/complete-next",
        payload,
    );
    return data;
}

export async function undoLatestPrayer(assessmentId: string): Promise<PrayerProgress> {
    const { data } = await apiClient.delete<PrayerProgress>(
        "/api/v1/prayers/recovery/latest",
        { data: { assessmentId } },
    );
    return data;
}

export async function getRecoveryHistory(): Promise<RecoveryHistoryItem[]> {
    const { data } = await apiClient.get<RecoveryHistoryItem[]>(
        "/api/v1/prayers/recovery/history",
    );
    return data;
}
