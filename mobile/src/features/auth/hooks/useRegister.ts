import { useMutation } from "@tanstack/react-query";

import { registerUser } from "../api/register.api";
import type { RegisterRequest, RegisterResponse } from "../api/register.types";

export function useRegister() {
    return useMutation<RegisterResponse, unknown, RegisterRequest>({
        mutationFn: registerUser,
    });
}
