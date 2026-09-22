import apiClient from "@/lib/fetch/api-client";
import { TimeWindowPreset } from "@/features/dashboard/types/timewindow";
import { ChartType, ChartColour } from "@/features/dashboard/types/widgets";

// ==========================================
// DTO Types
// ==========================================

export interface AiSessionResponseDto {
    sessionId: string;
    createdAt: string;
    lastActivity: string;
    currentVersionId: string | null;
}

export interface AiDashboardPlanRequestDto {
    sessionId: string;
    message: string;
}

export interface DashboardPlanWidgetDto {
    widgetId: string;
    widgetType: "CHART" | "KPI";
    displayName: string | null;
    startX: number;
    startY: number;
    width: number;
    height: number;

    // Chart specific
    chartType?: ChartType;
    chartColour?: ChartColour;
    provider?: string;
    accountId?: string;
    resourceId?: string;
    metricType?: string;
    metricName?: string;

    // KPI specific
    chargeIds?: string[];
    aggregationWindowDays?: number;
}

export interface DashboardPlanDto {
    title: string;
    description: string | null;
    timeFrom: string | null;
    timeTo: string | null;
    predefinedTime: TimeWindowPreset | null;
    widgets: DashboardPlanWidgetDto[];
}

export interface AiDashboardPlanResponseDto {
    sessionId: string;
    versionId: string;
    version: number;
    assistantMessage: string;
    dashboard: DashboardPlanDto;
}

export interface AiVersionSummaryDto {
    versionId: string;
    version: number;
    parentVersionId: string | null;
    title: string;
    createdAt: string;
    current: boolean;
}

export interface AiVersionResponseDto {
    versionId: string;
    sessionId: string;
    version: number;
    parentVersionId: string | null;
    current: boolean;
    createdAt: string;
    dashboard: DashboardPlanDto;
}

//create new session.
export async function createAiSession(): Promise<AiSessionResponseDto> {
    return await apiClient<AiSessionResponseDto>("/ai/sessions", {
        method: "POST",
    });
}

//delete all session related data.
export async function deleteAiSession(sessionId: string): Promise<void> {
    await apiClient<void>(`/ai/sessions/${sessionId}`, {
        method: "DELETE",
    });
}

//send prompt to create 'staged' dashboard
export async function generateDashboardPlan(
    payload: AiDashboardPlanRequestDto
): Promise<AiDashboardPlanResponseDto> {
    return await apiClient<AiDashboardPlanResponseDto>("/ai/dashboard/plan", {
        method: "POST",
        body: JSON.stringify(payload),
    });
}

//return all dashboards versions
export async function getAiDashboardVersions(sessionId: string): Promise<AiVersionSummaryDto[]> {
    return await apiClient<AiVersionSummaryDto[]>(`/ai/sessions/${sessionId}/versions`, {
        method: "GET",
    });
}

//return specific dashboard version.
export async function getAiDashboardVersion(
    sessionId: string,
    versionId: string
): Promise<AiVersionResponseDto> {
    return await apiClient<AiVersionResponseDto>(
        `/ai/sessions/${sessionId}/versions/${versionId}`,
        {
            method: "GET",
        }
    );
}

//add ai dash to actual list of dashboards.
export async function applyAiDashboardVersion(sessionId: string, versionId: string): Promise<void> {
    await apiClient<void>(`/ai/sessions/${sessionId}/versions/${versionId}/apply`, {
        method: "POST",
    });
}
