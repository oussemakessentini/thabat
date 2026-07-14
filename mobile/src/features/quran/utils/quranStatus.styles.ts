import { colors } from "../../../shared/theme";

import type { QuranPageStatus } from "../types/quran.types";

export function quranStatusColor(status: QuranPageStatus): string {
    switch (status) {
        case "NOT_STARTED":
            return colors.muted;
        case "LEARNING":
            return colors.secondary;
        case "MEMORIZED":
            return colors.primary;
        case "NEEDS_REVISION":
            return colors.warning;
        case "STRONG":
            return colors.success;
        default:
            return colors.muted;
    }
}
