package com.thabat.quran.goal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuranDailyGoalRepository extends JpaRepository<QuranDailyGoal, UUID> {

    Optional<QuranDailyGoal> findByUserIdAndActiveTrue(UUID userId);

    Optional<QuranDailyGoal> findByUserId(UUID userId);
}
