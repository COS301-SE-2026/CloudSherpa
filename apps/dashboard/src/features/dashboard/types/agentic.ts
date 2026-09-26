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
