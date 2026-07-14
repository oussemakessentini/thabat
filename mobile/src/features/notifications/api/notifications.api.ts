import { apiClient } from "../../../shared/api/apiClient";

import type {
    AppNotification,
    NotificationPage,
    UnreadCountResponse,
} from "../types/notification.types";

export async function getNotifications(
    page = 0,
    size = 20,
): Promise<NotificationPage> {
    const { data } = await apiClient.get<NotificationPage>(
        "/api/v1/notifications",
        { params: { page, size } },
    );
    return data;
}

export async function getUnreadNotificationCount(): Promise<UnreadCountResponse> {
    const { data } = await apiClient.get<UnreadCountResponse>(
        "/api/v1/notifications/unread-count",
    );
    return data;
}

export async function markNotificationRead(
    notificationId: string,
): Promise<AppNotification> {
    const { data } = await apiClient.patch<AppNotification>(
        `/api/v1/notifications/${notificationId}/read`,
    );
    return data;
}

export async function markAllNotificationsRead(): Promise<void> {
    await apiClient.patch("/api/v1/notifications/read-all");
}
