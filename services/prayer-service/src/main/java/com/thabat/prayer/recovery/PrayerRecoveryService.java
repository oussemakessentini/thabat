package com.thabat.prayer.recovery;

import com.thabat.prayer.assessment.PrayerAssessment;
import com.thabat.prayer.assessment.PrayerAssessmentRepository;
import com.thabat.prayer.common.exception.InvalidRecoveryException;
import com.thabat.prayer.common.exception.ResourceNotFoundException;
import com.thabat.prayer.recovery.dto.CompleteNextPrayerRequest;
import com.thabat.prayer.recovery.dto.CompleteNextPrayerResponse;
import com.thabat.prayer.recovery.dto.PrayerProgressResponse;
import com.thabat.prayer.recovery.dto.RecoveryHistoryItemResponse;
import com.thabat.prayer.recovery.dto.UndoLatestRecoveryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrayerRecoveryService {

    private final PrayerRecoveryEntryRepository recoveryEntryRepository;
    private final PrayerAssessmentRepository assessmentRepository;
    private final Clock clock;

    @Transactional
    public CompleteNextPrayerResponse completeNext(
            UUID userId,
            CompleteNextPrayerRequest request
    ) {
        PrayerAssessment assessment = assessmentRepository
                .findByIdAndUserIdForUpdate(request.assessmentId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prayer assessment was not found for the current user"
                ));

        validateCompletedOn(request.completedOn());

        long completedCount = recoveryEntryRepository.countByUserIdAndAssessmentId(
                userId,
                assessment.getId()
        );
        long originalTotal = assessment.getTotalRemainingPrayers();
        if (completedCount >= originalTotal) {
            throw new InvalidRecoveryException(
                    "All missed prayers for this assessment are already completed"
            );
        }

        long sequenceNumber = SequentialPrayerOrder.nextSequenceNumber(completedCount);
        PrayerType prayerType = SequentialPrayerOrder.prayerForSequence(sequenceNumber);

        try {
            recoveryEntryRepository.saveAndFlush(
                    PrayerRecoveryEntry.builder()
                            .userId(userId)
                            .assessmentId(assessment.getId())
                            .prayerType(prayerType)
                            .sequenceNumber(sequenceNumber)
                            .completedOn(request.completedOn())
                            .build()
            );
        } catch (DataIntegrityViolationException exception) {
            throw new InvalidRecoveryException(
                    "Another completion was already recorded for this sequence. Please retry."
            );
        }

        PrayerProgressResponse progress = buildProgress(assessment, completedCount + 1L);
        return new CompleteNextPrayerResponse(
                prayerType,
                progress.nextPrayer(),
                progress.completedCycles(),
                progress.completedPrayersInCurrentCycle(),
                progress.totalCompletedPrayers(),
                progress.totalRemainingPrayers(),
                progress.totalRecoveryCycles(),
                progress.currentCycleNumber(),
                progress.progressPercentage()
        );
    }

    @Transactional
    public PrayerProgressResponse undoLatest(UUID userId, UndoLatestRecoveryRequest request) {
        PrayerAssessment assessment = assessmentRepository
                .findByIdAndUserIdForUpdate(request.assessmentId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prayer assessment was not found for the current user"
                ));

        PrayerRecoveryEntry latest = recoveryEntryRepository
                .findFirstByUserIdAndAssessmentIdOrderBySequenceNumberDesc(
                        userId,
                        assessment.getId()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No recovery entry exists to undo"
                ));

        recoveryEntryRepository.delete(latest);
        recoveryEntryRepository.flush();

        long remainingCount = recoveryEntryRepository.countByUserIdAndAssessmentId(
                userId,
                assessment.getId()
        );
        return buildProgress(assessment, remainingCount);
    }

    @Transactional(readOnly = true)
    public PrayerProgressResponse getProgress(UUID userId) {
        PrayerAssessment assessment = assessmentRepository
                .findFirstByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No prayer assessment was found for the current user"
                ));

        long completedCount = recoveryEntryRepository.countByUserIdAndAssessmentId(
                userId,
                assessment.getId()
        );
        return buildProgress(assessment, completedCount);
    }

    @Transactional(readOnly = true)
    public List<RecoveryHistoryItemResponse> getHistory(UUID userId) {
        PrayerAssessment assessment = assessmentRepository
                .findFirstByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No prayer assessment was found for the current user"
                ));

        return recoveryEntryRepository
                .findByUserIdAndAssessmentIdOrderBySequenceNumberDesc(
                        userId,
                        assessment.getId()
                )
                .stream()
                .map(entry -> new RecoveryHistoryItemResponse(
                        entry.getId(),
                        entry.getPrayerType(),
                        entry.getSequenceNumber(),
                        entry.getCompletedOn(),
                        entry.getCreatedAt()
                ))
                .toList();
    }

    private PrayerProgressResponse buildProgress(
            PrayerAssessment assessment,
            long completedCount
    ) {
        return PrayerProgressCalculator.calculate(assessment, completedCount);
    }

    private void validateCompletedOn(LocalDate completedOn) {
        LocalDate today = LocalDate.now(clock);
        if (completedOn.isAfter(today)) {
            throw new InvalidRecoveryException("Completion date cannot be in the future");
        }
    }
}
