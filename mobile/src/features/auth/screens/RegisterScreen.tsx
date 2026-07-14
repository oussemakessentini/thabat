import { zodResolver } from "@hookform/resolvers/zod";
import React, { useState } from "react";
import { useForm } from "react-hook-form";
import {
    Alert,
    Pressable,
    StatusBar,
    StyleSheet,
    Text,
    View,
} from "react-native";

import type { RootStackScreenProps } from "../../../navigation/navigation.types";
import { parseApiError } from "../../../shared/api/parseApiError";
import { ScreenContainer } from "../../../shared/components/layout/ScreenContainer";
import { Logo } from "../../../shared/components/logo/Logo";
import {
    colors,
    spacing,
    typography,
} from "../../../shared/theme";
import type { RegisterRequest } from "../api/register.types";
import { RegisterAccountStep } from "../components/RegisterAccountStep";
import { RegisterPersonalStep } from "../components/RegisterPersonalStep";
import { RegisterStepIndicator } from "../components/RegisterStepIndicator";
import { useRegister } from "../hooks/useRegister";
import type {
    RegisterFormValues,
    RegisterStep,
} from "../types/register.types";
import { resolveDeviceTimezone } from "../utils/device.utils";
import { registerSchema } from "../validation/register.schema";

type RegisterScreenProps = RootStackScreenProps<"Register">;

const PERSONAL_INFO_FIELDS = [
    "firstName",
    "lastName",
    "dateOfBirth",
] as const;

const REGISTER_FIELD_NAMES = [
    "firstName",
    "lastName",
    "email",
    "password",
    "dateOfBirth",
] as const;

type RegisterFieldName = (typeof REGISTER_FIELD_NAMES)[number];

function isRegisterFieldName(field: string): field is RegisterFieldName {
    return (REGISTER_FIELD_NAMES as readonly string[]).includes(field);
}

export function RegisterScreen({
    navigation,
}: RegisterScreenProps): React.JSX.Element {
    const [step, setStep] = useState<RegisterStep>(1);
    const [formError, setFormError] = useState<string | undefined>();
    const { mutateAsync, isPending } = useRegister();

    const {
        control,
        handleSubmit,
        trigger,
        setError,
        clearErrors,
        formState: { isSubmitting },
    } = useForm<RegisterFormValues>({
        resolver: zodResolver(registerSchema),
        defaultValues: {
            firstName: "",
            lastName: "",
            dateOfBirth: "",
            email: "",
            password: "",
            confirmPassword: "",
        },
    });

    const isBusy = isSubmitting || isPending;

    const handleContinue = async (): Promise<void> => {
        setFormError(undefined);
        const isValid = await trigger(PERSONAL_INFO_FIELDS);

        if (isValid) {
            setStep(2);
        }
    };

    const handleBack = (): void => {
        setFormError(undefined);
        setStep(1);
    };

    const onSubmit = async (data: RegisterFormValues): Promise<void> => {
        if (isPending) {
            return;
        }

        setFormError(undefined);
        clearErrors();

        const payload: RegisterRequest = {
            firstName: data.firstName,
            lastName: data.lastName,
            email: data.email,
            password: data.password,
            dateOfBirth: data.dateOfBirth,
            countryCode: "US",
            timezone: resolveDeviceTimezone(),
            preferredLanguage: "en",
        };

        try {
            const response = await mutateAsync(payload);

            Alert.alert(
                "Account created",
                response.message || "Your account was created successfully.",
                [
                    {
                        text: "Sign in",
                        onPress: () =>
                            navigation.navigate("Login", {
                                email: response.email,
                            }),
                    },
                ],
            );
        } catch (error) {
            const parsed = parseApiError(error);
            let hasFieldError = false;

            Object.entries(parsed.validationErrors).forEach(([field, message]) => {
                if (isRegisterFieldName(field)) {
                    setError(field, { type: "server", message });
                    hasFieldError = true;

                    if (field === "firstName" || field === "lastName" || field === "dateOfBirth") {
                        setStep(1);
                    }
                }
            });

            if (parsed.isConflict) {
                setError("email", {
                    type: "server",
                    message: parsed.message || "An account with this email already exists.",
                });
                setStep(2);
                return;
            }

            if (!hasFieldError) {
                setFormError(parsed.message);
                setStep(2);
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
            <RegisterStepIndicator step={step} />

            {step === 1 ? (
                <RegisterPersonalStep
                    control={control}
                    disabled={isBusy}
                    onContinue={handleContinue}
                />
            ) : (
                <RegisterAccountStep
                    control={control}
                    disabled={isBusy}
                    formError={formError}
                    loading={isBusy}
                    onBack={handleBack}
                    onSubmit={handleSubmit(onSubmit)}
                />
            )}

            <View style={styles.footer}>
                <Text style={styles.footerText}>Already have an account?</Text>
                <Pressable
                    accessibilityRole="button"
                    disabled={isBusy}
                    onPress={() => navigation.navigate("Login")}
                >
                    <Text style={styles.footerLink}>Sign in</Text>
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
