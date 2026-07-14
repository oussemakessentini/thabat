CREATE TABLE quran_page_progress (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    page_number INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    memorized_at DATE,
    last_reviewed_at DATE,
    successful_review_count INT NOT NULL DEFAULT 0,
    confidence_level INT,
    notes VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_quran_page_progress_user_page UNIQUE (user_id, page_number),
    CONSTRAINT chk_quran_page_number CHECK (page_number BETWEEN 1 AND 604),
    CONSTRAINT chk_quran_successful_review_count CHECK (successful_review_count >= 0),
    CONSTRAINT chk_quran_confidence_level CHECK (
        confidence_level IS NULL OR (confidence_level BETWEEN 1 AND 5)
    )
);

CREATE INDEX idx_quran_page_progress_user_id
    ON quran_page_progress (user_id);

CREATE INDEX idx_quran_page_progress_status
    ON quran_page_progress (status);

CREATE INDEX idx_quran_page_progress_updated_at
    ON quran_page_progress (updated_at);
