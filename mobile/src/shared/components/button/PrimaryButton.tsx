import React from "react";
import {
    ActivityIndicator,
    Pressable,
    StyleProp,
    StyleSheet,
    Text,
    ViewStyle,
} from "react-native";

import {
    colors,
    radius,
    spacing,
    typography,
} from "../../theme";

type PrimaryButtonProps = {
    title: string;
    onPress: () => void;
    disabled?: boolean;
    loading?: boolean;
    fullWidth?: boolean;
    style?: StyleProp<ViewStyle>;
};

export function PrimaryButton({
    title,
    onPress,
    disabled = false,
    loading = false,
    fullWidth = true,
    style,
}: PrimaryButtonProps): React.JSX.Element {
    const isDisabled = disabled || loading;

    return (
        <Pressable
            accessibilityRole="button"
            accessibilityState={{ disabled: isDisabled, busy: loading }}
            disabled={isDisabled}
            onPress={onPress}
            style={({ pressed }) => [
                styles.button,
                fullWidth && styles.fullWidth,
                isDisabled && styles.disabled,
                pressed && !isDisabled && styles.pressed,
                style,
            ]}
        >
            {loading ? (
                <ActivityIndicator color={colors.surface} />
            ) : (
                <Text style={styles.text}>{title}</Text>
            )}
        </Pressable>
    );
}

const styles = StyleSheet.create({
    button: {
        minHeight: 54,
        paddingHorizontal: spacing.lg,
        borderRadius: radius.lg,
        backgroundColor: colors.primary,
        alignItems: "center",
        justifyContent: "center",
    },

    fullWidth: {
        width: "100%",
    },

    pressed: {
        opacity: 0.85,
        transform: [{ scale: 0.99 }],
    },

    disabled: {
        opacity: 0.5,
    },

    text: {
        color: colors.surface,
        fontSize: typography.body,
        fontWeight: "700",
    },
});
