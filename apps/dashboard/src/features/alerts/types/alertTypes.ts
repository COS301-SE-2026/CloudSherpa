export type TypeForAlerts = "THRESHOLD" | "ANOMALY" | "BUDGET";

export type SeverityForAlerts = "WARNING" | "CRITICAL";

export type StatusForAlerts = "ACTIVE" | "DISABLED";

export interface Alert{
    alertId : string;
    userId : string;
    alertType : TypeForAlerts;
    severity : SeverityForAlerts;
    title : string;
    message : string | null;
    payload : Record<string, unknown>;
    status : StatusForAlerts;
    canonicalKey : string | null;
    createdAt : string | null;
    lastSeen : string | null;
    resolvedAt : string | null;
}

export const SEVERITY_COLOURS : Record<SeverityForAlerts, string> = {
    WARNING : "text-warning",
    CRITICAL : "text-destructive"
};

export const TYPE : Record<TypeForAlerts, string> = {
    THRESHOLD : "Threshold",
    ANOMALY : "Anomaly",
    BUDGET : "Budget",
};

export const STATUS_LABELS : Record<StatusForAlerts, string> = {
    ACTIVE : "Active",
    DISABLED : "Disabled",
};