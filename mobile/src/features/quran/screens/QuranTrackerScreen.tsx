import React, { useCallback, useMemo, useState } from "react";
import {
    ActivityIndicator,
    Dimensions,
    FlatList,
    Pressable,
    RefreshControl,
    StatusBar,
    StyleSheet,
    Text,
    View,
} from "react-native";

import type { AppStackScreenProps } from "../../../navigation/navigation.types";
import { PrimaryButton } from "../../../shared/components/button/PrimaryButton";
import { ScreenContainer } from "../../../shared/components/layout/ScreenContainer";
import { parseApiError } from "../../../shared/api/parseApiError";
import {
    colors,
    radius,
    spacing,
    typography,
} from "../../../shared/theme";
import { QuranHizbGrid } from "../components/QuranHizbGrid";
import { QuranJuzGrid } from "../components/QuranJuzGrid";
import { QuranPageTile } from "../components/QuranPageTile";
import { QuranProgressSummary } from "../components/QuranProgressSummary";
import { QuranSurahList } from "../components/QuranSurahList";
import {
    QuranTrackerTabs,
    type QuranTrackerTab,
} from "../components/QuranTrackerTabs";
import { useQuranPages } from "../hooks/useQuranPages";
import { useQuranProgress } from "../hooks/useQuranProgress";
import type { QuranPageProgress, QuranPageStatus } from "../types/quran.types";
import { QURAN_STATUS_LABELS } from "../types/quran.types";
import { quranStatusColor } from "../utils/quranStatus.styles";

type Props = AppStackScreenProps<"QuranTracker">;

type FilterOption = "ALL" | QuranPageStatus;

const FILTERS: FilterOption[] = [
    "ALL",
    "NOT_STARTED",
    "LEARNING",
    "MEMORIZED",
    "NEEDS_REVISION",
    "STRONG",
];

const GAP = spacing.xs;
const HORIZONTAL_PADDING = spacing.lg * 2;
const NUM_COLUMNS = 6;

