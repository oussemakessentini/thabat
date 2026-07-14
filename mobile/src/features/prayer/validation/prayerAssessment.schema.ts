import { z } from "zod";

export const prayerAssessmentSchema = z
    .object({
        missedYears: z
            .number()
            .int("Enter a whole number")
            .min(0, "Cannot be negative")
            .max(120, "Cannot exceed 120 years"),
        missedMonths: z
            .number()
            .int("Enter a whole number")
            .min(0, "Cannot be negative"),
        missedDays: z
            .number()
            .int("Enter a whole number")
            .min(0, "Cannot be negative"),
        dailyRecoveryTarget: z
            .number()
            .int("Enter a whole number")
            .min(1, "Must be at least 1")
            .max(100, "Cannot exceed 100"),
    })
    .refine(
        (values) =>
            values.missedYears > 0
            || values.missedMonths > 0
            || values.missedDays > 0,
        {
            message: "Enter at least one missed year, month, or day",
            path: ["missedDays"],
        },
    );

export type PrayerAssessmentFormValues = z.infer<typeof prayerAssessmentSchema>;
