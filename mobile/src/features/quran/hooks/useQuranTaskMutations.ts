import { useMutation, useQueryClient } from "@tanstack/react-query";

import { completeTask, skipTask } from "../api/quran.api";
import type { CompleteQuranTaskRequest } from "../types/quran.types";
import { notificationsUnreadCountQueryKey } from "../../notifications/hooks/notificationsQueryKeys";
import {
    quranHizbsQueryKey,
    quranJuzQueryKey,
    quranPageQueryKey,
    quranProgressQueryKey,
    quranSurahsQueryKey,
    quranTodayTasksQueryKey,
} from "./quranQueryKeys";

function invalidateQuranProgressCaches(
    queryClient: ReturnType<typeof useQueryClient>,
    pageNumber: number,
) {
    void queryClient.invalidateQueries({ queryKey: quranTodayTasksQueryKey });
    void queryClient.invalidateQueries({ queryKey: quranProgressQueryKey });
    void queryClient.invalidateQueries({ queryKey: ["quran", "pages"] });
    void queryClient.invalidateQueries({ queryKey: quranPageQueryKey(pageNumber) });
    void queryClient.invalidateQueries({ queryKey: quranSurahsQueryKey });
    void queryClient.invalidateQueries({ queryKey: quranJuzQueryKey });
    void queryClient.invalidateQueries({ queryKey: quranHizbsQueryKey });
    void queryClient.invalidateQueries({ queryKey: ["quran", "section"] });
}

function scheduleUnreadCountRefresh(
    queryClient: ReturnType<typeof useQueryClient>,
) {
    // Notification is created asynchronously via Kafka — refresh after a short delay.
    setTimeout(() => {
        void queryClient.invalidateQueries({
            queryKey: notificationsUnreadCountQueryKey,
        });
    }, 1500);
}

export function useCompleteQuranTask() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({
            taskId,
            payload,
        }: {
            taskId: string;
            payload: CompleteQuranTaskRequest;
        }) => completeTask(taskId, payload),
        onSuccess: (result) => {
            queryClient.setQueryData(quranTodayTasksQueryKey, result.today);
            invalidateQuranProgressCaches(queryClient, result.task.pageNumber);
            scheduleUnreadCountRefresh(queryClient);
        },
    });
}

export function useSkipQuranTask() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (taskId: string) => skipTask(taskId),
        onSuccess: (result) => {
            queryClient.setQueryData(quranTodayTasksQueryKey, result.today);
            invalidateQuranProgressCaches(queryClient, result.task.pageNumber);
        },
    });
}
