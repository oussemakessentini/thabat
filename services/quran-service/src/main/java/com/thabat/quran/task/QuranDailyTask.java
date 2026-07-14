package com.thabat.quran.task;

import com.thabat.quran.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "quran_daily_task",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_quran_daily_task_user_date_type_page",
                columnNames = {"user_id", "task_date", "task_type", "page_number"}
        )
)
public class QuranDailyTask extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "goal_id", nullable = false)
    private UUID goalId;

    @Column(name = "task_date", nullable = false)
    private LocalDate taskDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 32)
    private QuranTaskType taskType;

    @Column(name = "page_number", nullable = false)
    private int pageNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private QuranTaskStatus status = QuranTaskStatus.PENDING;

    @Column(name = "completed_at")
    private Instant completedAt;
}
