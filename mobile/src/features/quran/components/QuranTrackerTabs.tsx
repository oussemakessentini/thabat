import React from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";

import {
    colors,
    radius,
    spacing,
    typography,
} from "../../../shared/theme";

export type QuranTrackerTab = "Pages" | "Surahs" | "Juz" | "Hizb";

type Props = {
    value: QuranTrackerTab;
    onChange: (tab: QuranTrackerTab) => void;
};

const TABS: QuranTrackerTab[] = ["Pages", "Surahs", "Juz", "Hizb"];

export function QuranTrackerTabs({
    value,
    onChange,
}: Props): React.JSX.Element {
    return (
        <View style={styles.tabs}>
            {TABS.map((option) => {
                const selected = value === option;
                return (
                    <Pressable
                        key={option}
                        accessibilityRole="button"
                        onPress={() => onChange(option)}
                        style={[styles.tab, selected && styles.tabSelected]}
                    >
                        <Text
                            style={[
                                styles.tabText,
                                selected && styles.tabTextSelected,
                            ]}
                        >
                            {option}
                        </Text>
                    </Pressable>
                );
            })}
        </View>
    );
}

const styles = StyleSheet.create({
    tabs: {
        flexDirection: "row",
        flexWrap: "wrap",
        gap: spacing.xs,
        marginBottom: spacing.md,
    },

    tab: {
        minHeight: 40,
        paddingHorizontal: spacing.md,
        borderRadius: radius.full,
        borderWidth: 1,
        borderColor: colors.border,
        backgroundColor: colors.surface,
        justifyContent: "center",
    },

    tabSelected: {
        backgroundColor: colors.primaryDark,
        borderColor: colors.primaryDark,
    },

    tabText: {
        color: colors.textSecondary,
        fontSize: typography.small,
        fontWeight: "700",
    },

    tabTextSelected: {
        color: colors.surface,
    },
});
