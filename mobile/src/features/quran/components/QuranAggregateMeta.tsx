import React from "react";
import { StyleSheet, Text, View } from "react-native";

import { colors, spacing, typography } from "../../../shared/theme";
import type { QuranAggregateFields } from "../types/quran.types";

type Props = {
    aggregate: QuranAggregateFields;
};

export function QuranAggregateMeta({ aggregate }: Props): React.JSX.Element {
    return (
        <View style={styles.wrap}>
            <Text style={styles.meta}>
                {aggregate.completedPages}/{aggregate.totalPages} completed (
                {aggregate.completionPercentage.toFixed(2)}%)
            </Text>
            <Text style={styles.meta}>
                Not started {aggregate.notStartedPages} · Learning{" "}
                {aggregate.learningPages} · Memorized {aggregate.memorizedPages}
            </Text>
            <Text style={styles.meta}>
                Needs revision {aggregate.needsRevisionPages} · Strong{" "}
                {aggregate.strongPages}
            </Text>
        </View>
    );
}

const styles = StyleSheet.create({
    wrap: {
        gap: spacing.xs / 2,
    },

    meta: {
        color: colors.textSecondary,
        fontSize: typography.small,
    },
});
