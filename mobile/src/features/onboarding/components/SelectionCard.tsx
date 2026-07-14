import React from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";

import {
    colors,
    radius,
    spacing,
    typography,
} from "../../../shared/theme";

type SelectionCardProps = {
    label: string;
    selected: boolean;
    onPress: () => void;
};

export function SelectionCard({
    label,
    selected,
    onPress,
}: SelectionCardProps): React.JSX.Element {
    return (
        <Pressable
            accessibilityRole="button"
            accessibilityState={{ selected }}
            onPress={onPress}
            style={({ pressed }) => [
                styles.card,
                selected && styles.cardSelected,
                pressed && styles.cardPressed,
            ]}
        >
            <View
                style={[styles.indicator, selected && styles.indicatorSelected]}
            />
            <Text style={[styles.label, selected && styles.labelSelected]}>
                {label}
            </Text>
        </Pressable>
    );
}

const styles = StyleSheet.create({
    card: {
        minHeight: 56,
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
        borderWidth: 1.5,
        borderColor: colors.border,
        borderRadius: radius.lg,
        backgroundColor: colors.surface,
        paddingHorizontal: spacing.md,
        paddingVertical: spacing.sm + 2,
    },

    cardSelected: {
        borderColor: colors.primary,
        backgroundColor: colors.muted,
    },

    cardPressed: {
        opacity: 0.9,
    },

    indicator: {
        width: 18,
        height: 18,
        borderRadius: radius.full,
        borderWidth: 2,
        borderColor: colors.border,
        backgroundColor: colors.surface,
    },

    indicatorSelected: {
        borderColor: colors.secondary,
        backgroundColor: colors.secondary,
    },

    label: {
        flex: 1,
        color: colors.textPrimary,
        fontSize: typography.body,
        fontWeight: "500",
    },

    labelSelected: {
        color: colors.primaryDark,
        fontWeight: "700",
    },
});
