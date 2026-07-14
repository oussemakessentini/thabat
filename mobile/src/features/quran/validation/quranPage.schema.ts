import { z } from "zod";

const statuses = [
    "NOT_STARTED",
    "LEARNING",
    "MEMORIZED",
    "NEEDS_REVISION",
    "STRONG",
] as const;

const confidenceField = z
    .string()
    .optional()
    .refine(
        (value) =>
            value === undefined
            || value === ""
            || (Number(value) >= 1 && Number(value) <= 5),
        "Confidence must be 1–5",
    );

export const updateQuranPageSchema = z.object({
    status: z.enum(statuses),
    memorizedAt: z
        .string()
        .optional()
        .refine(
            (value) =>
                value === undefined
                || value === ""
                || /^\d{4}-\d{2}-\d{2}$/.test(value),
            "Use YYYY-MM-DD",
        ),
    confidenceLevel: confidenceField,
    notes: z.string().max(1000, "Notes must be at most 1000 characters").optional(),
});

export type UpdateQuranPageFormValues = z.infer<typeof updateQuranPageSchema>;

export const recordQuranReviewSchema = z.object({
    reviewedAt: z
        .string()
        .regex(/^\d{4}-\d{2}-\d{2}$/, "Use YYYY-MM-DD"),
    successful: z.boolean(),
    confidenceLevel: confidenceField,
    newStatus: z.enum([...statuses, ""] as const).optional(),
});

export type RecordQuranReviewFormValues = z.infer<typeof recordQuranReviewSchema>;
