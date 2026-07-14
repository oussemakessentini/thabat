import { useQuery } from "@tanstack/react-query";

import { getHizb, getJuz, getSurah } from "../api/quran.api";
import type { QuranSectionType } from "../types/quran.types";
import { quranSectionDetailQueryKey } from "./quranQueryKeys";

export function useQuranSectionDetail(
    sectionType: QuranSectionType,
    sectionNumber: number,
    enabled = true,
) {
    return useQuery({
        queryKey: quranSectionDetailQueryKey(sectionType, sectionNumber),
        queryFn: () => {
            if (sectionType === "SURAH") {
                return getSurah(sectionNumber);
            }
            if (sectionType === "JUZ") {
                return getJuz(sectionNumber);
            }
            return getHizb(sectionNumber);
        },
        enabled,
        retry: false,
    });
}
