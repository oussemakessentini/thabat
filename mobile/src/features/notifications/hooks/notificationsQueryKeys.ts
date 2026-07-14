export const notificationsListQueryKey = (page: number) =>
    ["notifications", "list", page] as const;

export const notificationsUnreadCountQueryKey = [
    "notifications",
    "unreadCount",
] as const;
