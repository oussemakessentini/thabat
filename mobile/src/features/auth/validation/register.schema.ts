import { z } from "zod";

import type { RegisterFormValues } from "../types/register.types";
import {
    calculateAge,
    isDateInPast,
    MAX_AGE,
    MIN_AGE,
    parseApiDate,
} from "../utils/date.utils";

const nameSchema = z
    .string()
    .trim()
    .min(1, "First name is required")
    .max(100, "First name must be at most 100 characters");

const lastNameSchema = z
    .string()
    .trim()
    .min(1, "Last name is required")
    .max(100, "Last name must be at most 100 characters");

const dateOfBirthSchema = z
    .string()
    .min(1, "Date of birth is required")
    .refine((value) => parseApiDate(value) !== undefined, {
        message: "Enter a valid date of birth",
    })
    .refine((value) => {
        const date = parseApiDate(value);
        return date !== undefined && isDateInPast(date);
    }, "Date of birth must be in the past")
    .refine((value) => {
        const date = parseApiDate(value);
        return date !== undefined && calculateAge(date) >= MIN_AGE;
    }, "You must be at least 5 years old")
    .refine((value) => {
        const date = parseApiDate(value);
        return date !== undefined && calculateAge(date) <= MAX_AGE;
    }, "Age cannot be greater than 120 years");

export const personalInfoSchema = z.object({
    firstName: nameSchema,
    lastName: lastNameSchema,
    dateOfBirth: dateOfBirthSchema,
});

export const registerSchema = personalInfoSchema
    .extend({
        email: z
            .string()
            .trim()
            .min(1, "Email is required")
            .email("Enter a valid email address"),
        password: z
            .string()
            .min(8, "Password must be at least 8 characters")
            .max(72, "Password must be at most 72 characters")
            .regex(
                /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/,
                "Password must include uppercase, lowercase, and a number",
            ),
        confirmPassword: z
            .string()
            .min(1, "Please confirm your password"),
    })
    .refine((data) => data.password === data.confirmPassword, {
        message: "Passwords do not match",
        path: ["confirmPassword"],
    }) satisfies z.ZodType<RegisterFormValues>;

export type PersonalInfoFormValues = z.infer<typeof personalInfoSchema>;
