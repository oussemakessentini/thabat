import React, { useCallback } from "react";
import {
    ActivityIndicator,
    FlatList,
    Pressable,
    RefreshControl,
    StatusBar,
    StyleSheet,
    Text,
    View,
} from "react-native";

import type { AppStackScreenProps } from "../../../navigation/navigation.types";
import { parseApiError } from "../../../shared/api/parseApiError";
import { PrimaryButton } from "../../../shared/components/button/PrimaryButton";
import { ScreenContainer } from "../../../shared/components/layout/ScreenContainer";
import {
    colors,
    radius,
    spacing,
    typography,
} from "../../../shared/theme";
import { QuranAggregateMeta } from "../components/QuranAggregateMeta";
import { useQuranSectionDetail } from "../hooks/useQuranSectionDetail";
import type { QuranSectionPage } from "../types/quran.types";
import { QURAN_STATUS_LABELS } from "../types/quran.types";
import { quranStatusColor } from "../utils/quranStatus.styles";

type Props = AppStackScreenProps<"QuranSectionDetail">;

export function QuranSectionDetailScreen({
    navigation,
    route,
}: Props): React.JSX.Element {
    const { sectionType, sectionNumber } = route.params;
    const detailQuery = useQuranSectionDetail(sectionType, sectionNumber);

    const onPressPage = useCallback(
        (pageNumber: number) => {
            navigation.navigate("QuranPageDetail", { pageNumber });
        },
        [navigation],
    );

    if (detailQuery.isLoading) {
        return (
            <View style={styles.centered}>
                <ActivityIndicator color={colors.primary} size="large" />
            </View>
        );
    }

    if (detailQuery.isError || !detailQuery.data) {
        return (
            <ScreenContainer contentContainerStyle={styles.content}>
                <Text style={styles.error}>
                    {detailQuery.error
                        ? parseApiError(detailQuery.error).message
                        : "Unable to load section"}
                </Text>
                <PrimaryButton
                    title="Retry"
                    onPress={() => void detailQuery.refetch()}
                />
                <PrimaryButton title="Back" onPress={() => navigation.goBack()} />
            </ScreenContainer>
        );
    }

    const detail = detailQuery.data;

    return (
        <ScreenContainer contentContainerStyle={styles.content}>
            <StatusBar barStyle="dark-content" />

            <View style={styles.header}>
                <Text style={styles.title}>{detail.title}</Text>
                <PrimaryButton
                    fullWidth={false}
                    onPress={() => navigation.goBack()}
                    style={styles.backButton}
                    title="Back"
                />
            </View>

            {detail.nameArabic || detail.nameEnglish ? (
                <Text style={styles.meta}>
                    {[detail.nameArabic, detail.nameEnglish, detail.revelationType]
                        .filter(Boolean)
                        .join(" · ")}
                </Text>
            ) : null}

            <Text style={styles.meta}>
                Pages {detail.startPage}–{detail.endPage}
                {detail.ayahCount != null ? ` · ${detail.ayahCount} ayahs` : ""}
            </Text>

            <QuranAggregateMeta aggregate={detail} />

            <Text style={styles.note}>
                Progress is page-based and calculated by Quran Service. Update pages
                to change this section.
            </Text>

            <FlatList
                data={detail.pages}
                keyExtractor={(item) => String(item.pageNumber)}
                initialNumToRender={24}
                contentContainerStyle={styles.list}
                refreshControl={
                    <RefreshControl
                        refreshing={detailQuery.isRefetching}
                        onRefresh={() => void detailQuery.refetch()}
                        tintColor={colors.primary}
                    />
                }
                renderItem={({ item }: { item: QuranSectionPage }) => (
                    <Pressable
                        accessibilityLabel={`Page ${item.pageNumber}, ${QURAN_STATUS_LABELS[item.status]}`}
                        accessibilityRole="button"
                        onPress={() => onPressPage(item.pageNumber)}
                        style={({ pressed }) => [
                            styles.pageRow,
                            pressed && styles.pressed,
                        ]}
                    >
                        <View style={styles.pageMain}>
                            <Text style={styles.pageTitle}>
                                Page {item.pageNumber}
                            </Text>
                            {item.startAyah != null && item.endAyah != null ? (
                                <Text style={styles.pageMeta}>
                                    Ayahs {item.startAyah}–{item.endAyah}
                                </Text>
                            ) : null}
                        </View>
                        <Text
                            style={[
                                styles.status,
                                { color: quranStatusColor(item.status) },
                            ]}
                        >
                            {QURAN_STATUS_LABELS[item.status]}
                        </Text>
                    </Pressable>
                )}
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
        flex: 1,
        paddingHorizontal: spacing.lg,
        paddingTop: spacing.md,
        paddingBottom: spacing.md,
        gap: spacing.sm,
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
        fontSize: typography.h3,
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

    note: {
        color: colors.textSecondary,
        fontSize: typography.small,
        marginBottom: spacing.sm,
    },

    list: {
        paddingBottom: spacing.xl,
        gap: spacing.sm,
    },

    pageRow: {
        minHeight: 56,
        paddingHorizontal: spacing.md,
        paddingVertical: spacing.sm,
        borderWidth: 1,
        borderColor: colors.border,
        borderRadius: radius.lg,
        backgroundColor: colors.surface,
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
        gap: spacing.sm,
    },

    pressed: {
        opacity: 0.8,
    },

    pageMain: {
        flex: 1,
        gap: spacing.xs / 2,
    },

    pageTitle: {
        color: colors.textPrimary,
        fontSize: typography.body,
        fontWeight: "700",
    },

    pageMeta: {
        color: colors.textSecondary,
        fontSize: typography.small,
    },

    status: {
        fontSize: typography.caption,
        fontWeight: "700",
    },

    error: {
        color: colors.error,
        textAlign: "center",
    },
});
