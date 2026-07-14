import React, { useEffect } from "react";
import { ActivityIndicator, StyleSheet, View } from "react-native";
import { useQueryClient } from "@tanstack/react-query";

import {
    clearJourneyProfileCache,
    isJourneyProfileMissing,
    useJourneyProfile,
} from "./features/onboarding/hooks/useJourneyProfile";
import { useAuth } from "./shared/auth/AuthContext";
import { colors } from "./shared/theme";

export type AppReadyState =
    | { status: "loading" }
    | { status: "unauthenticated" }
    | { status: "onboarding" }
    | { status: "ready" };

/**
 * Gates the app on auth restore + journey profile so Home does not flash
 * before onboarding status is known.
 */
export function useAppReadyState(): AppReadyState {
    const { isAuthenticated, isInitializing } = useAuth();
    const queryClient = useQueryClient();

    const profileQuery = useJourneyProfile(isAuthenticated && !isInitializing);

    useEffect(() => {
        if (!isAuthenticated) {
            clearJourneyProfileCache(queryClient);
        }
    }, [isAuthenticated, queryClient]);

    if (isInitializing) {
        return { status: "loading" };
    }

    if (!isAuthenticated) {
        return { status: "unauthenticated" };
    }

    // isLoading = no cached data yet (avoids spinner flash on background refetch)
    if (profileQuery.isLoading) {
        return { status: "loading" };
    }

    if (profileQuery.isSuccess && profileQuery.data.onboardingCompleted) {
        return { status: "ready" };
    }

    if (profileQuery.isError && isJourneyProfileMissing(profileQuery.error)) {
        return { status: "onboarding" };
    }

    if (profileQuery.isSuccess && !profileQuery.data.onboardingCompleted) {
        return { status: "onboarding" };
    }

    // Non-404 errors: keep spinner. Auth 401 uses existing refresh/logout.
    if (profileQuery.isError) {
        return { status: "loading" };
    }

    return { status: "loading" };
}

export function AppBootstrapLoading(): React.JSX.Element {
    return (
        <View style={styles.loading}>
            <ActivityIndicator color={colors.primary} size="large" />
        </View>
    );
}

const styles = StyleSheet.create({
    loading: {
        flex: 1,
        alignItems: "center",
        justifyContent: "center",
        backgroundColor: colors.background,
    },
});
