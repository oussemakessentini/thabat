import { useMutation, useQueryClient } from "@tanstack/react-query";

import { undoLatestPrayer } from "../api/prayer.api";

import {
    prayerProgressQueryKey,
    recoveryEntriesQueryKey,
} from "./prayerQueryKeys";

export function useUndoLatestPrayer() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (assessmentId: string) => undoLatestPrayer(assessmentId),
        onSuccess: async () => {
            await Promise.all([
                queryClient.invalidateQueries({ queryKey: prayerProgressQueryKey }),
                queryClient.invalidateQueries({ queryKey: recoveryEntriesQueryKey }),
            ]);
        },
    });
}
