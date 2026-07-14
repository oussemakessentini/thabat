import { zodResolver } from "@hookform/resolvers/zod";
import React, { useEffect, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import {
    ActivityIndicator,
    Pressable,
    StatusBar,
    StyleSheet,
    Text,
    View,
} from "react-native";

import type { AppStackScreenProps } from "../../../navigation/navigation.types";
import { parseApiError } from "../../../shared/api/parseApiError";
import { PrimaryButton } from "../../../shared/components/button/PrimaryButton";
import { PrimaryInput } from "../../../shared/components/input/PrimaryInput";
import { ScreenContainer } from "../../../shared/components/layout/ScreenContainer";
import {
    colors,
    radius,
    spacing,
    typography,
} from "../../../shared/theme";
import { formatDateForApi } from "../../auth/utils/date.utils";
import { QuranDateField } from "../components/QuranDateField";
import { useQuranPage } from "../hooks/useQuranPage";
import { useRecordQuranReview } from "../hooks/useRecordQuranReview";
import { useUpdateQuranPage } from "../hooks/useUpdateQuranPage";
import type { QuranPageStatus } from "../types/quran.types";
import { QURAN_STATUS_LABELS } from "../types/quran.types";
import {
    recordQuranReviewSchema,
    type RecordQuranReviewFormValues,
    updateQuranPageSchema,
    type UpdateQuranPageFormValues,
} from "../validation/quranPage.schema";

type Props = AppStackScreenProps<"QuranPageDetail">;

const STATUSES = Object.keys(QURAN_STATUS_LABELS) as QuranPageStatus[];

export function QuranPageDetailScreen({
    navigation,
    route,
}: Props): React.JSX.Element {
    const { pageNumber } = route.params;
    const pageQuery = useQuranPage(pageNumber);
    const updateMutation = useUpdateQuranPage(pageNumber);
    const reviewMutation = useRecordQuranReview(pageNumber);
    const [successMessage, setSuccessMessage] = useState<string | undefined>();
    const [formError, setFormError] = useState<string | undefined>();

    const updateForm = useForm<UpdateQuranPageFormValues>({
        resolver: zodResolver(updateQuranPageSchema),
        defaultValues: {
            status: "NOT_STARTED",
            memorizedAt: "",
            confidenceLevel: "",
            notes: "",
        },
    });

    const reviewForm = useForm<RecordQuranReviewFormValues>({
        resolver: zodResolver(recordQuranReviewSchema),
        defaultValues: {
            reviewedAt: formatDateForApi(new Date()),
            successful: true,
            confidenceLevel: "",
            newStatus: "",
        },
    });

    useEffect(() => {
        if (!pageQuery.data) {
            return;
        }

        updateForm.reset({
            status: pageQuery.data.status,
            memorizedAt: pageQuery.data.memorizedAt ?? "",
            confidenceLevel:
                pageQuery.data.confidenceLevel != null
                    ? String(pageQuery.data.confidenceLevel)
                    : "",
            notes: pageQuery.data.notes ?? "",
        });
    }, [pageQuery.data, updateForm]);

    const saveProgress = updateForm.handleSubmit(async (values) => {
        if (updateMutation.isPending) {
            return;
        }

        setFormError(undefined);
        setSuccessMessage(undefined);

        try {
            await updateMutation.mutateAsync({
                status: values.status,
                memorizedAt: values.memorizedAt || null,
                confidenceLevel:
                    values.confidenceLevel && values.confidenceLevel !== ""
                        ? Number(values.confidenceLevel)
                        : null,
                notes: values.notes?.trim() ? values.notes.trim() : null,
            });
            setSuccessMessage("Page progress saved.");
        } catch (error) {
            const parsed = parseApiError(error);
            setFormError(parsed.message);
        }
    });

    const saveReview = reviewForm.handleSubmit(async (values) => {
        if (reviewMutation.isPending) {
            return;
        }

        setFormError(undefined);
        setSuccessMessage(undefined);

        try {
            await reviewMutation.mutateAsync({
                reviewedAt: values.reviewedAt,
                successful: values.successful,
                confidenceLevel:
                    values.confidenceLevel && values.confidenceLevel !== ""
                        ? Number(values.confidenceLevel)
                        : null,
                newStatus:
                    values.newStatus && values.newStatus.length > 0
                        ? (values.newStatus as QuranPageStatus)
                        : null,
            });
            setSuccessMessage("Review recorded.");
        } catch (error) {
            const parsed = parseApiError(error);
            setFormError(parsed.message);
        }
    });

    if (pageQuery.isLoading) {
        return (
            <View style={styles.centered}>
                <ActivityIndicator color={colors.primary} size="large" />
            </View>
        );
    }

    if (pageQuery.isError || !pageQuery.data) {
        return (
            <ScreenContainer contentContainerStyle={styles.content}>
                <Text style={styles.error}>
                    {pageQuery.error
                        ? parseApiError(pageQuery.error).message
                        : "Unable to load page"}
                </Text>
                <PrimaryButton title="Back" onPress={() => navigation.goBack()} />
            </ScreenContainer>
        );
    }

    const page = pageQuery.data;
    const today = new Date();

    return (
        <ScreenContainer scrollable contentContainerStyle={styles.content}>
            <StatusBar barStyle="dark-content" />

            <View style={styles.header}>
                <Text style={styles.title}>Page {pageNumber}</Text>
                <PrimaryButton
                    fullWidth={false}
                    onPress={() => navigation.goBack()}
                    style={styles.backButton}
                    title="Back"
                />
            </View>

            <Text style={styles.meta}>
                Current status: {QURAN_STATUS_LABELS[page.status]}
            </Text>
            <Text style={styles.meta}>
                Memorized: {page.memorizedAt ?? "—"}
            </Text>
            <Text style={styles.meta}>
                Last review: {page.lastReviewedAt ?? "—"}
            </Text>
            <Text style={styles.meta}>
                Successful reviews: {page.successfulReviewCount}
            </Text>
            <Text style={styles.meta}>
                Confidence: {page.confidenceLevel ?? "—"}
            </Text>
            <Text style={styles.meta}>Notes: {page.notes ?? "—"}</Text>

            {successMessage ? (
                <Text style={styles.success}>{successMessage}</Text>
            ) : null}
            {formError ? <Text style={styles.error}>{formError}</Text> : null}

            <Text style={styles.section}>Update progress</Text>

            <Controller
                control={updateForm.control}
                name="status"
                render={({ field: { value, onChange } }) => (
                    <View style={styles.chips}>
                        {STATUSES.map((status) => {
                            const selected = value === status;
                            return (
                                <Pressable
                                    key={status}
                                    onPress={() => onChange(status)}
                                    style={[
                                        styles.chip,
                                        selected && styles.chipSelected,
                                    ]}
                                >
                                    <Text
                                        style={[
                                            styles.chipText,
                                            selected && styles.chipTextSelected,
                                        ]}
                                    >
                                        {QURAN_STATUS_LABELS[status]}
                                    </Text>
                                </Pressable>
                            );
                        })}
                    </View>
                )}
            />

            <Controller
                control={updateForm.control}
                name="memorizedAt"
                render={({ field: { value, onChange }, fieldState }) => (
                    <QuranDateField
                        error={fieldState.error?.message}
                        label="Memorized date"
                        maximumDate={today}
                        onChange={onChange}
                        value={value ?? ""}
                    />
                )}
            />

            <Controller
                control={updateForm.control}
                name="confidenceLevel"
                render={({ field: { value, onChange }, fieldState }) => (
                    <PrimaryInput
                        error={fieldState.error?.message}
                        keyboardType="number-pad"
                        label="Confidence (1–5)"
                        onChangeText={onChange}
                        value={value ?? ""}
                    />
                )}
            />

            <Controller
                control={updateForm.control}
                name="notes"
                render={({ field: { value, onChange }, fieldState }) => (
                    <PrimaryInput
                        error={fieldState.error?.message}
                        label="Notes"
                        onChangeText={onChange}
                        value={value ?? ""}
                    />
                )}
            />

            <PrimaryButton
                loading={updateMutation.isPending}
                onPress={saveProgress}
                title="Save progress"
            />

            <Text style={styles.section}>Record review</Text>

            <Controller
                control={reviewForm.control}
                name="reviewedAt"
                render={({ field: { value, onChange }, fieldState }) => (
                    <QuranDateField
                        error={fieldState.error?.message}
                        label="Review date"
                        maximumDate={today}
                        onChange={onChange}
                        value={value}
                    />
                )}
            />

            <Controller
                control={reviewForm.control}
                name="successful"
                render={({ field: { value, onChange } }) => (
                    <View style={styles.chips}>
                        <Pressable
                            onPress={() => onChange(true)}
                            style={[styles.chip, value && styles.chipSelected]}
                        >
                            <Text
                                style={[
                                    styles.chipText,
                                    value && styles.chipTextSelected,
                                ]}
                            >
                                Successful
                            </Text>
                        </Pressable>
                        <Pressable
                            onPress={() => onChange(false)}
                            style={[styles.chip, !value && styles.chipSelected]}
                        >
                            <Text
                                style={[
                                    styles.chipText,
                                    !value && styles.chipTextSelected,
                                ]}
                            >
                                Needs more work
                            </Text>
                        </Pressable>
                    </View>
                )}
            />

            <Controller
                control={reviewForm.control}
                name="confidenceLevel"
                render={({ field: { value, onChange }, fieldState }) => (
                    <PrimaryInput
                        error={fieldState.error?.message}
                        keyboardType="number-pad"
                        label="Review confidence (1–5)"
                        onChangeText={onChange}
                        value={value ?? ""}
                    />
                )}
            />

            <Controller
                control={reviewForm.control}
                name="newStatus"
                render={({ field: { value, onChange } }) => {
                    const keepStatus = !value;
                    return (
                    <View style={styles.chips}>
                        <Pressable
                            onPress={() => onChange("")}
                            style={[
                                styles.chip,
                                keepStatus && styles.chipSelected,
                            ]}
                        >
                            <Text
                                style={[
                                    styles.chipText,
                                    keepStatus && styles.chipTextSelected,
                                ]}
                            >
                                Keep status
                            </Text>
                        </Pressable>
                        {STATUSES.map((status) => {
                            const selected = value === status;
                            return (
                                <Pressable
                                    key={status}
                                    onPress={() => onChange(status)}
                                    style={[
                                        styles.chip,
                                        selected && styles.chipSelected,
                                    ]}
                                >
                                    <Text
                                        style={[
                                            styles.chipText,
                                            selected && styles.chipTextSelected,
                                        ]}
                                    >
                                        {QURAN_STATUS_LABELS[status]}
                                    </Text>
                                </Pressable>
                            );
                        })}
                    </View>
                    );
                }}
            />

            <PrimaryButton
                loading={reviewMutation.isPending}
                onPress={saveReview}
                title="Record review"
            />
        </ScreenContainer>
    );
}

const styles = StyleSheet.create({
    centered: {
        flex: 1,
        alignItems: "center",
        justifyContent: "center",
        backgroundColor: colors.background,
    },

    content: {
        paddingHorizontal: spacing.lg,
        paddingVertical: spacing.lg,
        gap: spacing.md,
    },

    header: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
        gap: spacing.sm,
    },

    title: {
        flex: 1,
        color: colors.primary,
        fontSize: typography.h2,
        fontWeight: "700",
    },

    backButton: {
        minHeight: 44,
        paddingHorizontal: spacing.md,
    },

    meta: {
        color: colors.textSecondary,
        fontSize: typography.caption,
    },

    section: {
        marginTop: spacing.sm,
        color: colors.primary,
        fontSize: typography.body,
        fontWeight: "700",
    },

    chips: {
        flexDirection: "row",
        flexWrap: "wrap",
        gap: spacing.xs,
    },

    chip: {
        minHeight: 36,
        paddingHorizontal: spacing.sm,
        borderRadius: radius.full,
        borderWidth: 1,
        borderColor: colors.border,
        backgroundColor: colors.surface,
        justifyContent: "center",
    },

    chipSelected: {
        backgroundColor: colors.primary,
        borderColor: colors.primary,
    },

    chipText: {
        color: colors.textSecondary,
        fontSize: typography.small,
        fontWeight: "600",
    },

    chipTextSelected: {
        color: colors.surface,
    },

    success: {
        color: colors.success,
        fontSize: typography.caption,
        fontWeight: "600",
    },

    error: {
        color: colors.error,
        fontSize: typography.caption,
    },
});
