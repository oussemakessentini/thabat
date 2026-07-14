import { useQuery } from "@tanstack/react-query";
import axios from "axios";

import { getDailyGoal } from "../api/quran.api";
import { quranDailyGoalQueryKey } from "./quranQueryKeys";

export function useQuranDailyGoal(enabled = true) {
    return useQuery({
        queryKey: quranDailyGoalQueryKey,
        queryFn: getDailyGoal,
        enabled,
        retry: false,
    });
}

export function isQuranDailyGoalMissing(error: unknown): boolean {
    return axios.isAxiosError(error) && error.response?.status === 404;
}
