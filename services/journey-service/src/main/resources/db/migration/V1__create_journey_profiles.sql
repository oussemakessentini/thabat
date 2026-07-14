CREATE TABLE journey_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    experience_mode VARCHAR(20) NOT NULL,
    prayer_level VARCHAR(40) NOT NULL,
    quran_level VARCHAR(40) NOT NULL,
    reminder_preference VARCHAR(20) NOT NULL,
    onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_journey_profiles_user_id UNIQUE (user_id)
);

CREATE INDEX idx_journey_profiles_user_id ON journey_profiles (user_id);
CREATE INDEX idx_journey_profiles_onboarding_completed
    ON journey_profiles (onboarding_completed);

CREATE TABLE journey_profile_goals (
    profile_id UUID NOT NULL,
    goal VARCHAR(50) NOT NULL,
    CONSTRAINT pk_journey_profile_goals PRIMARY KEY (profile_id, goal),
    CONSTRAINT fk_journey_profile_goals_profile
        FOREIGN KEY (profile_id)
            REFERENCES journey_profiles (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_journey_profile_goals_profile_id
    ON journey_profile_goals (profile_id);
