import { useQuery } from "@tanstack/react-query";

import { getQuranProgress } from "../api/quran.api";
import { quranProgressQueryKey } from "./quranQueryKeys";

export function useQuranProgress(enabled = true) {
    return useQuery({
        queryKey: quranProgressQueryKey,
        queryFn: getQuranProgress,
        enabled,
        retry: false,
    });
}
