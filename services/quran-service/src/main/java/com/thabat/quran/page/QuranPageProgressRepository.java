package com.thabat.quran.page;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuranPageProgressRepository extends JpaRepository<QuranPageProgress, UUID> {

    List<QuranPageProgress> findByUserIdOrderByPageNumberAsc(UUID userId);

    Optional<QuranPageProgress> findByUserIdAndPageNumber(UUID userId, int pageNumber);

    List<QuranPageProgress> findByUserIdAndStatusOrderByPageNumberAsc(
            UUID userId,
            QuranPageStatus status
    );

    long countByUserIdAndStatus(UUID userId, QuranPageStatus status);

    long countByUserIdAndLastReviewedAtBetween(
            UUID userId,
            java.time.LocalDate startInclusive,
            java.time.LocalDate endInclusive
    );

    Optional<QuranPageProgress> findFirstByUserIdOrderByUpdatedAtDesc(UUID userId);

    boolean existsByUserIdAndPageNumber(UUID userId, int pageNumber);
}
