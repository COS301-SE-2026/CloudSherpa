import { TimeWindowPreset } from "@/features/dashboard/types/timewindow";
import { ChartType, ChartColour } from "@/features/dashboard/types/widgets";

export type AiSessionResponse = {
    sessionId: string;
    createdAt: string;
    lastActivity: string;
    currentVersionId: string | null;
};

export type AiDashboardPlanRequest = {
    sessionId: string;
    startingDashboardId: string;
    message: string;
};

export type DashboardPlanWidget = {
    widgetId: string;
    widgetType: "CHART" | "KPI";
    displayName: string | null;
    startX: number;
    startY: number;
    width: number;
    height: number;

    chartType?: ChartType;
    chartColour?: ChartColour;
    provider?: string;
    accountId?: string;
    resourceId?: string;
    metricType?: string;
    metricName?: string;

    chargeIds?: string[];
    aggregationWindowDays?: number;
};

export type DashboardPlan = {
    title: string;
    description: string | null;
    timeFrom: string | null;
    timeTo: string | null;
    predefinedTime: TimeWindowPreset | null;
    widgets: DashboardPlanWidget[];
};

export type AiDashboardPlanResponse = {
    sessionId: string;
    versionId: string;
    version: number;
    stageAttempted: boolean;
    stageSucceeded: boolean;
    assistantMessage: string;
    dashboard: DashboardPlan;
};

export type AiVersionSummary = {
    versionId: string;
    version: number;
    parentVersionId: string | null;
    title: string;
    createdAt: string;
    current: boolean;
};

export type AiVersionResponse = {
    versionId: string;
    sessionId: string;
    version: number;
    parentVersionId: string | null;
    current: boolean;
    createdAt: string;
    dashboard: DashboardPlan;
};

export type AiDashboardApplyMode = "REPLACE_STARTED_DASHBOARD" | "CREATE_NEW_DASHBOARD";

export type AiDashboardApplyRequest = {
    mode: AiDashboardApplyMode;
    startedDashboardId?: string;
};

export const THEMES = [
    {
        id: "compute",
        label: "Compute",
        metricNames: [
            "CPU Utilization",
            "CPU Reservation",
            "Container CPU Utilizations",
            "CPU Percent",
            "Reserved CPU cores",
            "CPU usage time",
        ],
    },
    {
        id: "memory",
        label: "Memory",
        metricNames: [
            "Memory Utilization",
            "Memory Reservation",
            "JVM Memory Pressure",
            "Memory used",
            "Memory usage",
            "Freeable Memory",
        ],
    },
    {
        id: "storage",
        label: "Storage & Disk",
        metricNames: [
            "Percentage Disk Space Used",
            "Disk Read Bytes",
            "Disk Write Bytes",
            "Free Storage Space",
            "Stored bytes",
            "Allocated Storage",
            "OS Disk Write Bytes",
            "OS Disk Read Bytes",
            "Read IOPS",
            "Write IOPS",
            "Blob Count",
            "Bytes uploaded",
            "Bytes downloaded",
        ],
    },
    {
        id: "network",
        label: "Network",
        metricNames: [
            "Network In",
            "Network Out",
            "Network Bytes In",
            "Network Bytes Out",
            "Ingress",
            "Egress",
            "Received Bytes Count",
            "Send Bytes Count",
        ],
    },
    {
        id: "latency",
        label: "Latency & Performance",
        metricNames: [
            "Duration",
            "Read Latency",
            "Write Latency",
            "Search Latency",
            "Function execution time",
            "Request latency",
        ],
    },
    {
        id: "database",
        label: "Database",
        metricNames: [
            "DTU Consumption",
            "Database Connections",
            "Current Connections",
            "Deadlocks",
            "Cluster Index Writes Blocked",
        ],
    },
    {
        id: "traffic_errors",
        label: "Requests, Traffic & Errors",
        metricNames: [
            "Cluster Failed Request Count",
            "Cluster Request Total",
            "HTTP requests",
            "API requests",
            "HTTP Success 2xx",
            "HTTP Error 5xx",
            "Throttles",
            "Errors",
            "Health Check Failed",
            "Evictions",
        ],
    },
    {
        id: "orchestration",
        label: "Nodes, Instances & Functions",
        metricNames: [
            "Cluster Node Count",
            "Active instances",
            "Running instances",
            "Concurrent Executions",
            "Function executions",
            "Invocations",
            "Pod restart count",
            "Health Status",
        ],
    },
];
