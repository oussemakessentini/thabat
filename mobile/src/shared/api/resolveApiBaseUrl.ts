import Constants from "expo-constants";
import { Platform } from "react-native";

/**
 * Resolves the API Gateway base URL for the current runtime.
 * On a physical device in Expo LAN mode, prefers the same host Expo uses.
 */
export function resolveApiBaseUrl(): string {
    const envUrl = (process.env.EXPO_PUBLIC_API_URL ?? "").replace(/\/$/, "");

    if (Platform.OS === "web") {
        return envUrl;
    }

    const hostUri =
        Constants.expoConfig?.hostUri
        ?? Constants.expoGoConfig?.debuggerHost
        ?? (
            Constants as {
                manifest?: { debuggerHost?: string };
            }
        ).manifest?.debuggerHost;

    if (!hostUri) {
        return envUrl;
    }

    const host = hostUri.split(":")[0]?.trim();

    if (
        !host
        || host === "localhost"
        || host === "127.0.0.1"
        || host.includes("exp.direct")
        || host.includes("ngrok")
    ) {
        return envUrl;
    }

    return `http://${host}:8080`;
}
