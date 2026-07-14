import { useMutation, useQueryClient } from "@tanstack/react-query";

import { createPrayerAssessment } from "../api/prayer.api";
import type {
    CreatePrayerAssessmentRequest,
    PrayerAssessment,
} from "../types/prayer.types";

import { prayerProgressQueryKey } from "./prayerQueryKeys";

export const latestPrayerAssessmentQueryKey = [
    "prayer",
    "assessments",
    "latest",
] as const;

export function useCreatePrayerAssessment() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (payload: CreatePrayerAssessmentRequest) =>
            createPrayerAssessment(payload),
        onSuccess: (assessment: PrayerAssessment) => {
            queryClient.setQueryData(latestPrayerAssessmentQueryKey, assessment);
            void queryClient.invalidateQueries({ queryKey: prayerProgressQueryKey });
        },
    });
}
