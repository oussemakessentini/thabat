import { useQuery } from "@tanstack/react-query";

import { getLatestPrayerAssessment } from "../api/prayer.api";

import { latestPrayerAssessmentQueryKey } from "./useCreatePrayerAssessment";

export { latestPrayerAssessmentQueryKey };

export function useLatestPrayerAssessment(enabled = true) {
    return useQuery({
        queryKey: latestPrayerAssessmentQueryKey,
        queryFn: getLatestPrayerAssessment,
        enabled,
        retry: false,
    });
}
