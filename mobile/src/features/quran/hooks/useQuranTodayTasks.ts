import { useQuery } from "@tanstack/react-query";

import { getTodayTasks } from "../api/quran.api";
import { quranTodayTasksQueryKey } from "./quranQueryKeys";

export function useQuranTodayTasks(enabled = true) {
    return useQuery({
        queryKey: quranTodayTasksQueryKey,
        queryFn: getTodayTasks,
        enabled,
        retry: false,
    });
}
