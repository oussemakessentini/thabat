import { useQuery, useQueryClient } from "@tanstack/react-query";
import axios from "axios";

import { getJourneyProfile } from "../api/journey.api";

export const journeyProfileQueryKey = ["journey", "profile"] as const;

export function useJourneyProfile(enabled: boolean) {
    return useQuery({
        queryKey: journeyProfileQueryKey,
        queryFn: getJourneyProfile,
        enabled,
        retry: false,
        staleTime: 60_000,
    });
}

export function isJourneyProfileMissing(error: unknown): boolean {
    return axios.isAxiosError(error) && error.response?.status === 404;
}

export function clearJourneyProfileCache(
    queryClient: ReturnType<typeof useQueryClient>,
): void {
    queryClient.removeQueries({ queryKey: journeyProfileQueryKey });
}
