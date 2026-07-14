import { useQuery } from "@tanstack/react-query";

import { getUnreadNotificationCount } from "../api/notifications.api";
import { notificationsUnreadCountQueryKey } from "./notificationsQueryKeys";

export function useUnreadNotificationCount(enabled = true) {
    return useQuery({
        queryKey: notificationsUnreadCountQueryKey,
        queryFn: getUnreadNotificationCount,
        enabled,
        retry: false,
        refetchInterval: 30_000,
    });
}
