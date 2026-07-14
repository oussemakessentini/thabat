import { useMutation, useQueryClient } from "@tanstack/react-query";

import {
    markAllNotificationsRead,
    markNotificationRead,
} from "../api/notifications.api";
import { notificationsUnreadCountQueryKey } from "./notificationsQueryKeys";

export function useMarkNotificationRead() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (notificationId: string) =>
            markNotificationRead(notificationId),
        onSuccess: async () => {
            await Promise.all([
                queryClient.invalidateQueries({ queryKey: ["notifications"] }),
                queryClient.invalidateQueries({
                    queryKey: notificationsUnreadCountQueryKey,
                }),
            ]);
        },
    });
}

export function useMarkAllNotificationsRead() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: () => markAllNotificationsRead(),
        onSuccess: async () => {
            await Promise.all([
                queryClient.invalidateQueries({ queryKey: ["notifications"] }),
                queryClient.invalidateQueries({
                    queryKey: notificationsUnreadCountQueryKey,
                }),
            ]);
        },
    });
}