export function QuranTrackerScreen({ navigation }: Props): React.JSX.Element {
    const [tab, setTab] = useState<QuranTrackerTab>("Pages");
    const [filter, setFilter] = useState<FilterOption>("ALL");
    const statusFilter = filter === "ALL" ? undefined : filter;

    const pagesQuery = useQuranPages(statusFilter, tab === "Pages");
    const progressQuery = useQuranProgress(true);

    const tileSize = useMemo(() => {
        const width = Dimensions.get("window").width;
        const available = width - HORIZONTAL_PADDING - GAP * (NUM_COLUMNS - 1);
        return Math.max(44, Math.floor(available / NUM_COLUMNS));
    }, []);

    const onPressPage = useCallback(
        (pageNumber: number) => {
            navigation.navigate("QuranPageDetail", { pageNumber });
        },
        [navigation],
    );

    const renderItem = useCallback(
        ({ item }: { item: QuranPageProgress }) => (
            <QuranPageTile
                pageNumber={item.pageNumber}
                status={item.status}
                onPress={onPressPage}
                size={tileSize}
            />
        ),
        [onPressPage, tileSize],
    );

    const refreshing =
        progressQuery.isRefetching
        || (tab === "Pages" && pagesQuery.isRefetching);

    const onRefresh = useCallback(() => {
        void progressQuery.refetch();
        if (tab === "Pages") {
            void pagesQuery.refetch();
        }
    }, [tab, progressQuery, pagesQuery]);

    const pagesErrorMessage = pagesQuery.isError
        ? parseApiError(pagesQuery.error).message
        : progressQuery.isError
            ? parseApiError(progressQuery.error).message
            : null;

    return (
        <ScreenContainer contentContainerStyle={styles.container}>
            <StatusBar barStyle="dark-content" />

            <View style={styles.header}>
                <Text style={styles.title}>Quran tracker</Text>
                <PrimaryButton
                    fullWidth={false}
                    onPress={() => navigation.goBack()}
                    style={styles.backButton}
                    title="Back"
                />
            </View>

            <QuranProgressSummary summary={progressQuery.data} />
            <QuranTrackerTabs value={tab} onChange={setTab} />

            {tab === "Pages" ? (
                <>
                    <View style={styles.legend}>
                        {(Object.keys(QURAN_STATUS_LABELS) as QuranPageStatus[]).map(
                            (status) => (
                                <View key={status} style={styles.legendItem}>
                                    <View
                                        style={[
                                            styles.legendSwatch,
                                            {
                                                backgroundColor:
                                                    quranStatusColor(status),
                                            },
                                        ]}
                                    />
                                    <Text style={styles.legendLabel}>
                                        {QURAN_STATUS_LABELS[status]}
                                    </Text>
                                </View>
                            ),
                        )}
                    </View>

                    <View style={styles.filters}>
                        {FILTERS.map((option) => {
                            const selected = filter === option;
                            const label =
                                option === "ALL"
                                    ? "All"
                                    : QURAN_STATUS_LABELS[option];
                            return (
                                <Pressable
                                    key={option}
                                    onPress={() => setFilter(option)}
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
                                        {label}
                                    </Text>
                                </Pressable>
                            );
                        })}
                    </View>

                    {pagesQuery.isLoading ? (
                        <ActivityIndicator
                            color={colors.primary}
                            style={styles.loader}
                        />
                    ) : pagesErrorMessage ? (
                        <View style={styles.stateBox}>
                            <Text style={styles.error}>{pagesErrorMessage}</Text>
                            <PrimaryButton title="Retry" onPress={onRefresh} />
                        </View>
                    ) : !pagesQuery.data || pagesQuery.data.length === 0 ? (
                        <View style={styles.stateBox}>
                            <Text style={styles.empty}>
                                No pages match this filter.
                            </Text>
                        </View>
                    ) : (
                        <FlatList
                            data={pagesQuery.data}
                            keyExtractor={(item) => String(item.pageNumber)}
                            renderItem={renderItem}
                            numColumns={NUM_COLUMNS}
                            initialNumToRender={48}
                            maxToRenderPerBatch={48}
                            windowSize={11}
                            removeClippedSubviews
                            getItemLayout={(_, index) => ({
                                length: tileSize + GAP,
                                offset:
                                    Math.floor(index / NUM_COLUMNS)
                                    * (tileSize + GAP),
                                index,
                            })}
                            columnWrapperStyle={styles.row}
                            contentContainerStyle={styles.listContent}
                            refreshControl={
                                <RefreshControl
                                    refreshing={refreshing}
                                    onRefresh={onRefresh}
                                    tintColor={colors.primary}
                                />
                            }
                        />
                    )}
                </>
            ) : null}

            {tab === "Surahs" ? (
                <QuranSurahList
                    onPressSurah={(surahNumber) =>
                        navigation.navigate("QuranSectionDetail", {
                            sectionType: "SURAH",
                            sectionNumber: surahNumber,
                        })
                    }
                />
            ) : null}

            {tab === "Juz" ? (
                <QuranJuzGrid
                    onPressJuz={(juzNumber) =>
                        navigation.navigate("QuranSectionDetail", {
                            sectionType: "JUZ",
                            sectionNumber: juzNumber,
                        })
                    }
                />
            ) : null}

            {tab === "Hizb" ? (
                <QuranHizbGrid
                    onPressHizb={(hizbNumber) =>
                        navigation.navigate("QuranSectionDetail", {
                            sectionType: "HIZB",
                            sectionNumber: hizbNumber,
                        })
                    }
                />
            ) : null}
        </ScreenContainer>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        paddingHorizontal: spacing.lg,
        paddingTop: spacing.md,
        paddingBottom: spacing.md,
    },

    header: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
        marginBottom: spacing.md,
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

    legend: {
        flexDirection: "row",
        flexWrap: "wrap",
        gap: spacing.sm,
        marginBottom: spacing.md,
    },

    legendItem: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.xs,
    },

    legendSwatch: {
        width: 12,
        height: 12,
        borderRadius: radius.sm,
        borderWidth: 1,
        borderColor: colors.border,
    },

    legendLabel: {
        color: colors.textSecondary,
        fontSize: typography.small,
    },

    filters: {
        flexDirection: "row",
        flexWrap: "wrap",
        gap: spacing.xs,
        marginBottom: spacing.md,
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

    loader: {
        marginTop: spacing.xl,
    },

    stateBox: {
        gap: spacing.md,
        marginTop: spacing.lg,
    },

    error: {
        color: colors.error,
        fontSize: typography.body,
        textAlign: "center",
    },

    empty: {
        color: colors.textSecondary,
        fontSize: typography.body,
        textAlign: "center",
    },

    row: {
        gap: GAP,
        marginBottom: GAP,
    },

    listContent: {
        paddingBottom: spacing.xl,
    },
});
