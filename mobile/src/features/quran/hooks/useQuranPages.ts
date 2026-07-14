import { useQuery } from "@tanstack/react-query";

import { getQuranPages } from "../api/quran.api";
import type { QuranPageStatus } from "../types/quran.types";
import { quranPagesQueryKey } from "./quranQueryKeys";

export function useQuranPages(
    status?: QuranPageStatus,
    enabled = true,
) {
    return useQuery({
        queryKey: quranPagesQueryKey(status),
        queryFn: () => getQuranPages(status),
        enabled,
        retry: false,
    });
}
