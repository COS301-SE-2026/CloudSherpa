export interface UsageError {
    readonly item: "usage" | "forecast" | "both";
    readonly errorMessage: string;
}
