import React from "react";
import { Control, Controller } from "react-hook-form";
import { Pressable, StyleSheet, Text, View } from "react-native";

import { PrimaryButton } from "../../../shared/components/button/PrimaryButton";
import { PrimaryInput } from "../../../shared/components/input/PrimaryInput";
import {
    colors,
    radius,
    spacing,
    typography,
} from "../../../shared/theme";
import type { RegisterFormValues } from "../types/register.types";

type RegisterAccountStepProps = {
    control: Control<RegisterFormValues>;
    disabled?: boolean;
    loading?: boolean;
    formError?: string;
    onBack: () => void;
    onSubmit: () => void;
};

export function RegisterAccountStep({
    control,
    disabled = false,
    loading = false,
    formError,
    onBack,
    onSubmit,
}: RegisterAccountStepProps): React.JSX.Element {
    return (
        <View style={styles.form}>
            <Controller
                control={control}
                name="email"
                render={({ field: { onChange, value }, fieldState: { error } }) => (
                    <PrimaryInput
                        autoCapitalize="none"
                        editable={!disabled}
                        error={error?.message}
                        keyboardType="email-address"
                        label="Email"
                        onChangeText={onChange}
                        placeholder="you@example.com"
                        value={value}
                    />
                )}
            />

            <Controller
                control={control}
                name="password"
                render={({ field: { onChange, value }, fieldState: { error } }) => (
                    <PrimaryInput
                        editable={!disabled}
                        error={error?.message}
                        label="Password"
                        onChangeText={onChange}
                        placeholder="Create a password"
                        secureTextEntry
                        value={value}
                    />
                )}
            />

            <Controller
                control={control}
                name="confirmPassword"
                render={({ field: { onChange, value }, fieldState: { error } }) => (
                    <PrimaryInput
                        editable={!disabled}
                        error={error?.message}
                        label="Confirm Password"
                        onChangeText={onChange}
                        placeholder="Confirm your password"
                        secureTextEntry
                        value={value}
                    />
                )}
            />

            {formError ? <Text style={styles.formError}>{formError}</Text> : null}

            <View style={styles.actions}>
                <Pressable
                    accessibilityRole="button"
                    disabled={disabled || loading}
                    onPress={onBack}
                    style={({ pressed }) => [
                        styles.backButton,
                        (disabled || loading) && styles.backButtonDisabled,
                        pressed && !disabled && !loading && styles.backButtonPressed,
                    ]}
                >
                    <Text style={styles.backButtonText}>Back</Text>
                </Pressable>

                <PrimaryButton
                    disabled={disabled || loading}
                    loading={loading}
                    onPress={onSubmit}
                    style={styles.submitButton}
                    title="Create Account"
                />
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    form: {
        marginTop: spacing.lg,
        gap: spacing.md,
    },

    formError: {
        color: colors.error,
        fontSize: typography.caption,
        textAlign: "center",
    },

    actions: {
        marginTop: spacing.sm,
        gap: spacing.md,
    },

    backButton: {
        minHeight: 54,
        borderWidth: 1,
        borderColor: colors.primary,
        borderRadius: radius.lg,
        alignItems: "center",
        justifyContent: "center",
        backgroundColor: colors.surface,
    },

    backButtonDisabled: {
        opacity: 0.5,
    },

    backButtonPressed: {
        opacity: 0.85,
    },

    backButtonText: {
        color: colors.primary,
        fontSize: typography.body,
        fontWeight: "700",
    },

    submitButton: {},
});
