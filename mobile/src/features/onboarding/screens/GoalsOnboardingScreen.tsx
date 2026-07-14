import React, { useState } from "react";
import { StatusBar, StyleSheet, Text, View } from "react-native";

import type { OnboardingStackScreenProps } from "../../../navigation/navigation.types";
import { PrimaryButton } from "../../../shared/components/button/PrimaryButton";
import { ScreenContainer } from "../../../shared/components/layout/ScreenContainer";
import {
    colors,
    spacing,
    typography,
} from "../../../shared/theme";
import { SecondaryButton } from "../components/SecondaryButton";
import { SelectionCard } from "../components/SelectionCard";
import { GOAL_OPTIONS } from "../constants/onboardingOptions";
import { useOnboardingDraft } from "../state/OnboardingContext";

type Props = OnboardingStackScreenProps<"GoalsOnboarding">;

export function GoalsOnboardingScreen({
    navigation,
}: Props): React.JSX.Element {
    const { draft, toggleGoal } = useOnboardingDraft();
    const [error, setError] = useState<string | undefined>();

    const handleContinue = (): void => {
        if (draft.selectedGoals.length === 0) {
            setError("Select at least one goal to continue.");
            return;
        }
        setError(undefined);
        navigation.navigate("LevelsOnboarding");
    };

    return (
        <ScreenContainer scrollable contentContainerStyle={styles.content}>
            <StatusBar barStyle="dark-content" />

            <View style={styles.header}>
                <Text style={styles.title}>What would you like to focus on?</Text>
                <Text style={styles.subtitle}>Choose one or more goals.</Text>
            </View>

            <View style={styles.options}>
                {GOAL_OPTIONS.map((option) => (
                    <SelectionCard
                        key={option.value}
                        label={option.label}
                        selected={draft.selectedGoals.includes(option.value)}
                        onPress={() => {
                            setError(undefined);
                            toggleGoal(option.value);
                        }}
                    />
                ))}
            </View>

            {error ? <Text style={styles.error}>{error}</Text> : null}

            <View style={styles.actions}>
                <PrimaryButton title="Continue" onPress={handleContinue} />
                <SecondaryButton
                    title="Back"
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
        gap: spacing.lg,
    },

    header: {
        gap: spacing.sm,
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
