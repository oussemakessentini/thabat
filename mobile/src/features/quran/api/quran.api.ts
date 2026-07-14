import { apiClient } from "../../../shared/api/apiClient";

import type {
    CompleteQuranTaskRequest,
    QuranDailyGoal,
    QuranHizbProgress,
    QuranJuzProgress,
    QuranPageProgress,
    QuranPageStatus,
    QuranProgressSummary,
    QuranSectionDetail,
    QuranSurahProgress,
    QuranTaskMutationResult,
    QuranTodayTasks,
    RecordQuranReviewRequest,
    UpdateQuranPageRequest,
    UpsertQuranDailyGoalRequest,
} from "../types/quran.types";

export async function getQuranPages(
    status?: QuranPageStatus,
): Promise<QuranPageProgress[]> {
    const { data } = await apiClient.get<QuranPageProgress[]>(
        "/api/v1/quran/pages",
        {
            params: status ? { status } : undefined,
        },
    );
    return data;
}

export async function getQuranPage(
    pageNumber: number,
): Promise<QuranPageProgress> {
    const { data } = await apiClient.get<QuranPageProgress>(
        `/api/v1/quran/pages/${pageNumber}`,
    );
    return data;
}

export async function updateQuranPage(
    pageNumber: number,
    payload: UpdateQuranPageRequest,
): Promise<QuranPageProgress> {
    const { data } = await apiClient.put<QuranPageProgress>(
        `/api/v1/quran/pages/${pageNumber}`,
        payload,
    );
    return data;
}

export async function recordQuranReview(
    pageNumber: number,
    payload: RecordQuranReviewRequest,
): Promise<QuranPageProgress> {
    const { data } = await apiClient.post<QuranPageProgress>(
        `/api/v1/quran/pages/${pageNumber}/reviews`,
        payload,
    );
    return data;
}

export async function getQuranProgress(): Promise<QuranProgressSummary> {
    const { data } = await apiClient.get<QuranProgressSummary>(
        "/api/v1/quran/progress",
    );
    return data;
}

export async function getSurahs(): Promise<QuranSurahProgress[]> {
    const { data } = await apiClient.get<QuranSurahProgress[]>(
        "/api/v1/quran/surahs",
    );
    return data;
}

export async function getSurah(
    surahNumber: number,
): Promise<QuranSectionDetail> {
    const { data } = await apiClient.get<QuranSectionDetail>(
        `/api/v1/quran/surahs/${surahNumber}`,
    );
    return data;
}

export async function getJuzList(): Promise<QuranJuzProgress[]> {
    const { data } = await apiClient.get<QuranJuzProgress[]>(
        "/api/v1/quran/juz",
    );
    return data;
}

export async function getJuz(juzNumber: number): Promise<QuranSectionDetail> {
    const { data } = await apiClient.get<QuranSectionDetail>(
        `/api/v1/quran/juz/${juzNumber}`,
    );
    return data;
}

export async function getHizbList(): Promise<QuranHizbProgress[]> {
    const { data } = await apiClient.get<QuranHizbProgress[]>(
        "/api/v1/quran/hizbs",
    );
    return data;
}

export async function getHizb(
    hizbNumber: number,
): Promise<QuranSectionDetail> {
    const { data } = await apiClient.get<QuranSectionDetail>(
        `/api/v1/quran/hizbs/${hizbNumber}`,
    );
    return data;
}

export async function getDailyGoal(): Promise<QuranDailyGoal> {
    const { data } = await apiClient.get<QuranDailyGoal>(
        "/api/v1/quran/goals/daily",
    );
    return data;
}

export async function saveDailyGoal(
    payload: UpsertQuranDailyGoalRequest,
): Promise<QuranDailyGoal> {
    const { data } = await apiClient.put<QuranDailyGoal>(
        "/api/v1/quran/goals/daily",
        payload,
    );
    return data;
}

export async function getTodayTasks(): Promise<QuranTodayTasks> {
    const { data } = await apiClient.get<QuranTodayTasks>(
        "/api/v1/quran/tasks/today",
    );
    return data;
}

export async function completeTask(
    taskId: string,
    payload: CompleteQuranTaskRequest,
): Promise<QuranTaskMutationResult> {
    const { data } = await apiClient.post<QuranTaskMutationResult>(
        `/api/v1/quran/tasks/${taskId}/complete`,
        payload,
    );
    return data;
}

export async function skipTask(
    taskId: string,
): Promise<QuranTaskMutationResult> {
    const { data } = await apiClient.post<QuranTaskMutationResult>(
        `/api/v1/quran/tasks/${taskId}/skip`,
    );
    return data;
}
