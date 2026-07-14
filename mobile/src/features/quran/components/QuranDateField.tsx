import DateTimePicker, {
    DateTimePickerEvent,
} from "@react-native-community/datetimepicker";
import React, { useState } from "react";
import {
    Platform,
    Pressable,
    StyleSheet,
    Text,
    TextInput,
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
    parseApiDate,
} from "../../auth/utils/date.utils";

type Props = {
    label: string;
    value: string;
    onChange: (value: string) => void;
    error?: string;
    editable?: boolean;
    maximumDate?: Date;
};

export function QuranDateField({
    label,
    value,
    onChange,
    error,
    editable = true,
    maximumDate,
}: Props): React.JSX.Element {
    const [showPicker, setShowPicker] = useState(false);
    const hasError = Boolean(error);
    const selectedDate = parseApiDate(value);
    const pickerValue = selectedDate ?? new Date();
    const isWeb = Platform.OS === "web";

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

    if (isWeb) {
        return (
            <View style={styles.wrapper}>
                <Text style={styles.label}>{label}</Text>
                <TextInput
                    editable={editable}
                    onChangeText={onChange}
                    placeholder="YYYY-MM-DD"
                    placeholderTextColor={colors.textSecondary}
                    style={[
                        styles.input,
                        hasError && styles.inputError,
                        !editable && styles.inputDisabled,
                    ]}
                    value={value}
                />
                {hasError ? <Text style={styles.error}>{error}</Text> : null}
            </View>
        );
    }

    return (
        <View style={styles.wrapper}>
            <Text style={styles.label}>{label}</Text>

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
                <Text style={[styles.value, !value && styles.placeholder]}>
                    {value
                        ? formatDateOfBirthDisplay(value)
                        : "Select date"}
                </Text>
            </Pressable>

            {hasError ? <Text style={styles.error}>{error}</Text> : null}

            {showPicker ? (
                <DateTimePicker
                    display={Platform.OS === "ios" ? "spinner" : "default"}
                    maximumDate={maximumDate}
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
        color: colors.textPrimary,
        fontSize: typography.body,
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
