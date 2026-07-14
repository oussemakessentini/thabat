import { useQuery } from "@tanstack/react-query";

import { getHizbList } from "../api/quran.api";
import { quranHizbsQueryKey } from "./quranQueryKeys";

export function useQuranHizbs(enabled = true) {
    return useQuery({
        queryKey: quranHizbsQueryKey,
        queryFn: () => getHizbList(),
        enabled,
        retry: false,
    });
}
