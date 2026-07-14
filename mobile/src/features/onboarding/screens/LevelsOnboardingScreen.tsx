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
import {
    PRAYER_LEVEL_OPTIONS,
    QURAN_LEVEL_OPTIONS,
} from "../constants/onboardingOptions";
import { useOnboardingDraft } from "../state/OnboardingContext";

type Props = OnboardingStackScreenProps<"LevelsOnboarding">;

export function LevelsOnboardingScreen({
    navigation,
}: Props): React.JSX.Element {
    const { draft, setPrayerLevel, setQuranLevel } = useOnboardingDraft();
    const [error, setError] = useState<string | undefined>();

    const handleContinue = (): void => {
        if (!draft.prayerLevel || !draft.quranLevel) {
            setError("Select both a prayer level and a Quran level.");
            return;
        }
        setError(undefined);
        navigation.navigate("PreferencesOnboarding");
    };

    return (
        <ScreenContainer scrollable contentContainerStyle={styles.content}>
            <StatusBar barStyle="dark-content" />

            <View style={styles.header}>
                <Text style={styles.title}>Where are you starting from?</Text>
                <Text style={styles.subtitle}>
                    This helps set a pace that fits you.
                </Text>
            </View>

            <Text style={styles.sectionTitle}>Prayer</Text>
            <View style={styles.options}>
                {PRAYER_LEVEL_OPTIONS.map((option) => (
                    <SelectionCard
                        key={option.value}
                        label={option.label}
                        selected={draft.prayerLevel === option.value}
                        onPress={() => {
                            setError(undefined);
                            setPrayerLevel(option.value);
                        }}
                    />
                ))}
            </View>

            <Text style={styles.sectionTitle}>Quran</Text>
            <View style={styles.options}>
                {QURAN_LEVEL_OPTIONS.map((option) => (
                    <SelectionCard
                        key={option.value}
                        label={option.label}
                        selected={draft.quranLevel === option.value}
                        onPress={() => {
                            setError(undefined);
                            setQuranLevel(option.value);
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
