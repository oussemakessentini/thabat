package com.thabat.quran.goal;

import com.thabat.quran.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "quran_daily_goal",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_quran_daily_goal_user",
                columnNames = {"user_id"}
        )
)
public class QuranDailyGoal extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "memorization_pages_per_day", nullable = false)
    private int memorizationPagesPerDay;

    @Column(name = "revision_pages_per_day", nullable = false)
    private int revisionPagesPerDay;

    @Column(name = "preferred_start_page")
    private Integer preferredStartPage;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
