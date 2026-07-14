import { apiClient } from "../../../shared/api/apiClient";

import type {
    AuthResponse,
    AuthUser,
    LoginRequest,
    LogoutRequest,
    RefreshTokenRequest,
} from "./auth.types";

export async function login(payload: LoginRequest): Promise<AuthResponse> {
    const { data } = await apiClient.post<AuthResponse>(
        "/api/v1/auth/login",
        payload,
    );
    return data;
}

export async function refreshToken(
    payload: RefreshTokenRequest,
): Promise<AuthResponse> {
    const { data } = await apiClient.post<AuthResponse>(
        "/api/v1/auth/refresh",
        payload,
    );
    return data;
}

export async function logout(payload: LogoutRequest): Promise<void> {
    await apiClient.post("/api/v1/auth/logout", payload);
}

export async function getCurrentUser(): Promise<AuthUser> {
    const { data } = await apiClient.get<AuthUser>("/api/v1/users/me");
    return data;
}
