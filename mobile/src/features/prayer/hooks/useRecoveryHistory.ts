import { useQuery } from "@tanstack/react-query";

import { getRecoveryHistory } from "../api/prayer.api";

import { recoveryEntriesQueryKey } from "./prayerQueryKeys";

export function useRecoveryHistory(enabled = true) {
    return useQuery({
        queryKey: recoveryEntriesQueryKey,
        queryFn: getRecoveryHistory,
        enabled,
        retry: false,
    });
}
