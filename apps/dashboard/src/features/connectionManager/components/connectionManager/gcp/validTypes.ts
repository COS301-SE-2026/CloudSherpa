import * as z from "zod";

export const GcpBillingConfig = z.object({
    billingId: z
        .string()
        .trim()
        .regex(/^[0-9A-Z]{6}-[0-9A-Z]{6}-[0-9A-Z]{6}$/),
    dataset: z.string().trim().min(1),
});

export type GcpBillingConfigType = z.infer<typeof GcpBillingConfig>;

export type GcpBillingConfigSafeParseType = z.ZodSafeParseResult<GcpBillingConfigType>;
