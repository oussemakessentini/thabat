import React, { useState } from "react";
import {
    ActivityIndicator,
    Alert,
    Pressable,
    StatusBar,
    StyleSheet,
    Text,
    View,
} from "react-native";

import type { AppStackScreenProps } from "../../../navigation/navigation.types";
import { parseApiError } from "../../../shared/api/parseApiError";
import { formatDateForApi } from "../../auth/utils/date.utils";
import { PrimaryButton } from "../../../shared/components/button/PrimaryButton";
import { ScreenContainer } from "../../../shared/components/layout/ScreenContainer";
import {
    colors,
    radius,
    spacing,
    typography,
} from "../../../shared/theme";
import { useCompleteNextPrayer } from "../hooks/useCompleteNextPrayer";
import {
    isPrayerProgressMissing,
    usePrayerProgress,
} from "../hooks/usePrayerProgress";
import { useUndoLatestPrayer } from "../hooks/useUndoLatestPrayer";
import type { CyclePrayerStatus, PrayerType } from "../types/prayer.types";

type Props = AppStackScreenProps<"PrayerProgress">;

const CYCLE_ROWS: { key: keyof PrayerProgressCycle; label: string }[] = [
    { key: "fajr", label: "Fajr" },
    { key: "dhuhr", label: "Dhuhr" },
    { key: "asr", label: "Asr" },
    { key: "maghrib", label: "Maghrib" },
    { key: "isha", label: "Isha" },
];

type PrayerProgressCycle = {
    fajr: CyclePrayerStatus;
    dhuhr: CyclePrayerStatus;
    asr: CyclePrayerStatus;
    maghrib: CyclePrayerStatus;
    isha: CyclePrayerStatus;
};

const PRAYER_LABELS: Record<PrayerType, string> = {
    FAJR: "Fajr",
    DHUHR: "Dhuhr",
    ASR: "Asr",
    MAGHRIB: "Maghrib",
    ISHA: "Isha",
};

const STATUS_LABELS: Record<CyclePrayerStatus, string> = {
    COMPLETED: "Completed",
    NEXT: "Next",
    LOCKED: "Locked",
};

