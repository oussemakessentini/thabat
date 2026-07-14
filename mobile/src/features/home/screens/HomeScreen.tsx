import React, { useState } from "react";
import {
    ActivityIndicator,
    StatusBar,
    StyleSheet,
    Text,
    View,
} from "react-native";
import axios from "axios";

import type { AppStackScreenProps } from "../../../navigation/navigation.types";
import { useAuth } from "../../../shared/auth/AuthContext";
import { PrimaryButton } from "../../../shared/components/button/PrimaryButton";
import { ScreenContainer } from "../../../shared/components/layout/ScreenContainer";
import {
    colors,
    spacing,
    typography,
} from "../../../shared/theme";
import {
    isPrayerProgressMissing,
    usePrayerProgress,
} from "../../prayer/hooks/usePrayerProgress";
import type { PrayerType } from "../../prayer/types/prayer.types";
import { useUnreadNotificationCount } from "../../notifications/hooks/useUnreadNotificationCount";
import {
    isQuranDailyGoalMissing,
    useQuranDailyGoal,
} from "../../quran/hooks/useQuranDailyGoal";
import { useQuranTodayTasks } from "../../quran/hooks/useQuranTodayTasks";

type Props = AppStackScreenProps<"Home">;

const PRAYER_LABELS: Record<PrayerType, string> = {
    FAJR: "Fajr",
    DHUHR: "Dhuhr",
    ASR: "Asr",
    MAGHRIB: "Maghrib",
    ISHA: "Isha",
};

export function HomeScreen({ navigation }: Props): React.JSX.Element {
    const { user, signOut } = useAuth();
    const [isLoggingOut, setIsLoggingOut] = useState(false);
    const progressQuery = usePrayerProgress(true);
    const goalQuery = useQuranDailyGoal(true);
    const hasDailyGoal = goalQuery.isSuccess && goalQuery.data !== undefined;
    const missingDailyGoal =
        goalQuery.isError && isQuranDailyGoalMissing(goalQuery.error);
    const todayQuery = useQuranTodayTasks(hasDailyGoal);
    const unreadQuery = useUnreadNotificationCount(true);
    const unreadCount = unreadQuery.data?.unreadCount ?? 0;

    const handleLogout = async (): Promise<void> => {
        if (isLoggingOut) {
            return;
        }

        setIsLoggingOut(true);
        try {
            await signOut();
        } finally {
            setIsLoggingOut(false);
        }
    };

    const hasAssessment =
        progressQuery.isSuccess && progressQuery.data !== undefined;
    const missingAssessment =
        progressQuery.isError && isPrayerProgressMissing(progressQuery.error);

    return (
        <ScreenContainer contentContainerStyle={styles.content}>
            <StatusBar barStyle="dark-content" />

            <View style={styles.card}>
                <Text style={styles.greeting}>
                    Assalamu Alaikum, {user?.firstName ?? "friend"}
                </Text>
                <Text style={styles.message}>Your journey begins here.</Text>
            </View>

            {progressQuery.isLoading ? (
                <ActivityIndicator color={colors.primary} />
            ) : hasAssessment && progressQuery.data ? (
                <View style={styles.prayerCard}>
                    <Text style={styles.prayerTitle}>Prayer recovery</Text>
                    <Text style={styles.prayerMeta}>
                        Next:{" "}
                        {progressQuery.data.nextPrayer
                            ? PRAYER_LABELS[progressQuery.data.nextPrayer]
                            : "Complete"}
                    </Text>
                    <Text style={styles.prayerMeta}>
                        {progressQuery.data.totalCompletedPrayers.toLocaleString()}{" "}
                        completed ·{" "}
                        {progressQuery.data.totalRemainingPrayers.toLocaleString()}{" "}
                        remaining
                    </Text>
                    <PrimaryButton
                        title="Continue prayer recovery"
                        onPress={() => navigation.navigate("PrayerProgress")}
                    />
                </View>
            ) : (
                <PrimaryButton
                    title="Set up prayer recovery"
                    onPress={() => navigation.navigate("PrayerAssessment")}
                    disabled={
                        !missingAssessment
                        && progressQuery.isError
                        && !(
                            axios.isAxiosError(progressQuery.error)
                            && progressQuery.error.response?.status === 404
                        )
                    }
                />
            )}

            {goalQuery.isLoading ? (
                <ActivityIndicator color={colors.primary} />
            ) : hasDailyGoal ? (
                <View style={styles.quranCard}>
                    <Text style={styles.quranTitle}>Quran journey</Text>
                    <Text style={styles.quranMeta}>
                        {todayQuery.isSuccess && todayQuery.data
                            ? `${todayQuery.data.completedTasks}/${todayQuery.data.totalTasks} tasks today`
                            : todayQuery.isLoading
                                ? "Loading today's tasks…"
                                : "Your daily goal is set"}
                    </Text>
                    <PrimaryButton
                        title="Continue Quran journey"
                        onPress={() => navigation.navigate("QuranToday")}
                    />
                </View>
            ) : (
                <View style={styles.quranCard}>
                    <Text style={styles.quranTitle}>Quran journey</Text>
                    <Text style={styles.quranMeta}>
                        Set your Quran daily goal
                    </Text>
                    {goalQuery.isError && !missingDailyGoal ? (
                        <Text style={styles.quranError}>
                            Could not reach Quran service. Check that it is
                            running, then try again.
                        </Text>
                    ) : null}
                    <PrimaryButton
                        title="Set Quran daily goal"
                        onPress={() => navigation.navigate("QuranDailyGoal")}
                    />
                </View>
            )}

            <PrimaryButton
                title="Track Quran memorization"
                onPress={() => navigation.navigate("QuranTracker")}
            />

            <View style={styles.notificationsRow}>
                <PrimaryButton
                    title="Notifications"
                    onPress={() => navigation.navigate("Notifications")}
                    style={styles.notificationsButton}
                />
                {unreadCount > 0 ? (
                    <View style={styles.badge}>
                        <Text style={styles.badgeText}>
                            {unreadCount > 99 ? "99+" : String(unreadCount)}
                        </Text>
                    </View>
                ) : null}
            </View>

            <PrimaryButton
                loading={isLoggingOut}
                disabled={isLoggingOut}
                onPress={handleLogout}
                title="Log Out"
            />
        </ScreenContainer>
    );
}

