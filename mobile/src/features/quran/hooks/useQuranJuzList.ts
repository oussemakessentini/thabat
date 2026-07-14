import { useQuery } from "@tanstack/react-query";

import { getJuzList } from "../api/quran.api";
import { quranJuzQueryKey } from "./quranQueryKeys";

export function useQuranJuzList(enabled = true) {
    return useQuery({
        queryKey: quranJuzQueryKey,
        queryFn: () => getJuzList(),
        enabled,
        retry: false,
    });
}
