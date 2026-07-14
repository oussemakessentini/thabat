export type AuthUser = {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
    dateOfBirth: string;
    roles: string[];
};

export type LoginRequest = {
    email: string;
    password: string;
};

export type AuthResponse = {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    accessTokenExpiresIn: number;
    user: AuthUser;
};

export type RefreshTokenRequest = {
    refreshToken: string;
};

export type LogoutRequest = {
    refreshToken: string;
};
