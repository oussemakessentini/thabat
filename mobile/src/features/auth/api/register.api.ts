import { apiClient } from "../../../shared/api/apiClient";

import type { RegisterRequest, RegisterResponse } from "./register.types";

export async function registerUser(
    payload: RegisterRequest,
): Promise<RegisterResponse> {
    const { data } = await apiClient.post<RegisterResponse>(
        "/api/v1/auth/register",
        payload,
    );

    return data;
}
