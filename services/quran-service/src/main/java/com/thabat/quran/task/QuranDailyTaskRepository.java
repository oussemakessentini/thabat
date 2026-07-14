package com.thabat.quran.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuranDailyTaskRepository extends JpaRepository<QuranDailyTask, UUID> {

    List<QuranDailyTask> findByUserIdAndTaskDateOrderByTaskTypeAscPageNumberAsc(
            UUID userId,
            LocalDate taskDate
    );

    boolean existsByUserIdAndTaskDate(UUID userId, LocalDate taskDate);

    Optional<QuranDailyTask> findByIdAndUserId(UUID id, UUID userId);
}
