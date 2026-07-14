import React, { useCallback, useMemo, useState } from "react";
import {
    ActivityIndicator,
    Modal,
    Pressable,
    RefreshControl,
    ScrollView,
    StatusBar,
    StyleSheet,
    Text,
    View,
} from "react-native";

import type { AppStackScreenProps } from "../../../navigation/navigation.types";
import { parseApiError } from "../../../shared/api/parseApiError";
import { PrimaryButton } from "../../../shared/components/button/PrimaryButton";
import { ScreenContainer } from "../../../shared/components/layout/ScreenContainer";
import { colors, radius, spacing, typography } from "../../../shared/theme";
import { QuranDailyTaskCard } from "../components/QuranDailyTaskCard";
import {
    useCompleteQuranTask,
    useSkipQuranTask,
} from "../hooks/useQuranTaskMutations";
import { useQuranTodayTasks } from "../hooks/useQuranTodayTasks";
import type { QuranDailyTask } from "../types/quran.types";

type Props = AppStackScreenProps<"QuranToday">;

const CONFIDENCE_LEVELS = [1, 2, 3, 4, 5] as const;

export function QuranTodayScreen({ navigation }: Props): React.JSX.Element {
    const todayQuery = useQuranTodayTasks(true);
    const completeMutation = useCompleteQuranTask();
    const skipMutation = useSkipQuranTask();

    const [activeTask, setActiveTask] = useState<QuranDailyTask | null>(null);
    const [confidenceLevel, setConfidenceLevel] = useState<number>(4);
    const [successful, setSuccessful] = useState(true);
    const [actionError, setActionError] = useState<string | undefined>();

    const busy = completeMutation.isPending || skipMutation.isPending;

    const onRefresh = useCallback(() => {
        void todayQuery.refetch();
    }, [todayQuery]);

    const closeCompleteModal = (): void => {
        setActiveTask(null);
        setActionError(undefined);
        setConfidenceLevel(4);
        setSuccessful(true);
    };

    const submitComplete = async (): Promise<void> => {
        if (!activeTask || completeMutation.isPending) {
            return;
        }

        setActionError(undefined);
        try {
            await completeMutation.mutateAsync({
                taskId: activeTask.id,
                payload: {
                    confidenceLevel,
                    successful:
                        activeTask.taskType === "REVISION" ? successful : true,
                },
            });
            closeCompleteModal();
        } catch (error) {
            setActionError(parseApiError(error).message);
        }
    };

    const onSkip = async (task: QuranDailyTask): Promise<void> => {
        if (skipMutation.isPending) {
            return;
        }
        setActionError(undefined);
        try {
            await skipMutation.mutateAsync(task.id);
        } catch (error) {
            setActionError(parseApiError(error).message);
        }
    };

    const summary = todayQuery.data;
    const errorMessage = todayQuery.isError
        ? parseApiError(todayQuery.error).message
        : actionError;

    const sections = useMemo(() => {
        if (!summary) {
            return null;
        }
        return (
            <>
                <Text style={styles.sectionTitle}>Memorization</Text>
                {summary.memorizationTasks.length === 0 ? (
                    <Text style={styles.empty}>No memorization tasks today.</Text>
                ) : (
                    summary.memorizationTasks.map((task) => (
                        <QuranDailyTaskCard
                            key={task.id}
                            task={task}
                            busy={busy}
                            onOpenPage={(pageNumber) =>
                                navigation.navigate("QuranPageDetail", {
                                    pageNumber,
                                })
                            }
                            onComplete={setActiveTask}
                            onSkip={(t) => {
                                void onSkip(t);
                            }}
                        />
                    ))
                )}

                <Text style={styles.sectionTitle}>Revision</Text>
                {summary.revisionTasks.length === 0 ? (
                    <Text style={styles.empty}>No revision tasks today.</Text>
                ) : (
                    summary.revisionTasks.map((task) => (
                        <QuranDailyTaskCard
                            key={task.id}
                            task={task}
                            busy={busy}
                            onOpenPage={(pageNumber) =>
                                navigation.navigate("QuranPageDetail", {
                                    pageNumber,
                                })
                            }
                            onComplete={setActiveTask}
                            onSkip={(t) => {
                                void onSkip(t);
                            }}
                        />
                    ))
                )}
            </>
        );
    }, [summary, busy, navigation]);

    return (
        <ScreenContainer contentContainerStyle={styles.container}>
            <StatusBar barStyle="dark-content" />

            <View style={styles.header}>
                <Text style={styles.title}>Today&apos;s Quran</Text>
                <PrimaryButton
                    title="Back"
                    fullWidth={false}
                    onPress={() => navigation.goBack()}
                    style={styles.backButton}
                />
            </View>

            {todayQuery.isLoading ? (
                <ActivityIndicator color={colors.primary} style={styles.loader} />
            ) : todayQuery.isError ? (
                <View style={styles.stateBox}>
                    <Text style={styles.error}>{errorMessage}</Text>
                    <PrimaryButton title="Retry" onPress={onRefresh} />
                    <PrimaryButton
                        title="Edit daily goal"
                        onPress={() => navigation.navigate("QuranDailyGoal")}
                    />
                </View>
            ) : summary ? (
                <ScrollView
                    contentContainerStyle={styles.scroll}
                    refreshControl={
                        <RefreshControl
                            refreshing={todayQuery.isRefetching}
                            onRefresh={onRefresh}
                            tintColor={colors.primary}
                        />
                    }
                >
                    <Text style={styles.date}>{summary.date}</Text>
                    <Text style={styles.progress}>
                        {summary.completedTasks}/{summary.totalTasks} completed ·{" "}
                        {summary.completionPercentage.toFixed(0)}%
                    </Text>
                    <Text style={styles.counts}>
                        Pending {summary.pendingTasks} · Skipped{" "}
                        {summary.skippedTasks}
                    </Text>

                    {actionError && !activeTask ? (
                        <Text style={styles.error}>{actionError}</Text>
                    ) : null}

                    {sections}

                    <PrimaryButton
                        title="Edit daily goal"
                        onPress={() => navigation.navigate("QuranDailyGoal")}
                    />
                    <PrimaryButton
                        title="Open Quran tracker"
                        onPress={() => navigation.navigate("QuranTracker")}
                    />
                </ScrollView>
            ) : null}

            <Modal
                visible={activeTask != null}
                transparent
                animationType="fade"
                onRequestClose={closeCompleteModal}
            >
                <View style={styles.modalBackdrop}>
                    <View style={styles.modalCard}>
                        <Text style={styles.modalTitle}>Complete task</Text>
                        <Text style={styles.modalMeta}>
                            Page {activeTask?.pageNumber} ·{" "}
                            {activeTask?.taskType}
                        </Text>

                        <Text style={styles.modalLabel}>Confidence (1–5)</Text>
                        <View style={styles.chips}>
                            {CONFIDENCE_LEVELS.map((level) => (
                                <Pressable
                                    key={level}
                                    onPress={() => setConfidenceLevel(level)}
                                    style={[
                                        styles.chip,
                                        confidenceLevel === level
                                            && styles.chipSelected,
                                    ]}
                                >
                                    <Text
                                        style={[
                                            styles.chipText,
                                            confidenceLevel === level
                                                && styles.chipTextSelected,
                                        ]}
                                    >
                                        {level}
                                    </Text>
                                </Pressable>
                            ))}
                        </View>

                        {activeTask?.taskType === "REVISION" ? (
                            <>
                                <Text style={styles.modalLabel}>
                                    Was the revision successful?
                                </Text>
                                <View style={styles.chips}>
                                    <Pressable
                                        onPress={() => setSuccessful(true)}
                                        style={[
                                            styles.chip,
                                            successful && styles.chipSelected,
                                        ]}
                                    >
                                        <Text
                                            style={[
                                                styles.chipText,
                                                successful
                                                    && styles.chipTextSelected,
                                            ]}
                                        >
                                            Yes
                                        </Text>
                                    </Pressable>
                                    <Pressable
                                        onPress={() => setSuccessful(false)}
                                        style={[
                                            styles.chip,
                                            !successful && styles.chipSelected,
                                        ]}
                                    >
                                        <Text
                                            style={[
                                                styles.chipText,
                                                !successful
                                                    && styles.chipTextSelected,
                                            ]}
                                        >
                                            No
                                        </Text>
                                    </Pressable>
                                </View>
                            </>
                        ) : null}

                        {actionError ? (
                            <Text style={styles.error}>{actionError}</Text>
                        ) : null}

                        <PrimaryButton
                            title="Confirm"
                            loading={completeMutation.isPending}
                            disabled={completeMutation.isPending}
                            onPress={() => {
                                void submitComplete();
                            }}
                        />
                        <PrimaryButton
                            title="Cancel"
                            disabled={completeMutation.isPending}
                            onPress={closeCompleteModal}
                        />
                    </View>
                </View>
            </Modal>
        </ScreenContainer>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        paddingHorizontal: spacing.lg,
        paddingTop: spacing.md,
        paddingBottom: spacing.md,
    },

    header: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
        marginBottom: spacing.md,
        gap: spacing.sm,
    },

    title: {
        flex: 1,
        color: colors.primary,
        fontSize: typography.h3,
        fontWeight: "700",
    },

    backButton: {
        minHeight: 40,
        paddingHorizontal: spacing.md,
    },

    loader: {
        marginTop: spacing.xl,
    },

    stateBox: {
        gap: spacing.md,
        marginTop: spacing.lg,
    },

    scroll: {
        gap: spacing.md,
        paddingBottom: spacing.xl,
    },

    date: {
        color: colors.textPrimary,
        fontSize: typography.body,
        fontWeight: "600",
    },

    progress: {
        color: colors.primary,
        fontSize: typography.body,
        fontWeight: "700",
    },

    counts: {
        color: colors.textSecondary,
        fontSize: typography.caption,
    },

    sectionTitle: {
        color: colors.primary,
        fontSize: typography.body,
        fontWeight: "700",
        marginTop: spacing.sm,
    },

    empty: {
        color: colors.textSecondary,
        fontSize: typography.caption,
    },

    error: {
        color: colors.error,
        fontSize: typography.body,
        textAlign: "center",
    },

    modalBackdrop: {
        flex: 1,
        backgroundColor: "rgba(0,0,0,0.45)",
        justifyContent: "center",
        padding: spacing.lg,
    },

    modalCard: {
        backgroundColor: colors.surface,
        borderRadius: radius.md,
        padding: spacing.lg,
        gap: spacing.md,
    },

    modalTitle: {
        color: colors.primary,
        fontSize: typography.h3,
        fontWeight: "700",
    },

    modalMeta: {
        color: colors.textSecondary,
        fontSize: typography.caption,
    },

    modalLabel: {
        color: colors.textPrimary,
        fontSize: typography.caption,
        fontWeight: "600",
    },

    chips: {
        flexDirection: "row",
        flexWrap: "wrap",
        gap: spacing.xs,
    },

    chip: {
        minHeight: 36,
        minWidth: 40,
        paddingHorizontal: spacing.sm,
        borderRadius: radius.full,
        borderWidth: 1,
        borderColor: colors.border,
        backgroundColor: colors.surface,
        justifyContent: "center",
        alignItems: "center",
    },

    chipSelected: {
        backgroundColor: colors.primary,
        borderColor: colors.primary,
    },

    chipText: {
        color: colors.textSecondary,
        fontWeight: "600",
    },

    chipTextSelected: {
        color: colors.surface,
    },
});
