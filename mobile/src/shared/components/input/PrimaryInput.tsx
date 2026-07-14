import React from "react";
import {
    KeyboardTypeOptions,
    Pressable,
    StyleSheet,
    Text,
    TextInput,
    View,
} from "react-native";

import {
    colors,
    radius,
    spacing,
    typography,
} from "../../theme";

type PrimaryInputProps = {
    label: string;
    value: string;
    onChangeText: (text: string) => void;
    placeholder?: string;
    error?: string;
    secureTextEntry?: boolean;
    keyboardType?: KeyboardTypeOptions;
    autoCapitalize?: "none" | "sentences" | "words" | "characters";
    editable?: boolean;
    showPasswordToggle?: boolean;
};

export function PrimaryInput({
    label,
    value,
    onChangeText,
    placeholder,
    error,
    secureTextEntry = false,
    keyboardType = "default",
    autoCapitalize = "none",
    editable = true,
    showPasswordToggle = false,
}: PrimaryInputProps): React.JSX.Element {
    const [isSecure, setIsSecure] = React.useState(secureTextEntry);
    const hasError = Boolean(error);

    React.useEffect(() => {
        setIsSecure(secureTextEntry);
    }, [secureTextEntry]);

    return (
        <View style={styles.wrapper}>
            <Text style={styles.label}>{label}</Text>

            <View
                style={[
                    styles.inputRow,
                    hasError && styles.inputError,
                    !editable && styles.inputDisabled,
                ]}
            >
                <TextInput
                    accessibilityLabel={label}
                    autoCapitalize={autoCapitalize}
                    editable={editable}
                    keyboardType={keyboardType}
                    onChangeText={onChangeText}
                    placeholder={placeholder}
                    placeholderTextColor={colors.textSecondary}
                    secureTextEntry={isSecure}
                    style={styles.input}
                    value={value}
                />

                {showPasswordToggle && secureTextEntry ? (
                    <Pressable
                        accessibilityRole="button"
                        onPress={() => setIsSecure((current) => !current)}
                        style={styles.toggle}
                    >
                        <Text style={styles.toggleText}>
                            {isSecure ? "Show" : "Hide"}
                        </Text>
                    </Pressable>
                ) : null}
            </View>

            {hasError ? <Text style={styles.error}>{error}</Text> : null}
        </View>
    );
}

const styles = StyleSheet.create({
    wrapper: {
        width: "100%",
    },

    label: {
        marginBottom: spacing.sm,
        color: colors.textPrimary,
        fontSize: typography.caption,
        fontWeight: "600",
    },

    inputRow: {
        minHeight: 54,
        paddingHorizontal: spacing.md,
        borderWidth: 1,
        borderColor: colors.border,
        borderRadius: radius.lg,
        backgroundColor: colors.surface,
        flexDirection: "row",
        alignItems: "center",
    },

    input: {
        flex: 1,
        color: colors.textPrimary,
        fontSize: typography.body,
        paddingVertical: spacing.sm,
    },

    inputError: {
        borderColor: colors.error,
    },

    inputDisabled: {
        backgroundColor: colors.muted,
    },

    toggle: {
        marginLeft: spacing.sm,
        paddingVertical: spacing.xs,
    },

    toggleText: {
        color: colors.primary,
        fontSize: typography.caption,
        fontWeight: "600",
    },

    error: {
        marginTop: spacing.xs,
        color: colors.error,
        fontSize: typography.small,
    },
});
