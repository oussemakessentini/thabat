package com.thabat.quran.page;

import com.thabat.quran.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "quran_page_progress",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_quran_page_progress_user_page",
                columnNames = {"user_id", "page_number"}
        )
)
public class QuranPageProgress extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "page_number", nullable = false)
    private int pageNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private QuranPageStatus status = QuranPageStatus.NOT_STARTED;

    @Column(name = "memorized_at")
    private LocalDate memorizedAt;

    @Column(name = "last_reviewed_at")
    private LocalDate lastReviewedAt;

    @Column(name = "successful_review_count", nullable = false)
    private int successfulReviewCount = 0;

    @Column(name = "confidence_level")
    private Integer confidenceLevel;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Version
    @Column(name = "version", nullable = false)
    private long version;
}
