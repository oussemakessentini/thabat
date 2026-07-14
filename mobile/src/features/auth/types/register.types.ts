export type RegisterFormValues = {
    firstName: string;
    lastName: string;
    dateOfBirth: string;
    email: string;
    password: string;
    confirmPassword: string;
};

export type RegisterStep = 1 | 2;