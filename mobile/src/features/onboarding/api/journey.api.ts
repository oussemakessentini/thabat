import { apiClient } from "../../../shared/api/apiClient";

import type {
    JourneyProfile,
    OnboardingRequest,
} from "../types/onboarding.types";

export async function submitOnboarding(
    payload: OnboardingRequest,
): Promise<JourneyProfile> {
    const { data } = await apiClient.post<JourneyProfile>(
        "/api/v1/journey/onboarding",
        payload,
    );
    return data;
}

export async function getJourneyProfile(): Promise<JourneyProfile> {
    const { data } = await apiClient.get<JourneyProfile>(
        "/api/v1/journey/profile",
    );
    return data;
}
