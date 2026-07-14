export type NotificationType =
    | "QURAN_TASK_COMPLETED"
    | "PRAYER_RECOVERY_PROGRESS"
    | "JOURNEY_MILESTONE"
    | "SYSTEM";

export type AppNotification = {
    id: string;
    type: NotificationType;
    title: string;
    message: string;
    read: boolean;
    createdAt: string;
};

export type UnreadCountResponse = {
    unreadCount: number;
};

export type NotificationPage = {
    content: AppNotification[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
    last: boolean;
};
