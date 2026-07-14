import React from "react";
import {
    ActivityIndicator,
    Pressable,
    StatusBar,
    StyleSheet,
    Text,
    View,
} from "react-native";

import type { AppStackScreenProps } from "../../../navigation/navigation.types";
import { parseApiError } from "../../../shared/api/parseApiError";
import { formatDateOfBirthDisplay } from "../../auth/utils/date.utils";
import { ScreenContainer } from "../../../shared/components/layout/ScreenContainer";
import {
    colors,
    radius,
    spacing,
    typography,
} from "../../../shared/theme";
import { useRecoveryHistory } from "../hooks/useRecoveryHistory";
import type { PrayerType } from "../types/prayer.types";

type Props = AppStackScreenProps<"RecoveryHistory">;

const PRAYER_LABELS: Record<PrayerType, string> = {
    FAJR: "Fajr",
    DHUHR: "Dhuhr",
    ASR: "Asr",
    MAGHRIB: "Maghrib",
    ISHA: "Isha",
};

export function RecoveryHistoryScreen({}: Props): React.JSX.Element {
    const { data, isLoading, isError, error, refetch } = useRecoveryHistory();

    if (isLoading) {
        return (
            <View style={styles.centered}>
                <ActivityIndicator color={colors.primary} size="large" />
            </View>
        );
    }

    if (isError) {
        return (
            <ScreenContainer contentContainerStyle={styles.content}>
                <Text style={styles.title}>Unable to load history</Text>
                <Text style={styles.subtitle}>{parseApiError(error).message}</Text>
                <Pressable onPress={() => void refetch()}>
                    <Text style={styles.link}>Try again</Text>
                </Pressable>
            </ScreenContainer>
        );
    }

    const entries = data ?? [];

    return (
        <ScreenContainer scrollable contentContainerStyle={styles.content}>
            <StatusBar barStyle="dark-content" />

            <Text style={styles.title}>Recovery history</Text>
            <Text style={styles.subtitle}>
                Newest first. Only the latest prayer can be undone from the
                progress screen so the fixed order stays intact.
            </Text>

            {entries.length === 0 ? (
                <Text style={styles.empty}>No completed prayers yet.</Text>
            ) : (
                entries.map((entry) => (
                    <View key={entry.id} style={styles.card}>
                        <Text style={styles.cardTitle}>
                            #{entry.sequenceNumber} ·{" "}
                            {PRAYER_LABELS[entry.prayerType]}
                        </Text>
                        <Text style={styles.cardMeta}>
                            {formatDateOfBirthDisplay(entry.completedOn)}
                        </Text>
                    </View>
                ))
            )}
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

    subtitle: {
        color: colors.textSecondary,
        fontSize: typography.body,
        lineHeight: 22,
        marginBottom: spacing.sm,
    },

    empty: {
        color: colors.textSecondary,
        fontSize: typography.body,
    },

    card: {
        backgroundColor: colors.surface,
        borderRadius: radius.lg,
        borderWidth: 1,
        borderColor: colors.border,
        padding: spacing.md,
        gap: spacing.xs,
    },

    cardTitle: {
        color: colors.textPrimary,
        fontSize: typography.body,
        fontWeight: "700",
    },

    cardMeta: {
        color: colors.textSecondary,
        fontSize: typography.caption,
    },

    link: {
        color: colors.primary,
        fontWeight: "700",
        fontSize: typography.body,
    },
});
