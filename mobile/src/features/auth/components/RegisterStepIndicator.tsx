import React from "react";
import { StyleSheet, Text } from "react-native";

import { colors, spacing, typography } from "../../../shared/theme";
import type { RegisterStep } from "../types/register.types";

type RegisterStepIndicatorProps = {
    step: RegisterStep;
};

export function RegisterStepIndicator({
    step,
}: RegisterStepIndicatorProps): React.JSX.Element {
    return (
        <Text style={styles.text}>Step {step} of 2</Text>
    );
}

const styles = StyleSheet.create({
    text: {
        marginTop: spacing.md,
        color: colors.textSecondary,
        fontSize: typography.caption,
        fontWeight: "600",
        textAlign: "center",
    },
});
