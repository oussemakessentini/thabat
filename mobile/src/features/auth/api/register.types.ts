export type RegisterRequest = {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
    dateOfBirth: string;
    countryCode: string;
    timezone: string;
    preferredLanguage: string;
};

export type RegisterResponse = {
    userId: string;
    email: string;
    dateOfBirth: string;
    message: string;
};
