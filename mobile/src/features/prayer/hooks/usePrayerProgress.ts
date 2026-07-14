import { useQuery } from "@tanstack/react-query";
import axios from "axios";

import { getPrayerProgress } from "../api/prayer.api";

import { prayerProgressQueryKey } from "./prayerQueryKeys";

export function usePrayerProgress(enabled = true) {
    return useQuery({
        queryKey: prayerProgressQueryKey,
        queryFn: getPrayerProgress,
        enabled,
        retry: false,
    });
}

export function isPrayerProgressMissing(error: unknown): boolean {
    return axios.isAxiosError(error) && error.response?.status === 404;
}
