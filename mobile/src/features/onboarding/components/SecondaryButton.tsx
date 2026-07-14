import React from "react";
import { Pressable, StyleSheet, Text } from "react-native";

import {
    colors,
    radius,
    spacing,
    typography,
} from "../../../shared/theme";

type SecondaryButtonProps = {
    title: string;
    onPress: () => void;
    disabled?: boolean;
};

export function SecondaryButton({
    title,
    onPress,
    disabled = false,
}: SecondaryButtonProps): React.JSX.Element {
    return (
        <Pressable
            accessibilityRole="button"
            accessibilityState={{ disabled }}
            disabled={disabled}
            onPress={onPress}
            style={({ pressed }) => [
                styles.button,
                disabled && styles.disabled,
                pressed && !disabled && styles.pressed,
            ]}
        >
            <Text style={styles.text}>{title}</Text>
        </Pressable>
    );
}

const styles = StyleSheet.create({
    button: {
        minHeight: 54,
        width: "100%",
        borderRadius: radius.lg,
        borderWidth: 1.5,
        borderColor: colors.primary,
        backgroundColor: colors.surface,
        alignItems: "center",
        justifyContent: "center",
        paddingHorizontal: spacing.lg,
    },

    pressed: {
        opacity: 0.85,
        backgroundColor: colors.muted,
    },

    disabled: {
        opacity: 0.5,
    },

    text: {
        color: colors.primary,
        fontSize: typography.body,
        fontWeight: "700",
    },
});
