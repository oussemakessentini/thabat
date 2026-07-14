import { useMutation, useQueryClient } from "@tanstack/react-query";

import { saveDailyGoal } from "../api/quran.api";
import type { UpsertQuranDailyGoalRequest } from "../types/quran.types";
import { quranDailyGoalQueryKey, quranTodayTasksQueryKey } from "./quranQueryKeys";

export function useSaveQuranDailyGoal() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (payload: UpsertQuranDailyGoalRequest) => saveDailyGoal(payload),
        onSuccess: (goal) => {
            queryClient.setQueryData(quranDailyGoalQueryKey, goal);
            void queryClient.invalidateQueries({ queryKey: quranTodayTasksQueryKey });
        },
    });
}
