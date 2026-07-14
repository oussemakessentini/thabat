package com.thabat.quran.goal;

import com.thabat.quran.common.exception.InvalidQuranProgressException;
import com.thabat.quran.common.exception.ResourceNotFoundException;
import com.thabat.quran.goal.dto.QuranDailyGoalResponse;
import com.thabat.quran.goal.dto.UpsertQuranDailyGoalRequest;
import com.thabat.quran.page.QuranPageConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuranDailyGoalService {

    private final QuranDailyGoalRepository repository;

    @Transactional
    public QuranDailyGoalResponse upsert(UUID userId, UpsertQuranDailyGoalRequest request) {
        validatePreferredPage(request.preferredStartPage());

        QuranDailyGoal goal = repository.findByUserId(userId).orElseGet(QuranDailyGoal::new);
        goal.setUserId(userId);
        goal.setMemorizationPagesPerDay(request.memorizationPagesPerDay());
        goal.setRevisionPagesPerDay(request.revisionPagesPerDay());
        goal.setPreferredStartPage(request.preferredStartPage());
        goal.setActive(true);

        return toResponse(repository.save(goal));
    }

    @Transactional(readOnly = true)
    public QuranDailyGoalResponse getActive(UUID userId) {
        return repository.findByUserIdAndActiveTrue(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Active Quran daily goal not found"));
    }

    @Transactional(readOnly = true)
    public QuranDailyGoal requireActiveGoal(UUID userId) {
        return repository.findByUserIdAndActiveTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Active Quran daily goal not found"));
    }

    private void validatePreferredPage(Integer preferredStartPage) {
        if (preferredStartPage != null
                && !QuranPageConstants.isValidPageNumber(preferredStartPage)) {
            throw new InvalidQuranProgressException(
                    "preferredStartPage must be between "
                            + QuranPageConstants.MIN_PAGE
                            + " and "
                            + QuranPageConstants.MAX_PAGE
            );
        }
    }

    private QuranDailyGoalResponse toResponse(QuranDailyGoal goal) {
        return new QuranDailyGoalResponse(
                goal.getId(),
                goal.getMemorizationPagesPerDay(),
                goal.getRevisionPagesPerDay(),
                goal.getPreferredStartPage(),
                goal.isActive()
        );
    }
}
