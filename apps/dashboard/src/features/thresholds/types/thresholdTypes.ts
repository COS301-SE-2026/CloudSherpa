export type OperatorsForThreshold = "GT" | "GTE" | "LT" | "LTE" | "EQ";

export type SeverityForThreshold = "INFO" | "WARNING" | "CRITICAL";

export interface Threshold{
    thresholdId : string;
    resourceId : string;
    userId : string;
    metricName : string;
    operator : OperatorsForThreshold;
    value : number;
    severity : SeverityForThreshold;
    enabled : boolean;
    createdAt : string | null;
    updatedAt : string | null;
}

export interface CreateThresholdRequest{
    resourceId : string;
    userId : string;
    metric_name : string;
    operator : OperatorsForThreshold;
    value : number;
    severity : SeverityForThreshold;
    enabled : boolean;
}

export interface UpdateThresholdRequest{
    metric_name?: string;
    operator?: OperatorsForThreshold;
    value?: number;
    severity?: SeverityForThreshold;
    enabled?: boolean;
}

export const OPERATOR_LABEL : Record<OperatorsForThreshold, string> = {
    GT : ">", GTE : "> =", LT : "<", LTE : "< =", EQ : "=",
};

export const OPERATORS : OperatorsForThreshold[] = ["GT", "GTE", "LT", "LTE", "EQ"];

export const SEVERITY : SeverityForThreshold[] = ["INFO", "WARNING", "CRITICAL"];

export const SEVERITY_COLOURS : Record<SeverityForThreshold, string> = {
    INFO : "text-primary",
    WARNING : "text-warning",
    CRITICAL : "text-destructive",
};