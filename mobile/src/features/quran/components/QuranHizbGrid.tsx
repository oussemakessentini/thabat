import React, { useMemo } from "react";
import {
    ActivityIndicator,
    Dimensions,
    FlatList,
    RefreshControl,
    StyleSheet,
    Text,
} from "react-native";

import { parseApiError } from "../../../shared/api/parseApiError";
import { colors, spacing } from "../../../shared/theme";
import { useQuranHizbs } from "../hooks/useQuranHizbs";
import type { QuranHizbProgress } from "../types/quran.types";
import { QuranSectionCard } from "./QuranSectionCard";

type Props = {
    onPressHizb: (hizbNumber: number) => void;
};

const NUM_COLUMNS = 2;
const GAP = spacing.sm;

export function QuranHizbGrid({ onPressHizb }: Props): React.JSX.Element {
    const query = useQuranHizbs(true);
    const tileWidth = useMemo(() => {
        const width = Dimensions.get("window").width;
        return Math.floor((width - spacing.lg * 2 - GAP) / NUM_COLUMNS);
    }, []);

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
            keyExtractor={(item) => String(item.hizbNumber)}
            numColumns={NUM_COLUMNS}
            columnWrapperStyle={styles.row}
            contentContainerStyle={styles.list}
            initialNumToRender={16}
            refreshControl={
                <RefreshControl
                    refreshing={query.isRefetching}
                    onRefresh={() => {
                        void query.refetch();
                    }}
                    tintColor={colors.primary}
                />
            }
            renderItem={({ item }: { item: QuranHizbProgress }) => (
                <QuranSectionCard
                    title={`Hizb ${item.hizbNumber}`}
                    subtitle={`Juz ${item.juzNumber} · Pages ${item.startPage}–${item.endPage}`}
                    trailing={`${item.completionPercentage.toFixed(0)}%`}
                    aggregate={item}
                    width={tileWidth}
                    onPress={() => onPressHizb(item.hizbNumber)}
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
        gap: GAP,
    },

    row: {
        gap: GAP,
        marginBottom: GAP,
    },
});
