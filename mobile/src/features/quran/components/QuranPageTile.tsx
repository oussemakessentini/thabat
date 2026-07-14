import React, { memo } from "react";
import { Pressable, StyleSheet, Text } from "react-native";

import { colors, radius, spacing, typography } from "../../../shared/theme";
import type { QuranPageStatus } from "../types/quran.types";
import { QURAN_STATUS_LABELS } from "../types/quran.types";
import { quranStatusColor } from "../utils/quranStatus.styles";

type Props = {
    pageNumber: number;
    status: QuranPageStatus;
    onPress: (pageNumber: number) => void;
    size: number;
};

function QuranPageTileComponent({
    pageNumber,
    status,
    onPress,
    size,
}: Props): React.JSX.Element {
    const tone = quranStatusColor(status);

    return (
        <Pressable
            accessibilityLabel={`Page ${pageNumber}, ${QURAN_STATUS_LABELS[status]}`}
            accessibilityRole="button"
            onPress={() => onPress(pageNumber)}
            style={({ pressed }) => [
                styles.tile,
                {
                    width: size,
                    height: size,
                    backgroundColor: tone,
                    opacity: pressed ? 0.75 : 1,
                    borderColor: pressed ? colors.primaryDark : colors.border,
                },
            ]}
        >
            <Text style={styles.pageNumber}>{pageNumber}</Text>
        </Pressable>
    );
}

export const QuranPageTile = memo(QuranPageTileComponent);

const styles = StyleSheet.create({
    tile: {
        minWidth: 44,
        minHeight: 44,
        margin: spacing.xs / 2,
        borderRadius: radius.sm,
        borderWidth: 1,
        alignItems: "center",
        justifyContent: "center",
    },

    pageNumber: {
        color: colors.textPrimary,
        fontSize: typography.small,
        fontWeight: "700",
    },
});
