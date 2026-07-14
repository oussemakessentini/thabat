CREATE TABLE prayer_recovery_entries (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    assessment_id UUID NOT NULL,
    prayer_type VARCHAR(20) NOT NULL,
    quantity INTEGER NOT NULL,
    completed_on DATE NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_prayer_recovery_entries_assessment
        FOREIGN KEY (assessment_id)
            REFERENCES prayer_assessments (id)
            ON DELETE CASCADE,
    CONSTRAINT chk_prayer_recovery_entries_quantity
        CHECK (quantity >= 1 AND quantity <= 100)
);

CREATE INDEX idx_prayer_recovery_entries_user_completed
    ON prayer_recovery_entries (user_id, completed_on DESC, created_at DESC);

CREATE INDEX idx_prayer_recovery_entries_assessment_prayer
    ON prayer_recovery_entries (assessment_id, prayer_type);

CREATE INDEX idx_prayer_recovery_entries_user_id
    ON prayer_recovery_entries (user_id);
