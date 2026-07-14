import React from "react";
import {
    KeyboardAvoidingView,
    Platform,
    ScrollView,
    StyleProp,
    StyleSheet,
    View,
    ViewStyle,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

import { colors } from "../../theme";

type ScreenContainerProps = {
    children: React.ReactNode;
    scrollable?: boolean;
    contentContainerStyle?: StyleProp<ViewStyle>;
    style?: StyleProp<ViewStyle>;
};

export function ScreenContainer({
    children,
    scrollable = false,
    contentContainerStyle,
    style,
}: ScreenContainerProps): React.JSX.Element {
    const content = scrollable ? (
        <ScrollView
            contentContainerStyle={[styles.scrollContent, contentContainerStyle]}
            keyboardShouldPersistTaps="handled"
            showsVerticalScrollIndicator={false}
        >
            {children}
        </ScrollView>
    ) : (
        <View style={[styles.content, contentContainerStyle]}>{children}</View>
    );

    return (
        <SafeAreaView style={[styles.container, style]}>
            <KeyboardAvoidingView
                behavior={Platform.OS === "ios" ? "padding" : "height"}
                style={styles.keyboardAvoiding}
            >
                {content}
            </KeyboardAvoidingView>
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: colors.background,
    },

    keyboardAvoiding: {
        flex: 1,
    },

    content: {
        flex: 1,
    },

    scrollContent: {
        flexGrow: 1,
    },
});
