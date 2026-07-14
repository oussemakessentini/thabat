import React from "react";
import {
    StatusBar,
    StyleSheet,
    Text,
    View,
} from "react-native";

import type { RootStackScreenProps } from "../../../navigation/navigation.types";
import { PrimaryButton } from "../../../shared/components/button/PrimaryButton";
import { ScreenContainer } from "../../../shared/components/layout/ScreenContainer";
import { Logo } from "../../../shared/components/logo/Logo";
import {
    colors,
    radius,
    spacing,
    typography,
} from "../../../shared/theme";

type SplashScreenProps = RootStackScreenProps<"Splash">;

export function SplashScreen({
    navigation,
}: SplashScreenProps): React.JSX.Element {
    return (
        <ScreenContainer contentContainerStyle={styles.content}>
            <StatusBar barStyle="dark-content" />

            <Logo size="large" />

            <View style={styles.journey}>
                <View style={styles.sun} />
                <View style={styles.road}>
                    <View style={styles.traveler} />
                </View>
            </View>

            <Text style={styles.tagline}>Small steps. Lasting consistency.</Text>

            <PrimaryButton
                title="Start Journey"
                onPress={() => navigation.navigate("Login")}
                style={styles.button}
            />
        </ScreenContainer>
    );
}

const styles = StyleSheet.create({
    content: {
        flexGrow: 1,
        alignItems: "center",
        justifyContent: "center",
        paddingHorizontal: spacing.lg,
        paddingVertical: spacing.xl,
    },

    journey: {
        width: "100%",
        height: 150,
        marginTop: spacing.lg,
        justifyContent: "flex-end",
        alignItems: "center",
    },

    sun: {
        position: "absolute",
        top: spacing.sm,
        right: spacing.xxxl,
        width: 42,
        height: 42,
        borderRadius: radius.full,
        backgroundColor: colors.warning,
    },

    road: {
        width: "82%",
        height: spacing.sm,
        borderRadius: radius.full,
        backgroundColor: colors.muted,
        justifyContent: "center",
    },

    traveler: {
        width: 28,
        height: 28,
        borderRadius: radius.full,
        backgroundColor: colors.primary,
        marginLeft: "42%",
        marginTop: -spacing.lg,
    },

    tagline: {
        marginTop: spacing.xl,
        color: colors.textSecondary,
        fontSize: typography.body,
        fontWeight: "600",
        textAlign: "center",
    },

    button: {
        marginTop: spacing.xl,
    },
});
