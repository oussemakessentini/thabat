import React from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";

import {
    colors,
    radius,
    spacing,
    typography,
} from "../../../shared/theme";
import type { QuranAggregateFields } from "../types/quran.types";
import { QuranAggregateMeta } from "./QuranAggregateMeta";

type Props = {
    title: string;
    subtitle?: string;
    trailing?: string;
    aggregate: QuranAggregateFields;
    onPress: () => void;
    width?: number;
};

export function QuranSectionCard({
    title,
    subtitle,
    trailing,
    aggregate,
    onPress,
    width,
}: Props): React.JSX.Element {
    return (
        <Pressable
            accessibilityRole="button"
            onPress={onPress}
            style={({ pressed }) => [
                styles.card,
                width !== undefined ? { width } : null,
                pressed && styles.pressed,
            ]}
        >
            <View style={styles.header}>
                <Text style={styles.title}>{title}</Text>
                {trailing ? <Text style={styles.trailing}>{trailing}</Text> : null}
            </View>
            {subtitle ? <Text style={styles.subtitle}>{subtitle}</Text> : null}
            <QuranAggregateMeta aggregate={aggregate} />
        </Pressable>
    );
}

const styles = StyleSheet.create({
    card: {
        minHeight: 96,
        padding: spacing.md,
        borderWidth: 1,
        borderColor: colors.border,
        borderRadius: radius.lg,
        backgroundColor: colors.surface,
        gap: spacing.xs,
    },

    pressed: {
        opacity: 0.8,
    },

    header: {
        flexDirection: "row",
        justifyContent: "space-between",
        gap: spacing.sm,
    },

    title: {
        flex: 1,
        color: colors.primary,
        fontSize: typography.body,
        fontWeight: "700",
    },

    trailing: {
        color: colors.primary,
        fontSize: typography.body,
        fontWeight: "600",
    },

    subtitle: {
        color: colors.textSecondary,
        fontSize: typography.caption,
    },
});
