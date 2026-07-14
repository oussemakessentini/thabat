export type QuranPageStatus =
    | "NOT_STARTED"
    | "LEARNING"
    | "MEMORIZED"
    | "NEEDS_REVISION"
    | "STRONG";

export type QuranRevelationType = "MECCAN" | "MEDINAN";

export type QuranPageProgress = {
    pageNumber: number;
    status: QuranPageStatus;
    memorizedAt: string | null;
    lastReviewedAt: string | null;
    successfulReviewCount: number;
    confidenceLevel: number | null;
    notes: string | null;
};

export type QuranProgressSummary = {
    totalPages: number;
    notStartedPages: number;
    learningPages: number;
    memorizedPages: number;
    needsRevisionPages: number;
    strongPages: number;
    completedPages: number;
    completionPercentage: number;
    reviewedThisWeek: number;
    lastUpdatedPage: number | null;
};

export type UpdateQuranPageRequest = {
    status?: QuranPageStatus;
    memorizedAt?: string | null;
    confidenceLevel?: number | null;
    notes?: string | null;
};

export type RecordQuranReviewRequest = {
    reviewedAt: string;
    successful: boolean;
    confidenceLevel?: number | null;
    newStatus?: QuranPageStatus | null;
};

export type QuranSectionType = "SURAH" | "JUZ" | "HIZB";

export type QuranAggregateFields = {
    totalPages: number;
    notStartedPages: number;
    learningPages: number;
    memorizedPages: number;
    needsRevisionPages: number;
    strongPages: number;
    completedPages: number;
    completionPercentage: number;
};

export type QuranSurahProgress = QuranAggregateFields & {
    surahNumber: number;
    nameArabic: string;
    nameEnglish: string;
    transliteration: string;
    ayahCount: number;
    revelationType: QuranRevelationType;
    startPage: number;
    endPage: number;
};

export type QuranJuzProgress = QuranAggregateFields & {
    juzNumber: number;
    startPage: number;
    endPage: number;
};

export type QuranHizbProgress = QuranAggregateFields & {
    hizbNumber: number;
    juzNumber: number;
    startPage: number;
    endPage: number;
};

export type QuranSectionPage = {
    pageNumber: number;
    startAyah: number | null;
    endAyah: number | null;
    status: QuranPageStatus;
};

export type QuranSectionDetail = QuranAggregateFields & {
    sectionType: QuranSectionType;
    sectionNumber: number;
    title: string;
    nameArabic: string | null;
    nameEnglish: string | null;
    transliteration: string | null;
    ayahCount: number | null;
    revelationType: string | null;
    startPage: number;
    endPage: number;
    pages: QuranSectionPage[];
};

export const QURAN_STATUS_LABELS: Record<QuranPageStatus, string> = {
    NOT_STARTED: "Not started",
    LEARNING: "Learning",
    MEMORIZED: "Memorized",
    NEEDS_REVISION: "Needs revision",
    STRONG: "Strong",
};

export type QuranTaskType = "MEMORIZATION" | "REVISION";

export type QuranTaskStatus = "PENDING" | "COMPLETED" | "SKIPPED";

export type QuranDailyGoal = {
    id: string;
    memorizationPagesPerDay: number;
    revisionPagesPerDay: number;
    preferredStartPage: number | null;
    active: boolean;
};

export type UpsertQuranDailyGoalRequest = {
    memorizationPagesPerDay: number;
    revisionPagesPerDay: number;
    preferredStartPage?: number | null;
};

export type QuranDailyTask = {
    id: string;
    pageNumber: number;
    taskType: QuranTaskType;
    status: QuranTaskStatus;
};

export type QuranTodayTasks = {
    date: string;
    memorizationTasks: QuranDailyTask[];
    revisionTasks: QuranDailyTask[];
    completedTasks: number;
    pendingTasks: number;
    skippedTasks: number;
    totalTasks: number;
    completionPercentage: number;
};

export type CompleteQuranTaskRequest = {
    confidenceLevel?: number | null;
    successful?: boolean | null;
};

export type QuranTaskMutationResult = {
    task: QuranDailyTask;
    today: QuranTodayTasks;
};

export const QURAN_TASK_TYPE_LABELS: Record<QuranTaskType, string> = {
    MEMORIZATION: "Memorization",
    REVISION: "Revision",
};

export const QURAN_TASK_STATUS_LABELS: Record<QuranTaskStatus, string> = {
    PENDING: "Pending",
    COMPLETED: "Completed",
    SKIPPED: "Skipped",
};
