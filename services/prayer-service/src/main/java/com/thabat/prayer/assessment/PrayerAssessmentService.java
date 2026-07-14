package com.thabat.prayer.assessment;

import com.thabat.prayer.assessment.dto.CreatePrayerAssessmentRequest;
import com.thabat.prayer.assessment.dto.PrayerAssessmentResponse;
import com.thabat.prayer.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrayerAssessmentService {

    private final PrayerAssessmentRepository prayerAssessmentRepository;
    private final PrayerAssessmentMapper prayerAssessmentMapper;

    @Transactional
    public PrayerAssessmentResponse createAssessment(
            UUID userId,
            CreatePrayerAssessmentRequest request
    ) {
        MissedPrayerCalculator.AssessmentResult result = MissedPrayerCalculator.calculate(
                new MissedPrayerCalculator.AssessmentInput(
                        request.missedYears(),
                        request.missedMonths(),
                        request.missedDays(),
                        request.dailyRecoveryTarget()
                )
        );

        PrayerAssessment saved = prayerAssessmentRepository.save(
                prayerAssessmentMapper.toEntity(userId, request, result)
        );

        return prayerAssessmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PrayerAssessmentResponse getLatestAssessment(UUID userId) {
        PrayerAssessment assessment = prayerAssessmentRepository
                .findFirstByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No prayer assessment was found for the current user"
                ));
        return prayerAssessmentMapper.toResponse(assessment);
    }
}
