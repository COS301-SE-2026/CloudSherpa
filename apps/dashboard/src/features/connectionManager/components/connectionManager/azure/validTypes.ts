import * as z from "zod";

export const AzureBillingConfig = z.object({
    storageAccountName: z.string().trim().min(3).max(24),
    blobContainerName: z.string().trim().min(3).max(63),
    exportDirectory: z.string().trim().min(1).max(255),
    exportName: z.string().trim().min(1),
});

export type AzureBillingConfigType = z.infer<typeof AzureBillingConfig>;

export type AzureBillingConfigSafeParseType = z.ZodSafeParseResult<AzureBillingConfigType>;
