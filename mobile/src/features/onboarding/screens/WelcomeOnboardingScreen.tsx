import React from "react";
import { StatusBar, StyleSheet, Text, View } from "react-native";

import type { OnboardingStackScreenProps } from "../../../navigation/navigation.types";
import { PrimaryButton } from "../../../shared/components/button/PrimaryButton";
import { ScreenContainer } from "../../../shared/components/layout/ScreenContainer";
import {
    colors,
    spacing,
    typography,
} from "../../../shared/theme";

type Props = OnboardingStackScreenProps<"WelcomeOnboarding">;

export function WelcomeOnboardingScreen({
    navigation,
}: Props): React.JSX.Element {
    return (
        <ScreenContainer contentContainerStyle={styles.content}>
            <StatusBar barStyle="dark-content" />

            <View style={styles.copy}>
                <Text style={styles.eyebrow}>Thabat</Text>
                <Text style={styles.title}>Let’s personalize your journey</Text>
                <Text style={styles.subtitle}>
                    A few calm questions help us tailor prayer, Quran, and
                    reminders. You can change these settings later anytime.
                </Text>
            </View>

            <PrimaryButton
                title="Continue"
                onPress={() => navigation.navigate("GoalsOnboarding")}
            />
        </ScreenContainer>
    );
}

const styles = StyleSheet.create({
    content: {
        flexGrow: 1,
        justifyContent: "space-between",
        paddingHorizontal: spacing.lg,
        paddingVertical: spacing.xl,
        gap: spacing.xl,
    },

    copy: {
        flexGrow: 1,
        justifyContent: "center",
        gap: spacing.md,
    },

    eyebrow: {
        color: colors.secondary,
        fontSize: typography.caption,
        fontWeight: "700",
        letterSpacing: 1,
        textAlign: "center",
        textTransform: "uppercase",
    },

    title: {
        color: colors.primary,
        fontSize: typography.h1,
        fontWeight: "700",
        textAlign: "center",
    },

    subtitle: {
        color: colors.textSecondary,
        fontSize: typography.body,
        lineHeight: 24,
        textAlign: "center",
    },
});
