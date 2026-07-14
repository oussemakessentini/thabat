import React from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";

import { colors, radius, spacing, typography } from "../../../shared/theme";
import type { AppNotification } from "../types/notification.types";

type Props = {
    notification: AppNotification;
    onPress: (notification: AppNotification) => void;
};

export function NotificationCard({
    notification,
    onPress,
}: Props): React.JSX.Element {
    return (
        <Pressable
            onPress={() => onPress(notification)}
            style={[styles.card, !notification.read && styles.unreadCard]}
        >
            <View style={styles.header}>
                <Text style={styles.title}>{notification.title}</Text>
                {!notification.read ? <View style={styles.dot} /> : null}
            </View>
            <Text style={styles.message}>{notification.message}</Text>
            <Text style={styles.meta}>
                {new Date(notification.createdAt).toLocaleString()}
            </Text>
        </Pressable>
    );
}

const styles = StyleSheet.create({
    card: {
        borderWidth: 1,
        borderColor: colors.border,
        borderRadius: radius.md,
        backgroundColor: colors.surface,
        padding: spacing.md,
        gap: spacing.xs,
    },

    unreadCard: {
        backgroundColor: colors.muted,
        borderColor: colors.primary,
    },

    header: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
        gap: spacing.sm,
    },

    title: {
        flex: 1,
        color: colors.primary,
        fontSize: typography.body,
        fontWeight: "700",
    },

    dot: {
        width: 8,
        height: 8,
        borderRadius: radius.full,
        backgroundColor: colors.error,
    },

    message: {
        color: colors.textPrimary,
        fontSize: typography.caption,
    },

    meta: {
        color: colors.textSecondary,
        fontSize: typography.small,
        marginTop: spacing.xs,
    },
});
