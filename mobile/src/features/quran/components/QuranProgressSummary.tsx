import React from "react";
import { StyleSheet, Text, View } from "react-native";

import { colors, spacing, typography } from "../../../shared/theme";
import type { QuranProgressSummary as QuranProgressSummaryData } from "../types/quran.types";

type Props = {
    summary?: QuranProgressSummaryData;
};

export function QuranProgressSummary({
    summary,
}: Props): React.JSX.Element {
    if (!summary) {
        return (
            <View style={styles.card}>
                <Text style={styles.title}>Quran memorization</Text>
                <Text style={styles.meta}>Loading progress…</Text>
            </View>
        );
    }

    return (
        <View style={styles.card}>
            <Text style={styles.title}>Quran memorization</Text>
            <Text style={styles.meta}>
                {summary.completedPages} of {summary.totalPages} completed (
                {summary.completionPercentage.toFixed(2)}%)
            </Text>
            <Text style={styles.meta}>
                Learning {summary.learningPages} · Needs revision{" "}
                {summary.needsRevisionPages} · Strong {summary.strongPages}
            </Text>
            <Text style={styles.meta}>
                Reviewed this week: {summary.reviewedThisWeek}
            </Text>
        </View>
    );
}

const styles = StyleSheet.create({
    card: {
        gap: spacing.xs,
        width: "100%",
        marginBottom: spacing.md,
    },

    title: {
        color: colors.primary,
        fontSize: typography.body,
        fontWeight: "700",
    },

    meta: {
        color: colors.textSecondary,
        fontSize: typography.caption,
    },
});
