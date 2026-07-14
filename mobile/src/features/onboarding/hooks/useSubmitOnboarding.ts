import { useMutation, useQueryClient } from "@tanstack/react-query";

import { submitOnboarding } from "../api/journey.api";
import type {
    JourneyProfile,
    OnboardingRequest,
} from "../types/onboarding.types";

import { journeyProfileQueryKey } from "./useJourneyProfile";

export function useSubmitOnboarding() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (payload: OnboardingRequest) => submitOnboarding(payload),
        onSuccess: (profile: JourneyProfile) => {
            queryClient.setQueryData(journeyProfileQueryKey, profile);
        },
    });
}
