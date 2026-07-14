import React, { useState } from "react";
import { StatusBar, StyleSheet, Text, View } from "react-native";

import type { OnboardingStackScreenProps } from "../../../navigation/navigation.types";
import { parseApiError } from "../../../shared/api/parseApiError";
import { PrimaryButton } from "../../../shared/components/button/PrimaryButton";
import { ScreenContainer } from "../../../shared/components/layout/ScreenContainer";
import {
    colors,
    spacing,
    typography,
} from "../../../shared/theme";
import { SecondaryButton } from "../components/SecondaryButton";
import { SelectionCard } from "../components/SelectionCard";
import {
    EXPERIENCE_MODE_OPTIONS,
    REMINDER_OPTIONS,
} from "../constants/onboardingOptions";
import { useSubmitOnboarding } from "../hooks/useSubmitOnboarding";
import { useOnboardingDraft } from "../state/OnboardingContext";
import type { OnboardingRequest } from "../types/onboarding.types";

type Props = OnboardingStackScreenProps<"PreferencesOnboarding">;

export function PreferencesOnboardingScreen({
    navigation,
}: Props): React.JSX.Element {
    const {
        draft,
        setExperienceMode,
        setReminderPreference,
        resetDraft,
    } = useOnboardingDraft();
    const { mutateAsync, isPending } = useSubmitOnboarding();
    const [error, setError] = useState<string | undefined>();

    const handleSubmit = async (): Promise<void> => {
        if (isPending) {
            return;
        }

        if (!draft.experienceMode || !draft.reminderPreference) {
            setError("Select an experience mode and a reminder preference.");
            return;
        }

        if (
            draft.selectedGoals.length === 0
            || !draft.prayerLevel
            || !draft.quranLevel
        ) {
            setError("Please go back and complete the previous steps.");
            return;
        }

        const payload: OnboardingRequest = {
            experienceMode: draft.experienceMode,
            selectedGoals: draft.selectedGoals,
            prayerLevel: draft.prayerLevel,
            quranLevel: draft.quranLevel,
            reminderPreference: draft.reminderPreference,
        };

        setError(undefined);

        try {
            await mutateAsync(payload);
            resetDraft();
            // AppNavigator switches to AppStack/Home when the profile cache
            // shows onboardingCompleted === true.
        } catch (submitError) {
            const parsed = parseApiError(submitError);
            setError(parsed.message);
        }
    };

    return (
        <ScreenContainer scrollable contentContainerStyle={styles.content}>
            <StatusBar barStyle="dark-content" />

            <View style={styles.header}>
                <Text style={styles.title}>Almost done</Text>
                <Text style={styles.subtitle}>
                    Choose how Thabat should feel day to day.
                </Text>
            </View>

            <Text style={styles.sectionTitle}>Experience mode</Text>
            <View style={styles.options}>
                {EXPERIENCE_MODE_OPTIONS.map((option) => (
                    <SelectionCard
                        key={option.value}
                        label={option.label}
                        selected={draft.experienceMode === option.value}
                        onPress={() => {
                            setError(undefined);
                            setExperienceMode(option.value);
                        }}
                    />
                ))}
            </View>

            <Text style={styles.sectionTitle}>Reminders</Text>
            <View style={styles.options}>
                {REMINDER_OPTIONS.map((option) => (
                    <SelectionCard
                        key={option.value}
                        label={option.label}
                        selected={draft.reminderPreference === option.value}
                        onPress={() => {
                            setError(undefined);
                            setReminderPreference(option.value);
                        }}
                    />
                ))}
            </View>

            {error ? <Text style={styles.error}>{error}</Text> : null}

            <View style={styles.actions}>
                <PrimaryButton
                    title="Start my journey"
                    loading={isPending}
                    disabled={isPending}
                    onPress={handleSubmit}
                />
                <SecondaryButton
                    title="Back"
                    disabled={isPending}
                    onPress={() => navigation.goBack()}
                />
            </View>
        </ScreenContainer>
    );
}

const styles = StyleSheet.create({
    content: {
        flexGrow: 1,
        paddingHorizontal: spacing.lg,
        paddingVertical: spacing.xl,
        gap: spacing.md,
    },

    header: {
        gap: spacing.sm,
        marginBottom: spacing.sm,
    },

    title: {
        color: colors.primary,
        fontSize: typography.h2,
        fontWeight: "700",
    },

    subtitle: {
        color: colors.textSecondary,
        fontSize: typography.body,
    },

    sectionTitle: {
        color: colors.textPrimary,
        fontSize: typography.body,
        fontWeight: "700",
        marginTop: spacing.sm,
    },

    options: {
        gap: spacing.sm,
    },

    error: {
        color: colors.error,
        fontSize: typography.caption,
    },

    actions: {
        gap: spacing.sm,
        marginTop: spacing.sm,
    },
});
