import React from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";

import { PrimaryButton } from "../../../shared/components/button/PrimaryButton";
import { colors, radius, spacing, typography } from "../../../shared/theme";
import type { QuranDailyTask } from "../types/quran.types";
import {
    QURAN_TASK_STATUS_LABELS,
    QURAN_TASK_TYPE_LABELS,
} from "../types/quran.types";

type Props = {
    task: QuranDailyTask;
    onOpenPage: (pageNumber: number) => void;
    onComplete: (task: QuranDailyTask) => void;
    onSkip: (task: QuranDailyTask) => void;
    busy?: boolean;
};

export function QuranDailyTaskCard({
    task,
    onOpenPage,
    onComplete,
    onSkip,
    busy = false,
}: Props): React.JSX.Element {
    const pending = task.status === "PENDING";

    return (
        <View style={styles.card}>
            <Pressable onPress={() => onOpenPage(task.pageNumber)}>
                <Text style={styles.title}>Page {task.pageNumber}</Text>
                <Text style={styles.meta}>
                    {QURAN_TASK_TYPE_LABELS[task.taskType]} ·{" "}
                    {QURAN_TASK_STATUS_LABELS[task.status]}
                </Text>
            </Pressable>

            {pending ? (
                <View style={styles.actions}>
                    <PrimaryButton
                        title="Complete"
                        onPress={() => onComplete(task)}
                        disabled={busy}
                        fullWidth={false}
                        style={styles.actionButton}
                    />
                    <PrimaryButton
                        title="Skip"
                        onPress={() => onSkip(task)}
                        disabled={busy}
                        fullWidth={false}
                        style={styles.skipButton}
                    />
                </View>
            ) : null}
        </View>
    );
}

const styles = StyleSheet.create({
    card: {
        borderWidth: 1,
        borderColor: colors.border,
        borderRadius: radius.md,
        backgroundColor: colors.surface,
        padding: spacing.md,
        gap: spacing.sm,
    },

    title: {
        color: colors.primary,
        fontSize: typography.body,
        fontWeight: "700",
    },

    meta: {
        color: colors.textSecondary,
        fontSize: typography.caption,
        marginTop: spacing.xs,
    },

    actions: {
        flexDirection: "row",
        gap: spacing.sm,
        marginTop: spacing.xs,
    },

    actionButton: {
        minHeight: 40,
        paddingHorizontal: spacing.md,
    },

    skipButton: {
        minHeight: 40,
        paddingHorizontal: spacing.md,
        backgroundColor: colors.textSecondary,
    },
});
