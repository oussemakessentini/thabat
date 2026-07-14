-- Remodel recovery entries for sequential makeup tracking.
-- Historical quantity/notes-based rows are cleared; assessment baselines stay immutable.

DELETE FROM prayer_recovery_entries;

ALTER TABLE prayer_recovery_entries
    DROP CONSTRAINT IF EXISTS chk_prayer_recovery_entries_quantity;

DROP INDEX IF EXISTS idx_prayer_recovery_entries_assessment_prayer;
DROP INDEX IF EXISTS idx_prayer_recovery_entries_user_completed;

ALTER TABLE prayer_recovery_entries
    DROP COLUMN IF EXISTS quantity;

ALTER TABLE prayer_recovery_entries
    DROP COLUMN IF EXISTS notes;

ALTER TABLE prayer_recovery_entries
    ADD COLUMN sequence_number BIGINT NOT NULL;

ALTER TABLE prayer_recovery_entries
    ADD CONSTRAINT uq_prayer_recovery_assessment_sequence
        UNIQUE (assessment_id, sequence_number);

CREATE INDEX idx_prayer_recovery_entries_user_assessment_sequence
    ON prayer_recovery_entries (user_id, assessment_id, sequence_number DESC);

CREATE INDEX idx_prayer_recovery_entries_user_completed
    ON prayer_recovery_entries (user_id, completed_on DESC, created_at DESC);
