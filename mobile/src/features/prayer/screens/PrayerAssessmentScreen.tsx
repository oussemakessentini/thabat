import { zodResolver } from "@hookform/resolvers/zod";
import React, { useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { StatusBar, StyleSheet, Text, View } from "react-native";

import type { AppStackScreenProps } from "../../../navigation/navigation.types";
import { parseApiError } from "../../../shared/api/parseApiError";
import { PrimaryButton } from "../../../shared/components/button/PrimaryButton";
import { PrimaryInput } from "../../../shared/components/input/PrimaryInput";
import { ScreenContainer } from "../../../shared/components/layout/ScreenContainer";
import {
    colors,
    spacing,
    typography,
} from "../../../shared/theme";
import { useCreatePrayerAssessment } from "../hooks/useCreatePrayerAssessment";
import {
    prayerAssessmentSchema,
    type PrayerAssessmentFormValues,
} from "../validation/prayerAssessment.schema";

type Props = AppStackScreenProps<"PrayerAssessment">;

export function PrayerAssessmentScreen({
    navigation,
}: Props): React.JSX.Element {
    const { mutateAsync, isPending } = useCreatePrayerAssessment();
    const [formError, setFormError] = useState<string | undefined>();

    const {
        control,
        handleSubmit,
        setError,
        formState: { isSubmitting },
    } = useForm<PrayerAssessmentFormValues>({
        resolver: zodResolver(prayerAssessmentSchema),
        defaultValues: {
            missedYears: 0,
            missedMonths: 0,
            missedDays: 0,
            dailyRecoveryTarget: 5,
        },
    });

    const isBusy = isSubmitting || isPending;

    const onSubmit = async (values: PrayerAssessmentFormValues): Promise<void> => {
        if (isPending) {
            return;
        }

        setFormError(undefined);

        try {
            const assessment = await mutateAsync({
                missedYears: values.missedYears,
                missedMonths: values.missedMonths,
                missedDays: values.missedDays,
                dailyRecoveryTarget: values.dailyRecoveryTarget,
            });
            navigation.navigate("PrayerAssessmentResult", {
                assessment,
            });
        } catch (error) {
            const parsed = parseApiError(error);

            if (parsed.isValidationError) {
                Object.entries(parsed.validationErrors).forEach(([field, message]) => {
                    if (
                        field === "missedYears"
                        || field === "missedMonths"
                        || field === "missedDays"
                        || field === "dailyRecoveryTarget"
                    ) {
                        setError(field, { message });
                    }
                });
            }

            setFormError(parsed.message);
        }
    };

    return (
        <ScreenContainer scrollable contentContainerStyle={styles.content}>
            <StatusBar barStyle="dark-content" />

            <View style={styles.header}>
                <Text style={styles.title}>Missed prayer assessment</Text>
                <Text style={styles.subtitle}>
                    Estimate how much qadha you want to plan for. This is for
                    planning only — not a fatwa.
                </Text>
            </View>

            <Controller
                control={control}
                name="missedYears"
                render={({ field: { onChange, value }, fieldState }) => (
                    <PrimaryInput
                        label="Missed years"
                        keyboardType="number-pad"
                        value={String(value ?? 0)}
                        onChangeText={(text) => {
                            const digits = text.replace(/[^0-9]/g, "");
                            onChange(digits === "" ? 0 : Number(digits));
                        }}
                        error={fieldState.error?.message}
                    />
                )}
            />

            <Controller
                control={control}
                name="missedMonths"
                render={({ field: { onChange, value }, fieldState }) => (
                    <PrimaryInput
                        label="Missed months"
                        keyboardType="number-pad"
                        value={String(value ?? 0)}
                        onChangeText={(text) => {
                            const digits = text.replace(/[^0-9]/g, "");
                            onChange(digits === "" ? 0 : Number(digits));
                        }}
                        error={fieldState.error?.message}
                    />
                )}
            />

            <Controller
                control={control}
                name="missedDays"
                render={({ field: { onChange, value }, fieldState }) => (
                    <PrimaryInput
                        label="Missed days"
                        keyboardType="number-pad"
                        value={String(value ?? 0)}
                        onChangeText={(text) => {
                            const digits = text.replace(/[^0-9]/g, "");
                            onChange(digits === "" ? 0 : Number(digits));
                        }}
                        error={fieldState.error?.message}
                    />
                )}
            />

            <Controller
                control={control}
                name="dailyRecoveryTarget"
                render={({ field: { onChange, value }, fieldState }) => (
                    <PrimaryInput
                        label="Daily recovery target (prayers)"
                        keyboardType="number-pad"
                        value={String(value ?? 0)}
                        onChangeText={(text) => {
                            const digits = text.replace(/[^0-9]/g, "");
                            onChange(digits === "" ? 0 : Number(digits));
                        }}
                        error={fieldState.error?.message}
                    />
                )}
            />

            {formError ? <Text style={styles.error}>{formError}</Text> : null}

            <PrimaryButton
                title="Calculate estimate"
                loading={isBusy}
                disabled={isBusy}
                onPress={handleSubmit(onSubmit)}
            />
        </ScreenContainer>
    );
}

const styles = StyleSheet.create({
    content: {
        flexGrow: 1,
        paddingHorizontal: spacing.lg,
        paddingVertical: spacing.xl,
        gap: spacing.md,
    },

    header: {
        gap: spacing.sm,
        marginBottom: spacing.sm,
    },

    title: {
        color: colors.primary,
        fontSize: typography.h2,
        fontWeight: "700",
    },

    subtitle: {
        color: colors.textSecondary,
        fontSize: typography.body,
        lineHeight: 22,
    },

    error: {
        color: colors.error,
        fontSize: typography.caption,
    },
});
