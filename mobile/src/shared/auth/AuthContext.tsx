import React, {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useMemo,
    useState,
} from "react";

import {
    getCurrentUser,
    logout as logoutRequest,
    refreshToken as refreshTokenRequest,
} from "../../features/auth/api/auth.api";
import type { AuthResponse, AuthUser } from "../../features/auth/api/auth.types";
import { setSessionExpiredHandler } from "../api/apiClient";
import {
    clearTokens,
    getAccessToken,
    getRefreshToken,
    saveTokens,
} from "../storage/tokenStorage";

type AuthContextValue = {
    user: AuthUser | null;
    isAuthenticated: boolean;
    isInitializing: boolean;
    signIn: (response: AuthResponse) => Promise<void>;
    signOut: () => Promise<void>;
    restoreSession: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

type AuthProviderProps = {
    children: React.ReactNode;
};

export function AuthProvider({ children }: AuthProviderProps): React.JSX.Element {
    const [user, setUser] = useState<AuthUser | null>(null);
    const [isInitializing, setIsInitializing] = useState(true);

    const signOut = useCallback(async (): Promise<void> => {
        const storedRefreshToken = await getRefreshToken();

        try {
            if (storedRefreshToken) {
                await logoutRequest({ refreshToken: storedRefreshToken });
            }
        } catch {
            // Always clear local session even if logout API fails.
        } finally {
            await clearTokens();
            setUser(null);
        }
    }, []);

    const signIn = useCallback(async (response: AuthResponse): Promise<void> => {
        await saveTokens(response.accessToken, response.refreshToken);
        setUser(response.user);
    }, []);

    const restoreSession = useCallback(async (): Promise<void> => {
        const accessToken = await getAccessToken();
        const storedRefreshToken = await getRefreshToken();

        if (!accessToken && !storedRefreshToken) {
            setUser(null);
            return;
        }

        if (accessToken) {
            try {
                const currentUser = await getCurrentUser();
                setUser(currentUser);
                return;
            } catch {
                // Fall through and attempt refresh.
            }
        }

        if (!storedRefreshToken) {
            await clearTokens();
            setUser(null);
            return;
        }

        try {
            const refreshed = await refreshTokenRequest({
                refreshToken: storedRefreshToken,
            });
            await saveTokens(refreshed.accessToken, refreshed.refreshToken);
            setUser(refreshed.user);
        } catch {
            await clearTokens();
            setUser(null);
        }
    }, []);

    useEffect(() => {
        let isMounted = true;

        const initialize = async (): Promise<void> => {
            try {
                await restoreSession();
            } finally {
                if (isMounted) {
                    setIsInitializing(false);
                }
            }
        };

        void initialize();

        return () => {
            isMounted = false;
        };
    }, [restoreSession]);

    useEffect(() => {
        setSessionExpiredHandler(() => {
            setUser(null);
        });

        return () => {
            setSessionExpiredHandler(null);
        };
    }, []);

    const value = useMemo<AuthContextValue>(
        () => ({
            user,
            isAuthenticated: user !== null,
            isInitializing,
            signIn,
            signOut,
            restoreSession,
        }),
        [user, isInitializing, signIn, signOut, restoreSession],
    );

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth(): AuthContextValue {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error("useAuth must be used within an AuthProvider");
    }

    return context;
}
