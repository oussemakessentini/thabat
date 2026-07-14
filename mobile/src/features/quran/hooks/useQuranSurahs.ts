import { useQuery } from "@tanstack/react-query";

import { getSurahs } from "../api/quran.api";
import { quranSurahsQueryKey } from "./quranQueryKeys";

export function useQuranSurahs(enabled = true) {
    return useQuery({
        queryKey: quranSurahsQueryKey,
        queryFn: () => getSurahs(),
        enabled,
        retry: false,
    });
}
