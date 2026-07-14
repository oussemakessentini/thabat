import DateTimePicker, {
    DateTimePickerEvent,
} from "@react-native-community/datetimepicker";
import React, { useState } from "react";
import {
    Platform,
    Pressable,
    StyleSheet,
    Text,
    View,
} from "react-native";

import { PrimaryButton } from "../../../shared/components/button/PrimaryButton";
import {
    colors,
    radius,
    spacing,
    typography,
} from "../../../shared/theme";
import {
    formatDateForApi,
    formatDateOfBirthDisplay,
    MAX_AGE,
    MIN_AGE,
    parseApiDate,
    subtractYears,
} from "../utils/date.utils";

type DateOfBirthFieldProps = {
    value: string;
    onChange: (value: string) => void;
    error?: string;
    editable?: boolean;
};

export function DateOfBirthField({
    value,
    onChange,
    error,
    editable = true,
}: DateOfBirthFieldProps): React.JSX.Element {
    const [showPicker, setShowPicker] = useState(false);
    const hasError = Boolean(error);
    const minimumDate = subtractYears(MAX_AGE);
    const maximumDate = subtractYears(MIN_AGE);
    const selectedDate = parseApiDate(value);
    const pickerValue = selectedDate ?? subtractYears(18);

    const handleChange = (
        event: DateTimePickerEvent,
        date?: Date,
    ): void => {
        if (Platform.OS === "android") {
            setShowPicker(false);
        }

        if (event.type === "set" && date) {
            onChange(formatDateForApi(date));
        }
    };

    return (
        <View style={styles.wrapper}>
            <Text style={styles.label}>Date of Birth</Text>

            <Pressable
                accessibilityRole="button"
                disabled={!editable}
                onPress={() => setShowPicker(true)}
                style={[
                    styles.input,
                    hasError && styles.inputError,
                    !editable && styles.inputDisabled,
                ]}
            >
                <Text
                    style={[
                        styles.value,
                        !value && styles.placeholder,
                    ]}
                >
                    {value
                        ? formatDateOfBirthDisplay(value)
                        : "Select your date of birth"}
                </Text>
            </Pressable>

            {hasError ? <Text style={styles.error}>{error}</Text> : null}

            {showPicker ? (
                <DateTimePicker
                    display={Platform.OS === "ios" ? "spinner" : "default"}
                    maximumDate={maximumDate}
                    minimumDate={minimumDate}
                    mode="date"
                    onChange={handleChange}
                    value={pickerValue}
                />
            ) : null}

            {Platform.OS === "ios" && showPicker ? (
                <PrimaryButton
                    fullWidth={false}
                    onPress={() => setShowPicker(false)}
                    style={styles.doneButton}
                    title="Done"
                />
            ) : null}
        </View>
    );
}

const styles = StyleSheet.create({
    wrapper: {
        width: "100%",
    },

    label: {
        marginBottom: spacing.sm,
        color: colors.textPrimary,
        fontSize: typography.caption,
        fontWeight: "600",
    },

    input: {
        minHeight: 54,
        paddingHorizontal: spacing.md,
        borderWidth: 1,
        borderColor: colors.border,
        borderRadius: radius.lg,
        backgroundColor: colors.surface,
        justifyContent: "center",
    },

    inputError: {
        borderColor: colors.error,
    },

    inputDisabled: {
        backgroundColor: colors.muted,
    },

    value: {
        color: colors.textPrimary,
        fontSize: typography.body,
    },

    placeholder: {
        color: colors.textSecondary,
    },

    error: {
        marginTop: spacing.xs,
        color: colors.error,
        fontSize: typography.small,
    },

    doneButton: {
        alignSelf: "flex-end",
        marginTop: spacing.sm,
        minHeight: 44,
        paddingHorizontal: spacing.md,
    },
});
