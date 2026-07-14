CREATE TABLE quran_daily_goal (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    memorization_pages_per_day INT NOT NULL,
    revision_pages_per_day INT NOT NULL,
    preferred_start_page INT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_quran_daily_goal_user UNIQUE (user_id),
    CONSTRAINT chk_quran_daily_goal_mem_pages CHECK (
        memorization_pages_per_day BETWEEN 0 AND 20
    ),
    CONSTRAINT chk_quran_daily_goal_rev_pages CHECK (
        revision_pages_per_day BETWEEN 0 AND 50
    ),
    CONSTRAINT chk_quran_daily_goal_at_least_one CHECK (
        memorization_pages_per_day > 0 OR revision_pages_per_day > 0
    ),
    CONSTRAINT chk_quran_daily_goal_preferred_page CHECK (
        preferred_start_page IS NULL
        OR preferred_start_page BETWEEN 1 AND 604
    )
);

-- One goal row per user (MVP). App upserts the same row and keeps active=true.
-- Unique(user_id) enforces a single active goal without a partial index (H2-safe).

CREATE TABLE quran_daily_task (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    goal_id UUID NOT NULL REFERENCES quran_daily_goal (id),
    task_date DATE NOT NULL,
    task_type VARCHAR(32) NOT NULL,
    page_number INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_quran_daily_task_user_date_type_page
        UNIQUE (user_id, task_date, task_type, page_number),
    CONSTRAINT chk_quran_daily_task_page CHECK (page_number BETWEEN 1 AND 604),
    CONSTRAINT chk_quran_daily_task_type CHECK (
        task_type IN ('MEMORIZATION', 'REVISION')
    ),
    CONSTRAINT chk_quran_daily_task_status CHECK (
        status IN ('PENDING', 'COMPLETED', 'SKIPPED')
    )
);

CREATE INDEX idx_quran_daily_task_user_date
    ON quran_daily_task (user_id, task_date);

CREATE INDEX idx_quran_daily_task_goal_id
    ON quran_daily_task (goal_id);
