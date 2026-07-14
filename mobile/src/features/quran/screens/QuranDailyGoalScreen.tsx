import { zodResolver } from "@hookform/resolvers/zod";
import React, { useEffect, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { StatusBar, StyleSheet, Text, View } from "react-native";

import type { AppStackScreenProps } from "../../../navigation/navigation.types";
import { parseApiError } from "../../../shared/api/parseApiError";
import { PrimaryButton } from "../../../shared/components/button/PrimaryButton";
import { PrimaryInput } from "../../../shared/components/input/PrimaryInput";
import { ScreenContainer } from "../../../shared/components/layout/ScreenContainer";
import { colors, spacing, typography } from "../../../shared/theme";
import {
    isQuranDailyGoalMissing,
    useQuranDailyGoal,
} from "../hooks/useQuranDailyGoal";
import { useSaveQuranDailyGoal } from "../hooks/useSaveQuranDailyGoal";
import {
    quranDailyGoalSchema,
    type QuranDailyGoalFormValues,
} from "../validation/quranDailyGoal.schema";

type Props = AppStackScreenProps<"QuranDailyGoal">;

function parseOptionalInt(text: string): number | null {
    const digits = text.replace(/[^\d]/g, "");
    if (digits.length === 0) {
        return null;
    }
    return Number(digits);
}

export function QuranDailyGoalScreen({ navigation }: Props): React.JSX.Element {
    const goalQuery = useQuranDailyGoal(true);
    const { mutateAsync, isPending } = useSaveQuranDailyGoal();
    const [formError, setFormError] = useState<string | undefined>();

    const {
        control,
        handleSubmit,
        reset,
        setError,
        formState: { isSubmitting },
    } = useForm<QuranDailyGoalFormValues>({
        resolver: zodResolver(quranDailyGoalSchema),
        defaultValues: {
            memorizationPagesPerDay: 1,
            revisionPagesPerDay: 3,
            preferredStartPage: null,
        },
    });

    useEffect(() => {
        if (goalQuery.isSuccess && goalQuery.data) {
            reset({
                memorizationPagesPerDay: goalQuery.data.memorizationPagesPerDay,
                revisionPagesPerDay: goalQuery.data.revisionPagesPerDay,
                preferredStartPage: goalQuery.data.preferredStartPage,
            });
        }
    }, [goalQuery.isSuccess, goalQuery.data, reset]);

    const isBusy = isSubmitting || isPending;

    const onSubmit = async (values: QuranDailyGoalFormValues): Promise<void> => {
        if (isPending) {
            return;
        }

        setFormError(undefined);

        try {
            await mutateAsync({
                memorizationPagesPerDay: values.memorizationPagesPerDay,
                revisionPagesPerDay: values.revisionPagesPerDay,
                preferredStartPage: values.preferredStartPage ?? null,
            });
            navigation.replace("QuranToday");
        } catch (error) {
            const parsed = parseApiError(error);
            if (parsed.isValidationError) {
                Object.entries(parsed.validationErrors).forEach(([field, message]) => {
                    if (
                        field === "memorizationPagesPerDay"
                        || field === "revisionPagesPerDay"
                        || field === "preferredStartPage"
                    ) {
                        setError(field, { message });
                    }
                });
            }
            setFormError(parsed.message);
        }
    };

    const loadError =
        goalQuery.isError && !isQuranDailyGoalMissing(goalQuery.error)
            ? parseApiError(goalQuery.error).message
            : undefined;

    return (
        <ScreenContainer scrollable contentContainerStyle={styles.content}>
            <StatusBar barStyle="dark-content" />

            <View style={styles.header}>
                <Text style={styles.title}>Quran daily goal</Text>
                <Text style={styles.subtitle}>
                    Set how many pages to memorize and revise each day.
                </Text>
                <PrimaryButton
                    title="Back"
                    fullWidth={false}
                    onPress={() => navigation.goBack()}
                    style={styles.backButton}
                />
            </View>

            {loadError ? <Text style={styles.error}>{loadError}</Text> : null}

            <Controller
                control={control}
                name="memorizationPagesPerDay"
                render={({ field: { onChange, value }, fieldState }) => (
                    <PrimaryInput
                        label="Memorization pages per day"
                        value={String(value)}
                        keyboardType="number-pad"
                        error={fieldState.error?.message}
                        onChangeText={(text) => {
                            const next = parseOptionalInt(text);
                            onChange(next ?? 0);
                        }}
                    />
                )}
            />

            <Controller
                control={control}
                name="revisionPagesPerDay"
                render={({ field: { onChange, value }, fieldState }) => (
                    <PrimaryInput
                        label="Revision pages per day"
                        value={String(value)}
                        keyboardType="number-pad"
                        error={fieldState.error?.message}
                        onChangeText={(text) => {
                            const next = parseOptionalInt(text);
                            onChange(next ?? 0);
                        }}
                    />
                )}
            />

            <Controller
                control={control}
                name="preferredStartPage"
                render={({ field: { onChange, value }, fieldState }) => (
                    <PrimaryInput
                        label="Optional starting page (1–604)"
                        value={value == null ? "" : String(value)}
                        keyboardType="number-pad"
                        error={fieldState.error?.message}
                        onChangeText={(text) => onChange(parseOptionalInt(text))}
                    />
                )}
            />

            {formError ? <Text style={styles.error}>{formError}</Text> : null}

            <PrimaryButton
                title="Save daily goal"
                loading={isBusy}
                disabled={isBusy}
                onPress={handleSubmit(onSubmit)}
            />
        </ScreenContainer>
    );
}

const styles = StyleSheet.create({
    content: {
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
        fontSize: typography.h3,
        fontWeight: "700",
    },

    subtitle: {
        color: colors.textSecondary,
        fontSize: typography.body,
    },

    backButton: {
        alignSelf: "flex-start",
        minHeight: 40,
        paddingHorizontal: spacing.md,
        marginTop: spacing.xs,
    },

    error: {
        color: colors.error,
        fontSize: typography.body,
        textAlign: "center",
    },
});
