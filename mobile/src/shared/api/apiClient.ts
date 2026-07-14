import axios, {
    AxiosError,
    AxiosInstance,
    InternalAxiosRequestConfig,
} from "axios";

import {
    clearTokens,
    getAccessToken,
    getRefreshToken,
    saveTokens,
} from "../storage/tokenStorage";
import { resolveApiBaseUrl } from "./resolveApiBaseUrl";

const baseURL = resolveApiBaseUrl();

if (__DEV__) {
    console.log(`[api] baseURL=${baseURL || "(missing)"}`);
}

if (!baseURL) {
    console.warn(
        "EXPO_PUBLIC_API_URL is not set. Create mobile/.env from .env.example.",
    );
} else if (
    baseURL.includes("YOUR_COMPUTER_IP")
    || baseURL.includes("localhost")
    || baseURL.includes("127.0.0.1")
) {
    console.warn(
        `EXPO_PUBLIC_API_URL is "${baseURL}". On a physical phone use your PC Wi-Fi IP, start Expo with --lan, then: npx expo start -c --lan`,
    );
}

export const apiClient: AxiosInstance = axios.create({
    baseURL,
    timeout: 20000,
    headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
    },
});

export function getApiBaseUrl(): string {
    return baseURL;
}
type RetriableRequestConfig = InternalAxiosRequestConfig & {
    _retry?: boolean;
};

let isRefreshing = false;
let refreshWaitQueue: Array<(token: string | null) => void> = [];
let onSessionExpired: (() => void) | null = null;

export function setSessionExpiredHandler(handler: (() => void) | null): void {
    onSessionExpired = handler;
}

function notifyRefreshWaiters(token: string | null): void {
    refreshWaitQueue.forEach((resolve) => resolve(token));
    refreshWaitQueue = [];
}

function isAuthExemptPath(url?: string): boolean {
    if (!url) {
        return false;
    }

    return (
        url.includes("/api/v1/auth/login")
        || url.includes("/api/v1/auth/register")
        || url.includes("/api/v1/auth/refresh")
        || url.includes("/api/v1/auth/logout")
    );
}

async function refreshAccessToken(): Promise<string | null> {
    const storedRefreshToken = await getRefreshToken();

    if (!storedRefreshToken) {
        return null;
    }

    try {
        const { data } = await axios.post(
            `${baseURL}/api/v1/auth/refresh`,
            { refreshToken: storedRefreshToken },
            {
                headers: {
                    "Content-Type": "application/json",
                    Accept: "application/json",
                },
                timeout: 15000,
            },
        );

        await saveTokens(data.accessToken, data.refreshToken);
        return data.accessToken as string;
    } catch {
        await clearTokens();
        onSessionExpired?.();
        return null;
    }
}

apiClient.interceptors.request.use(async (config) => {
    if (isAuthExemptPath(config.url)) {
        return config;
    }

    const accessToken = await getAccessToken();
    if (accessToken) {
        config.headers.Authorization = `Bearer ${accessToken}`;
    }

    return config;
});

apiClient.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
        const originalRequest = error.config as RetriableRequestConfig | undefined;

        if (
            !originalRequest
            || error.response?.status !== 401
            || originalRequest._retry
            || isAuthExemptPath(originalRequest.url)
        ) {
            return Promise.reject(error);
        }

        if (isRefreshing) {
            return new Promise((resolve, reject) => {
                refreshWaitQueue.push((token) => {
                    if (!token) {
                        reject(error);
                        return;
                    }

                    originalRequest.headers.Authorization = `Bearer ${token}`;
                    resolve(apiClient(originalRequest));
                });
            });
        }

        originalRequest._retry = true;
        isRefreshing = true;

        try {
            const newAccessToken = await refreshAccessToken();
            notifyRefreshWaiters(newAccessToken);

            if (!newAccessToken) {
                return Promise.reject(error);
            }

            originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
            return apiClient(originalRequest);
        } finally {
            isRefreshing = false;
        }
    },
);
