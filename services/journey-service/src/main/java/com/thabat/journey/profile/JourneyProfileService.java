package com.thabat.journey.profile;

import com.thabat.journey.common.exception.ResourceNotFoundException;
import com.thabat.journey.profile.dto.JourneyProfileResponse;
import com.thabat.journey.profile.dto.OnboardingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JourneyProfileService {

    private final JourneyProfileRepository journeyProfileRepository;
    private final JourneyProfileMapper journeyProfileMapper;

    public record OnboardingResult(JourneyProfileResponse profile, boolean created) {
    }

    @Transactional
    public OnboardingResult completeOnboarding(UUID userId, OnboardingRequest request) {
        return journeyProfileRepository.findByUserId(userId)
                .map(existing -> {
                    journeyProfileMapper.applyOnboarding(existing, request);
                    JourneyProfile saved = journeyProfileRepository.save(existing);
                    return new OnboardingResult(journeyProfileMapper.toResponse(saved), false);
                })
                .orElseGet(() -> {
                    JourneyProfile created = journeyProfileRepository.save(
                            journeyProfileMapper.toNewProfile(userId, request)
                    );
                    return new OnboardingResult(journeyProfileMapper.toResponse(created), true);
                });
    }

    @Transactional(readOnly = true)
    public JourneyProfileResponse getProfile(UUID userId) {
        JourneyProfile profile = journeyProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Journey profile was not found for the current user"
                ));
        return journeyProfileMapper.toResponse(profile);
    }
}
