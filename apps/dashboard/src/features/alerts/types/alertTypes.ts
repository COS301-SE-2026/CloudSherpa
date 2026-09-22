export type TypeForAlerts = "THRESHOLD" | "ANOMALY" | "BILLING";

export type SeverityForAlerts = "INFO" | "WARNING" | "CRITICAL";

export type StatusForAlerts = "ACTIVE" | "ACKNOWLEDGED" | "DISMISSED";

export interface ThresholdAlertPayload{
    metric_name : string;
    metric_value : number;
    threshold_value : number;
    threshold_operator : "GT" | "GTE" | "LT" | "LTE" | "EQ";
    resource_id : string;
    period_start : number;
    period_end : number;
}

export interface AnomalyAlertPayload{
    metric_name : string;
    metric_value : number;
    average_value : number;
    standard_deviation : number;
    z_score : number;
    resource_id : string;
    period_start : number;
    period_end : number;
}

export interface Alert{
    alertId : string;
    userId : string;
    alertType : TypeForAlerts;
    severity : SeverityForAlerts;
    title : string;
    message : string | null;
    payload : ThresholdAlertPayload | AnomalyAlertPayload | Record<string, unknown>;
    status : StatusForAlerts;
    canonicalKey : string | null;
    createdAt : string | null;
    lastSeen : string | null;
    resolvedAt : string | null;
}

export const SEVERITY_COLOURS : Record<SeverityForAlerts, string> = {
    INFO : "text-primary",
    WARNING : "text-warning",
    CRITICAL : "text-destructive"
};