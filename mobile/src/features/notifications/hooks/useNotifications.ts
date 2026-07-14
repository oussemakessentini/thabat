import { useInfiniteQuery } from "@tanstack/react-query";

import { getNotifications } from "../api/notifications.api";
import { notificationsListQueryKey } from "./notificationsQueryKeys";

export function useNotifications(enabled = true) {
    return useInfiniteQuery({
        queryKey: notificationsListQueryKey(0),
        queryFn: ({ pageParam }) => getNotifications(pageParam, 20),
        initialPageParam: 0,
        getNextPageParam: (lastPage) =>
            lastPage.last ? undefined : lastPage.number + 1,
        enabled,
        retry: false,
    });
}
