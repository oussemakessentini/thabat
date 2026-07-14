import { useMutation, useQueryClient } from "@tanstack/react-query";

import { completeNextPrayer } from "../api/prayer.api";
import type { CompleteNextPrayerRequest } from "../types/prayer.types";

import {
    prayerProgressQueryKey,
    recoveryEntriesQueryKey,
} from "./prayerQueryKeys";

export function useCompleteNextPrayer() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (payload: CompleteNextPrayerRequest) =>
            completeNextPrayer(payload),
        onSuccess: async () => {
            await Promise.all([
                queryClient.invalidateQueries({ queryKey: prayerProgressQueryKey }),
                queryClient.invalidateQueries({ queryKey: recoveryEntriesQueryKey }),
            ]);
        },
    });
}
