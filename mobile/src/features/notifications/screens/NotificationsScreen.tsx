import { useQueryClient } from "@tanstack/react-query";
import React, { useCallback, useEffect, useMemo } from "react";
import {
    ActivityIndicator,
    FlatList,
    RefreshControl,
    StatusBar,
    StyleSheet,
    Text,
    View,
} from "react-native";

import type { AppStackScreenProps } from "../../../navigation/navigation.types";
import { parseApiError } from "../../../shared/api/parseApiError";
import { PrimaryButton } from "../../../shared/components/button/PrimaryButton";
import { ScreenContainer } from "../../../shared/components/layout/ScreenContainer";
import { colors, spacing, typography } from "../../../shared/theme";
import { NotificationCard } from "../components/NotificationCard";
import {
    useMarkAllNotificationsRead,
    useMarkNotificationRead,
} from "../hooks/useMarkNotificationRead";
import { notificationsUnreadCountQueryKey } from "../hooks/notificationsQueryKeys";
import { useNotifications } from "../hooks/useNotifications";
import type { AppNotification } from "../types/notification.types";

type Props = AppStackScreenProps<"Notifications">;

export function NotificationsScreen({ navigation }: Props): React.JSX.Element {
    const queryClient = useQueryClient();
    const query = useNotifications(true);
    const markRead = useMarkNotificationRead();
    const markAll = useMarkAllNotificationsRead();

    useEffect(() => {
        void queryClient.invalidateQueries({
            queryKey: notificationsUnreadCountQueryKey,
        });
    }, [queryClient]);

    const items = useMemo(
        () => query.data?.pages.flatMap((page) => page.content) ?? [],
        [query.data],
    );

    const onRefresh = useCallback(() => {
        void query.refetch();
    }, [query]);

    const onPressItem = useCallback(
        (notification: AppNotification) => {
            if (!notification.read) {
                void markRead.mutateAsync(notification.id);
            }
        },
        [markRead],
    );

    return (
        <ScreenContainer contentContainerStyle={styles.container}>
            <StatusBar barStyle="dark-content" />

            <View style={styles.header}>
                <Text style={styles.title}>Notifications</Text>
                <PrimaryButton
                    title="Back"
                    fullWidth={false}
                    onPress={() => navigation.goBack()}
                    style={styles.backButton}
                />
            </View>

            <PrimaryButton
                title="Mark all as read"
                loading={markAll.isPending}
                disabled={markAll.isPending || items.length === 0}
                onPress={() => {
                    void markAll.mutateAsync();
                }}
            />

            {query.isLoading ? (
                <ActivityIndicator color={colors.primary} style={styles.loader} />
            ) : query.isError ? (
                <View style={styles.stateBox}>
                    <Text style={styles.error}>
                        {parseApiError(query.error).message}
                    </Text>
                    <PrimaryButton title="Retry" onPress={onRefresh} />
                </View>
            ) : items.length === 0 ? (
                <Text style={styles.empty}>No notifications yet.</Text>
            ) : (
                <FlatList
                    data={items}
                    keyExtractor={(item) => item.id}
                    contentContainerStyle={styles.list}
                    refreshControl={
                        <RefreshControl
                            refreshing={query.isRefetching}
                            onRefresh={onRefresh}
                            tintColor={colors.primary}
                        />
                    }
                    onEndReached={() => {
                        if (query.hasNextPage && !query.isFetchingNextPage) {
                            void query.fetchNextPage();
                        }
                    }}
                    onEndReachedThreshold={0.4}
                    renderItem={({ item }) => (
                        <NotificationCard
                            notification={item}
                            onPress={onPressItem}
                        />
                    )}
                    ListFooterComponent={
                        query.isFetchingNextPage ? (
                            <ActivityIndicator color={colors.primary} />
                        ) : null
                    }
                />
            )}
        </ScreenContainer>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        paddingHorizontal: spacing.lg,
        paddingTop: spacing.md,
        paddingBottom: spacing.md,
        gap: spacing.md,
    },

    header: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
        gap: spacing.sm,
    },

    title: {
        flex: 1,
        color: colors.primary,
        fontSize: typography.h3,
        fontWeight: "700",
    },

    backButton: {
        minHeight: 40,
        paddingHorizontal: spacing.md,
    },

    loader: {
        marginTop: spacing.xl,
    },

    stateBox: {
        gap: spacing.md,
        marginTop: spacing.lg,
    },

    list: {
        gap: spacing.sm,
        paddingBottom: spacing.xl,
    },

    empty: {
        color: colors.textSecondary,
        textAlign: "center",
        marginTop: spacing.xl,
    },

    error: {
        color: colors.error,
        textAlign: "center",
    },
});
