CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    event_id UUID NOT NULL,
    type VARCHAR(64) NOT NULL,
    title VARCHAR(120) NOT NULL,
    message VARCHAR(500) NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_notifications_event_id UNIQUE (event_id),
    CONSTRAINT chk_notifications_title_len CHECK (char_length(title) BETWEEN 1 AND 120),
    CONSTRAINT chk_notifications_message_len CHECK (char_length(message) BETWEEN 1 AND 500),
    CONSTRAINT chk_notifications_type CHECK (
        type IN (
            'QURAN_TASK_COMPLETED',
            'PRAYER_RECOVERY_PROGRESS',
            'JOURNEY_MILESTONE',
            'SYSTEM'
        )
    )
);

CREATE INDEX idx_notifications_user_created
    ON notifications (user_id, created_at DESC);
