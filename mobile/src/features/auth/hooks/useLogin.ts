import { useMutation } from "@tanstack/react-query";

import { login } from "../api/auth.api";
import type { AuthResponse, LoginRequest } from "../api/auth.types";

export function useLogin() {
    return useMutation<AuthResponse, unknown, LoginRequest>({
        mutationFn: login,
    });
}
