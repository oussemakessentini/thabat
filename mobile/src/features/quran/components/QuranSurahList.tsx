import React from "react";
import {
    ActivityIndicator,
    FlatList,
    RefreshControl,
    StyleSheet,
    Text,
} from "react-native";

import { parseApiError } from "../../../shared/api/parseApiError";
import { colors, spacing } from "../../../shared/theme";
import { useQuranSurahs } from "../hooks/useQuranSurahs";
import type { QuranSurahProgress } from "../types/quran.types";
import { QuranSectionCard } from "./QuranSectionCard";

type Props = {
    onPressSurah: (surahNumber: number) => void;
};

export function QuranSurahList({ onPressSurah }: Props): React.JSX.Element {
    const query = useQuranSurahs(true);

    if (query.isLoading) {
        return <ActivityIndicator color={colors.primary} style={styles.loader} />;
    }

    if (query.isError) {
        return (
            <Text style={styles.error}>
                {parseApiError(query.error).message}
            </Text>
        );
    }

    return (
        <FlatList
            data={query.data ?? []}
            keyExtractor={(item) => String(item.surahNumber)}
            initialNumToRender={20}
            contentContainerStyle={styles.list}
            refreshControl={
                <RefreshControl
                    refreshing={query.isRefetching}
                    onRefresh={() => {
                        void query.refetch();
                    }}
                    tintColor={colors.primary}
                />
            }
            renderItem={({ item }: { item: QuranSurahProgress }) => (
                <QuranSectionCard
                    title={`${item.surahNumber}. ${item.transliteration}`}
                    subtitle={`${item.nameEnglish} · ${item.ayahCount} ayahs · ${item.revelationType}`}
                    trailing={item.nameArabic}
                    aggregate={item}
                    onPress={() => onPressSurah(item.surahNumber)}
                />
            )}
        />
    );
}

const styles = StyleSheet.create({
    loader: {
        marginTop: spacing.xl,
    },

    error: {
        color: colors.error,
        textAlign: "center",
        marginTop: spacing.lg,
    },

    list: {
        paddingBottom: spacing.xl,
        gap: spacing.sm,
    },
});