const styles = StyleSheet.create({
    content: {
        flexGrow: 1,
        justifyContent: "center",
        paddingHorizontal: spacing.lg,
        paddingVertical: spacing.xl,
        gap: spacing.md,
    },

    card: {
        alignItems: "center",
        gap: spacing.md,
        marginBottom: spacing.lg,
    },

    greeting: {
        color: colors.primary,
        fontSize: typography.h2,
        fontWeight: "700",
        textAlign: "center",
    },

    message: {
        color: colors.textSecondary,
        fontSize: typography.body,
        textAlign: "center",
    },

    prayerCard: {
        gap: spacing.sm,
        width: "100%",
    },

    prayerTitle: {
        color: colors.primary,
        fontSize: typography.body,
        fontWeight: "700",
    },

    prayerMeta: {
        color: colors.textSecondary,
        fontSize: typography.caption,
    },

    quranCard: {
        gap: spacing.sm,
        width: "100%",
    },

    quranTitle: {
        color: colors.primary,
        fontSize: typography.body,
        fontWeight: "700",
    },

    quranMeta: {
        color: colors.textSecondary,
        fontSize: typography.caption,
    },

    quranError: {
        color: colors.error,
        fontSize: typography.caption,
    },

    notificationsRow: {
        width: "100%",
        position: "relative",
    },

    notificationsButton: {
        width: "100%",
    },

    badge: {
        position: "absolute",
        top: -6,
        right: 8,
        minWidth: 22,
        height: 22,
        borderRadius: 999,
        paddingHorizontal: 6,
        backgroundColor: colors.error,
        alignItems: "center",
        justifyContent: "center",
    },

    badgeText: {
        color: colors.surface,
        fontSize: typography.small,
        fontWeight: "700",
    },
});
