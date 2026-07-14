import { z } from "zod";

export const quranDailyGoalSchema = z
    .object({
        memorizationPagesPerDay: z
            .number()
            .int("Enter a whole number")
            .min(0, "Must be between 0 and 20")
            .max(20, "Must be between 0 and 20"),
        revisionPagesPerDay: z
            .number()
            .int("Enter a whole number")
            .min(0, "Must be between 0 and 50")
            .max(50, "Must be between 0 and 50"),
        preferredStartPage: z
            .number()
            .int("Enter a whole number")
            .min(1, "Must be between 1 and 604")
            .max(604, "Must be between 1 and 604")
            .nullable()
            .optional(),
    })
    .refine(
        (values) =>
            values.memorizationPagesPerDay > 0
            || values.revisionPagesPerDay > 0,
        {
            message: "Set at least one daily target greater than zero",
            path: ["memorizationPagesPerDay"],
        },
    );

export type QuranDailyGoalFormValues = z.infer<typeof quranDailyGoalSchema>;
