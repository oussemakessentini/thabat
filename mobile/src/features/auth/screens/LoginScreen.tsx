import { zodResolver } from "@hookform/resolvers/zod";
import React, { useState } from "react";
import { Controller, useForm } from "react-hook-form";
import {
    Pressable,
    StatusBar,
    StyleSheet,
    Text,
    View,
} from "react-native";

import type { AuthStackScreenProps } from "../../../navigation/navigation.types";
import { parseApiError } from "../../../shared/api/parseApiError";
import { useAuth } from "../../../shared/auth/AuthContext";
import { PrimaryButton } from "../../../shared/components/button/PrimaryButton";
import { PrimaryInput } from "../../../shared/components/input/PrimaryInput";
import { ScreenContainer } from "../../../shared/components/layout/ScreenContainer";
import { Logo } from "../../../shared/components/logo/Logo";
import {
    colors,
    spacing,
    typography,
} from "../../../shared/theme";
import { useLogin } from "../hooks/useLogin";
import {
    loginSchema,
    type LoginFormValues,
} from "../validation/login.schema";

type LoginScreenProps = AuthStackScreenProps<"Login">;

export function LoginScreen({
    navigation,
    route,
}: LoginScreenProps): React.JSX.Element {
    const { signIn } = useAuth();
    const { mutateAsync, isPending } = useLogin();
    const [formError, setFormError] = useState<string | undefined>();

    const {
        control,
        handleSubmit,
        setError,
        formState: { isSubmitting },
    } = useForm<LoginFormValues>({
        resolver: zodResolver(loginSchema),
        defaultValues: {
            email: route.params?.email ?? "",
            password: "",
        },
    });

    const isBusy = isSubmitting || isPending;

    const onSubmit = async (data: LoginFormValues): Promise<void> => {
        if (isPending) {
            return;
        }

        setFormError(undefined);

        try {
            const response = await mutateAsync({
                email: data.email.trim().toLowerCase(),
                password: data.password,
            });
            await signIn(response);
        } catch (error) {
            const parsed = parseApiError(error);

            if (parsed.status === 401) {
                setFormError(parsed.message || "Invalid email or password");
                return;
            }

            if (parsed.status === 403) {
                setFormError(parsed.message);
                return;
            }

            if (parsed.validationErrors.email) {
                setError("email", {
                    type: "server",
                    message: parsed.validationErrors.email,
                });
            }

            if (parsed.validationErrors.password) {
                setError("password", {
                    type: "server",
                    message: parsed.validationErrors.password,
                });
            }

            if (
                !parsed.validationErrors.email
                && !parsed.validationErrors.password
            ) {
                setFormError(parsed.message);
            }
        }
    };

    return (
        <ScreenContainer
            scrollable
            contentContainerStyle={styles.content}
        >
            <StatusBar barStyle="dark-content" />

            <Logo size="medium" />

            <View style={styles.form}>
                <Controller
                    control={control}
                    name="email"
                    render={({ field: { onChange, value }, fieldState: { error } }) => (
                        <PrimaryInput
                            autoCapitalize="none"
                            editable={!isBusy}
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
                            editable={!isBusy}
                            error={error?.message}
                            label="Password"
                            onChangeText={onChange}
                            placeholder="Enter your password"
                            secureTextEntry
                            showPasswordToggle
                            value={value}
                        />
                    )}
                />

                <Pressable accessibilityRole="button" style={styles.forgotPassword}>
                    <Text style={styles.forgotPasswordText}>Forgot password?</Text>
                </Pressable>

                {formError ? <Text style={styles.formError}>{formError}</Text> : null}

                <PrimaryButton
                    disabled={isBusy}
                    loading={isBusy}
                    onPress={handleSubmit(onSubmit)}
                    style={styles.submitButton}
                    title="Sign In"
                />
            </View>

            <View style={styles.footer}>
                <Text style={styles.footerText}>Don&apos;t have an account?</Text>
                <Pressable
                    accessibilityRole="button"
                    disabled={isBusy}
                    onPress={() => navigation.navigate("Register")}
                >
                    <Text style={styles.footerLink}>Create one</Text>
                </Pressable>
            </View>
        </ScreenContainer>
    );
}

const styles = StyleSheet.create({
    content: {
        flexGrow: 1,
        paddingHorizontal: spacing.lg,
        paddingVertical: spacing.xl,
    },

    form: {
        marginTop: spacing.lg,
        gap: spacing.md,
    },

    forgotPassword: {
        alignSelf: "flex-end",
    },

    forgotPasswordText: {
        color: colors.primary,
        fontSize: typography.caption,
        fontWeight: "600",
    },

    formError: {
        color: colors.error,
        fontSize: typography.caption,
        textAlign: "center",
    },

    submitButton: {
        marginTop: spacing.sm,
    },

    footer: {
        marginTop: spacing.xl,
        flexDirection: "row",
        justifyContent: "center",
        alignItems: "center",
        gap: spacing.xs,
    },

    footerText: {
        color: colors.textSecondary,
        fontSize: typography.body,
    },

    footerLink: {
        color: colors.primary,
        fontSize: typography.body,
        fontWeight: "700",
    },
});
