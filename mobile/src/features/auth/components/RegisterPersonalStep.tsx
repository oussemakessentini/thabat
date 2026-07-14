import React from "react";
import { Control, Controller } from "react-hook-form";
import { StyleSheet, View } from "react-native";

import { PrimaryButton } from "../../../shared/components/button/PrimaryButton";
import { PrimaryInput } from "../../../shared/components/input/PrimaryInput";
import { spacing } from "../../../shared/theme";
import type { RegisterFormValues } from "../types/register.types";
import { DateOfBirthField } from "./DateOfBirthField";

type RegisterPersonalStepProps = {
    control: Control<RegisterFormValues>;
    disabled?: boolean;
    onContinue: () => void;
};

export function RegisterPersonalStep({
    control,
    disabled = false,
    onContinue,
}: RegisterPersonalStepProps): React.JSX.Element {
    return (
        <View style={styles.form}>
            <Controller
                control={control}
                name="firstName"
                render={({ field: { onChange, value }, fieldState: { error } }) => (
                    <PrimaryInput
                        autoCapitalize="words"
                        editable={!disabled}
                        error={error?.message}
                        label="First Name"
                        onChangeText={onChange}
                        placeholder="Enter your first name"
                        value={value}
                    />
                )}
            />

            <Controller
                control={control}
                name="lastName"
                render={({ field: { onChange, value }, fieldState: { error } }) => (
                    <PrimaryInput
                        autoCapitalize="words"
                        editable={!disabled}
                        error={error?.message}
                        label="Last Name"
                        onChangeText={onChange}
                        placeholder="Enter your last name"
                        value={value}
                    />
                )}
            />

            <Controller
                control={control}
                name="dateOfBirth"
                render={({ field: { onChange, value }, fieldState: { error } }) => (
                    <DateOfBirthField
                        editable={!disabled}
                        error={error?.message}
                        onChange={onChange}
                        value={value}
                    />
                )}
            />

            <PrimaryButton
                disabled={disabled}
                onPress={onContinue}
                style={styles.continueButton}
                title="Continue"
            />
        </View>
    );
}

const styles = StyleSheet.create({
    form: {
        marginTop: spacing.lg,
        gap: spacing.md,
    },

    continueButton: {
        marginTop: spacing.sm,
    },
});
