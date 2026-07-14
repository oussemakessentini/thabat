import React from "react";
import {
    StyleSheet,
    Text,
    View,
} from "react-native";

import {
    colors,
    spacing,
    typography,
} from "../../theme";

type LogoSize = "small" | "medium" | "large";

type LogoProps = {
    size?: LogoSize;
};

const sizeStyles: Record<
    LogoSize,
    {
        title: number;
        subtitle: number;
        tagline: number;
        letterSpacing: number;
        marginVertical: number;
        subtitleMarginTop: number;
        taglineMarginTop: number;
    }
> = {
    small: {
        title: typography.h3,
        subtitle: typography.caption,
        tagline: typography.small,
        letterSpacing: 2,
        marginVertical: spacing.md,
        subtitleMarginTop: spacing.xs,
        taglineMarginTop: spacing.sm,
    },
    medium: {
        title: typography.h2,
        subtitle: typography.body,
        tagline: typography.caption,
        letterSpacing: 3,
        marginVertical: spacing.lg,
        subtitleMarginTop: spacing.sm,
        taglineMarginTop: spacing.md,
    },
    large: {
        title: typography.h1,
        subtitle: typography.h3,
        tagline: typography.body,
        letterSpacing: 4,
        marginVertical: spacing.xl,
        subtitleMarginTop: spacing.sm,
        taglineMarginTop: spacing.md,
    },
};

export function Logo({ size = "large" }: LogoProps): React.JSX.Element {
    const config = sizeStyles[size];

    return (
        <View
            style={[
                styles.container,
                { marginVertical: config.marginVertical },
            ]}
        >
            <Text
                style={[
                    styles.title,
                    {
                        fontSize: config.title,
                        letterSpacing: config.letterSpacing,
                    },
                ]}
            >
                THABAT
            </Text>

            <Text
                style={[
                    styles.subtitle,
                    {
                        fontSize: config.subtitle,
                        marginTop: config.subtitleMarginTop,
                    },
                ]}
            >
                ثبات
            </Text>

            <Text
                style={[
                    styles.tagline,
                    {
                        fontSize: config.tagline,
                        marginTop: config.taglineMarginTop,
                    },
                ]}
            >
                Stay steadfast. Grow every day.
            </Text>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        alignItems: "center",
    },

    title: {
        color: colors.primary,
        fontWeight: "800",
    },

    subtitle: {
        color: colors.secondary,
        fontWeight: "700",
    },

    tagline: {
        color: colors.textSecondary,
        textAlign: "center",
    },
});
