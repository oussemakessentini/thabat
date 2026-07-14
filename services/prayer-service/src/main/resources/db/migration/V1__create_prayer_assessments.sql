CREATE TABLE prayer_assessments (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    missed_years INTEGER NOT NULL,
    missed_months INTEGER NOT NULL,
    missed_days INTEGER NOT NULL,
    total_estimated_days BIGINT NOT NULL,
    fajr_remaining BIGINT NOT NULL,
    dhuhr_remaining BIGINT NOT NULL,
    asr_remaining BIGINT NOT NULL,
    maghrib_remaining BIGINT NOT NULL,
    isha_remaining BIGINT NOT NULL,
    total_remaining_prayers BIGINT NOT NULL,
    daily_recovery_target INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_prayer_assessments_user_id_created_at
    ON prayer_assessments (user_id, created_at DESC);
