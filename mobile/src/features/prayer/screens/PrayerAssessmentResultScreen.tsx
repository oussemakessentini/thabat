import React from "react";
import { StatusBar, StyleSheet, Text, View } from "react-native";

import type { AppStackScreenProps } from "../../../navigation/navigation.types";
import { PrimaryButton } from "../../../shared/components/button/PrimaryButton";
import { ScreenContainer } from "../../../shared/components/layout/ScreenContainer";
import {
    colors,
    radius,
    spacing,
    typography,
} from "../../../shared/theme";

type Props = AppStackScreenProps<"PrayerAssessmentResult">;

export function PrayerAssessmentResultScreen({
    navigation,
    route,
}: Props): React.JSX.Element {
    const { assessment } = route.params;
    const remaining = assessment.remainingByPrayer;

    return (
        <ScreenContainer scrollable contentContainerStyle={styles.content}>
            <StatusBar barStyle="dark-content" />

            <View style={styles.header}>
                <Text style={styles.title}>Your recovery plan</Text>
                <Text style={styles.subtitle}>
                    Estimated missed days: {assessment.totalEstimatedDays}
                </Text>
            </View>

            <View style={styles.panel}>
                <Text style={styles.sectionTitle}>Remaining by prayer</Text>
                <Row label="Fajr" value={remaining.fajr} />
                <Row label="Dhuhr" value={remaining.dhuhr} />
                <Row label="Asr" value={remaining.asr} />
                <Row label="Maghrib" value={remaining.maghrib} />
                <Row label="Isha" value={remaining.isha} />
            </View>

            <View style={styles.panel}>
                <Row
                    label="Total remaining prayers"
                    value={assessment.totalRemainingPrayers}
                />
                <Row
                    label="Daily recovery target"
                    value={assessment.dailyRecoveryTarget}
                />
                <Row
                    label="Estimated completion (days)"
                    value={assessment.estimatedCompletionDays}
                />
            </View>

            <Text style={styles.disclaimer}>
                This calculation is an estimate for planning purposes and is not
                a religious ruling.
            </Text>

            <PrimaryButton
                title="Track progress"
                onPress={() => navigation.navigate("PrayerProgress")}
            />

            <PrimaryButton
                title="Back to Home"
                onPress={() => navigation.navigate("Home")}
            />
        </ScreenContainer>
    );
}

function Row({
    label,
    value,
}: {
    label: string;
    value: number;
}): React.JSX.Element {
    return (
        <View style={styles.row}>
            <Text style={styles.rowLabel}>{label}</Text>
            <Text style={styles.rowValue}>{value.toLocaleString()}</Text>
        </View>
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

    panel: {
        backgroundColor: colors.surface,
        borderRadius: radius.lg,
        borderWidth: 1,
        borderColor: colors.border,
        padding: spacing.md,
        gap: spacing.sm,
    },

    sectionTitle: {
        color: colors.textPrimary,
        fontSize: typography.body,
        fontWeight: "700",
        marginBottom: spacing.xs,
    },

    row: {
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "center",
        gap: spacing.md,
    },

    rowLabel: {
        flex: 1,
        color: colors.textSecondary,
        fontSize: typography.body,
    },

    rowValue: {
        color: colors.primaryDark,
        fontSize: typography.body,
        fontWeight: "700",
    },

    disclaimer: {
        color: colors.textSecondary,
        fontSize: typography.caption,
        lineHeight: 20,
        fontStyle: "italic",
    },
});
