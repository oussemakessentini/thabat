package com.thabat.journey.profile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JourneyProfileRepository extends JpaRepository<JourneyProfile, UUID> {

    Optional<JourneyProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
