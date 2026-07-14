import { useQuery } from "@tanstack/react-query";

import { getQuranPage } from "../api/quran.api";
import { quranPageQueryKey } from "./quranQueryKeys";

export function useQuranPage(pageNumber: number, enabled = true) {
    return useQuery({
        queryKey: quranPageQueryKey(pageNumber),
        queryFn: () => getQuranPage(pageNumber),
        enabled,
        retry: false,
    });
}
