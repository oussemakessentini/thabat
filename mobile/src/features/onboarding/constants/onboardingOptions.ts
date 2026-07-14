export const GOAL_OPTIONS = [
    { value: "PRAYER_CONSISTENCY" as const, label: "Build prayer consistency" },
    { value: "PRAYER_RECOVERY" as const, label: "Recover missed prayers" },
    { value: "QURAN_READING" as const, label: "Read Quran regularly" },
    { value: "QURAN_MEMORIZATION" as const, label: "Memorize Quran" },
    { value: "QURAN_REVISION" as const, label: "Review Quran" },
    { value: "DHIKR" as const, label: "Practice dhikr" },
    { value: "ISLAMIC_KNOWLEDGE" as const, label: "Learn more about Islam" },
];

export const PRAYER_LEVEL_OPTIONS = [
    { value: "NOT_PRAYING" as const, label: "I am not currently praying" },
    { value: "SOMETIMES" as const, label: "Sometimes" },
    { value: "MOST_PRAYERS" as const, label: "Most prayers" },
    { value: "FIVE_DAILY_PRAYERS" as const, label: "Five daily prayers" },
];

export const QURAN_LEVEL_OPTIONS = [
    { value: "BEGINNER" as const, label: "Beginner" },
    { value: "BASIC" as const, label: "Basic" },
    { value: "INTERMEDIATE" as const, label: "Intermediate" },
    { value: "ADVANCED" as const, label: "Advanced" },
    { value: "HAFIZ" as const, label: "Hafiz" },
];

export const EXPERIENCE_MODE_OPTIONS = [
    { value: "ADULT" as const, label: "Adult mode" },
    { value: "KIDS" as const, label: "Kids mode" },
];

export const REMINDER_OPTIONS = [
    { value: "MORNING" as const, label: "Morning" },
    { value: "EVENING" as const, label: "Evening" },
    { value: "BOTH" as const, label: "Both" },
    { value: "NONE" as const, label: "None" },
];
