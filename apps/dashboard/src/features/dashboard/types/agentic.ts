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

    //chart
    chartType?: ChartType;
    chartColour?: ChartColour;
    provider?: string;
    accountId?: string;
    resourceId?: string;
    metricType?: string;
    metricName?: string;

    // kpi
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
