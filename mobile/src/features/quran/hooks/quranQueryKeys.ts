import type { QuranPageStatus, QuranSectionType } from "../types/quran.types";

export const quranPagesQueryKey = (status?: QuranPageStatus | "ALL") =>
    ["quran", "pages", status ?? "ALL"] as const;

export const quranPageQueryKey = (pageNumber: number) =>
    ["quran", "page", pageNumber] as const;

export const quranProgressQueryKey = ["quran", "progress"] as const;

export const quranSurahsQueryKey = ["quran", "surahs"] as const;

export const quranJuzQueryKey = ["quran", "juzList"] as const;

export const quranHizbsQueryKey = ["quran", "hizbs"] as const;

export const quranSectionDetailQueryKey = (
    sectionType: QuranSectionType,
    sectionNumber: number,
) => ["quran", "section", sectionType, sectionNumber] as const;

export const quranDailyGoalQueryKey = ["quran", "dailyGoal"] as const;

export const quranTodayTasksQueryKey = ["quran", "todayTasks"] as const;