export function PrayerProgressScreen({
    navigation,
}: Props): React.JSX.Element {
    const { data, isLoading, isError, error, refetch } = usePrayerProgress();
    const completeMutation = useCompleteNextPrayer();
    const undoMutation = useUndoLatestPrayer();
    const [successMessage, setSuccessMessage] = useState<string | undefined>();
    const [actionError, setActionError] = useState<string | undefined>();

    if (isLoading) {
        return (
            <View style={styles.centered}>
                <ActivityIndicator color={colors.primary} size="large" />
            </View>
        );
    }

    if (isError && isPrayerProgressMissing(error)) {
        return (
            <ScreenContainer contentContainerStyle={styles.content}>
                <Text style={styles.title}>No assessment yet</Text>
                <PrimaryButton
                    title="Set up prayer recovery"
                    onPress={() => navigation.navigate("PrayerAssessment")}
                />
            </ScreenContainer>
        );
    }

    if (isError || !data) {
        return (
            <ScreenContainer contentContainerStyle={styles.content}>
                <Text style={styles.title}>Unable to load progress</Text>
                <Text style={styles.subtitle}>{parseApiError(error).message}</Text>
                <PrimaryButton title="Try again" onPress={() => void refetch()} />
            </ScreenContainer>
        );
    }

    const isBusy = completeMutation.isPending || undoMutation.isPending;
    const canUndo = data.totalCompletedPrayers > 0;
    const completeLabel = data.nextPrayer
        ? `Complete ${PRAYER_LABELS[data.nextPrayer]}`
        : "All prayers completed";

    const handleComplete = async (): Promise<void> => {
        if (isBusy || !data.nextPrayer) {
            return;
        }
        setActionError(undefined);
        setSuccessMessage(undefined);
        try {
            const response = await completeMutation.mutateAsync({
                assessmentId: data.assessmentId,
                completedOn: formatDateForApi(new Date()),
            });
            setSuccessMessage(
                `Completed ${PRAYER_LABELS[response.completedPrayer]}`,
            );
        } catch (completeError) {
            setActionError(parseApiError(completeError).message);
        }
    };

    const handleUndo = (): void => {
        if (!canUndo || isBusy) {
            return;
        }
        Alert.alert(
            "Undo last prayer?",
            "Only the most recent completed prayer can be undone to keep the order.",
            [
                { text: "Cancel", style: "cancel" },
                {
                    text: "Undo",
                    style: "destructive",
                    onPress: () => {
                        void (async () => {
                            setActionError(undefined);
                            setSuccessMessage(undefined);
                            try {
                                await undoMutation.mutateAsync(data.assessmentId);
                                setSuccessMessage("Last prayer undone");
                            } catch (undoError) {
                                setActionError(parseApiError(undoError).message);
                            }
                        })();
                    },
                },
            ],
        );
    };

    return (
        <ScreenContainer scrollable contentContainerStyle={styles.content}>
            <StatusBar barStyle="dark-content" />

            <Text style={styles.title}>Prayer recovery</Text>
            <Text style={styles.cycle}>
                Cycle {data.currentCycleNumber} of {data.totalRecoveryCycles}
            </Text>
            <Text style={styles.percent}>
                {Number(data.progressPercentage).toFixed(2)}%
            </Text>
            <Text style={styles.subtitle}>
                {data.totalCompletedPrayers.toLocaleString()} completed ·{" "}
                {data.totalRemainingPrayers.toLocaleString()} remaining
            </Text>

            <View style={styles.panel}>
                {CYCLE_ROWS.map((row) => {
                    const status = data.currentCycle[row.key];
                    return (
                        <View key={row.key} style={styles.row}>
                            <Text style={styles.rowLabel}>{row.label}</Text>
                            <Text
                                style={[
                                    styles.status,
                                    status === "COMPLETED" && styles.statusDone,
                                    status === "NEXT" && styles.statusNext,
                                    status === "LOCKED" && styles.statusLocked,
                                ]}
                            >
                                {STATUS_LABELS[status]}
                            </Text>
                        </View>
                    );
                })}
            </View>

            <Text style={styles.target}>
                Daily recovery target: {data.dailyRecoveryTarget} prayers
            </Text>
            <Text style={styles.target}>
                Estimated time remaining: {data.estimatedRemainingDays} days
            </Text>

            {successMessage ? (
                <Text style={styles.success}>{successMessage}</Text>
            ) : null}
            {actionError ? <Text style={styles.error}>{actionError}</Text> : null}

            <PrimaryButton
                title={completeLabel}
                loading={completeMutation.isPending}
                disabled={isBusy || !data.nextPrayer}
                onPress={handleComplete}
            />

            <PrimaryButton
                title="Undo last prayer"
                loading={undoMutation.isPending}
                disabled={isBusy || !canUndo}
                onPress={handleUndo}
            />

            <Pressable
                accessibilityRole="button"
                onPress={() => navigation.navigate("RecoveryHistory")}
            >
                <Text style={styles.link}>View history</Text>
            </Pressable>
        </ScreenContainer>
    );
}

const styles = StyleSheet.create({
    centered: {
        flex: 1,
        alignItems: "center",
        justifyContent: "center",
        backgroundColor: colors.background,
    },

    content: {
        flexGrow: 1,
        paddingHorizontal: spacing.lg,
        paddingVertical: spacing.xl,
        gap: spacing.md,
    },

    title: {
        color: colors.primary,
        fontSize: typography.h2,
        fontWeight: "700",
    },

    cycle: {
        color: colors.textPrimary,
        fontSize: typography.body,
        fontWeight: "700",
    },

    percent: {
        color: colors.secondary,
        fontSize: typography.h1,
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

    row: {
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "center",
    },

    rowLabel: {
        color: colors.textPrimary,
        fontSize: typography.body,
        fontWeight: "600",
    },

    status: {
        fontSize: typography.caption,
        fontWeight: "700",
    },

    statusDone: {
        color: colors.success,
    },

    statusNext: {
        color: colors.secondary,
    },

    statusLocked: {
        color: colors.textSecondary,
    },

    target: {
        color: colors.textPrimary,
        fontSize: typography.body,
        fontWeight: "600",
    },

    success: {
        color: colors.success,
        fontSize: typography.caption,
        fontWeight: "600",
    },

    error: {
        color: colors.error,
        fontSize: typography.caption,
    },

    link: {
        color: colors.primary,
        fontSize: typography.body,
        fontWeight: "700",
        textAlign: "center",
        paddingVertical: spacing.sm,
    },
});
