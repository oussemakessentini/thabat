import { useMutation, useQueryClient } from "@tanstack/react-query";

import { recordQuranReview } from "../api/quran.api";
import type { RecordQuranReviewRequest } from "../types/quran.types";
import {
    quranHizbsQueryKey,
    quranJuzQueryKey,
    quranPageQueryKey,
    quranProgressQueryKey,
    quranSurahsQueryKey,
    quranTodayTasksQueryKey,
} from "./quranQueryKeys";

export function useRecordQuranReview(pageNumber: number) {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (payload: RecordQuranReviewRequest) =>
            recordQuranReview(pageNumber, payload),
        onSuccess: (page) => {
            queryClient.setQueryData(quranPageQueryKey(pageNumber), page);
            void queryClient.invalidateQueries({ queryKey: ["quran", "pages"] });
            void queryClient.invalidateQueries({ queryKey: quranProgressQueryKey });
            void queryClient.invalidateQueries({ queryKey: quranSurahsQueryKey });
            void queryClient.invalidateQueries({ queryKey: quranJuzQueryKey });
            void queryClient.invalidateQueries({ queryKey: quranHizbsQueryKey });
            void queryClient.invalidateQueries({ queryKey: ["quran", "section"] });
            void queryClient.invalidateQueries({ queryKey: quranTodayTasksQueryKey });
            void queryClient.invalidateQueries({
                queryKey: quranPageQueryKey(pageNumber),
            });
        },
    });
}
