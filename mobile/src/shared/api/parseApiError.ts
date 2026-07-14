import axios from "axios";

import { resolveApiBaseUrl } from "./resolveApiBaseUrl";

export type ApiErrorBody = {
    timestamp?: string;
    status?: number;
    error?: string;
    message?: string;
    path?: string;
    validationErrors?: Record<string, string>;
};

export type ParsedApiError = {
    status?: number;
    message: string;
    validationErrors: Record<string, string>;
    isConflict: boolean;
    isValidationError: boolean;
    isNetworkError: boolean;
};

const DEFAULT_MESSAGE = "Something went wrong. Please try again.";

export function parseApiError(error: unknown): ParsedApiError {
    if (axios.isAxiosError(error)) {
        if (!error.response) {
            const apiUrl = resolveApiBaseUrl() || "unknown URL";
            return {
                message:
                    `Unable to reach the server at ${apiUrl}. `
                    + "On your phone open that host /actuator/health in the browser. "
                    + "Use the same Wi‑Fi, start Expo with --lan (not tunnel), "
                    + "and keep Windows Firewall open for port 8080.",
                validationErrors: {},
                isConflict: false,
                isValidationError: false,
                isNetworkError: true,
            };
        }

        const status = error.response.status;
        const body = error.response.data as ApiErrorBody | undefined;
        const validationErrors = body?.validationErrors ?? {};

        let message = DEFAULT_MESSAGE;
        if (typeof body?.message === "string" && body.message.trim().length > 0) {
            message = body.message;
        } else if (status === 404) {
            message =
                "Journey service was not found through the gateway. "
                + "Restart gateway-service after rebuilding so /api/v1/journey/** is routed.";
        } else if (typeof body?.error === "string" && body.error.trim().length > 0) {
            message = `${body.error} (${status})`;
        } else if (status) {
            message = `Request failed (${status}). Please try again.`;
        }

        return {
            status,
            message,
            validationErrors,
            isConflict: status === 409,
            isValidationError:
                status === 400 && Object.keys(validationErrors).length > 0,
            isNetworkError: false,
        };
    }

    return {
        message: DEFAULT_MESSAGE,
        validationErrors: {},
        isConflict: false,
        isValidationError: false,
        isNetworkError: false,
    };
}
