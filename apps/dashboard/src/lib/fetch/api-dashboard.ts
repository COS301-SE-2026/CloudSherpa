import { ChartType } from "@/features/dashboard/types/widgets";
import apiClient from "@/lib/fetch/api-client";

// export type WidgetTypeEnum = "kpi" | "chart";

export interface BaseWidgetDTO {
    id: string;
    type: "kpi" | "chart";
    displayName: string | null;
    startX: number;
    startY: number;
    width: number;
    height: number;
}

export interface ChartWidgetDTO extends BaseWidgetDTO {
    type: "chart";
    chartType: ChartType;
    resourceId: string | null;
    metricType: string | null;
}

export interface KpiWidgetDto extends BaseWidgetDTO {
    type: "kpi";
    chargeIds: string[];
    aggregationWindow: number;
}

type WidgetDto = ChartWidgetDTO | KpiWidgetDto;

export interface DashboardDTO {
    id: string;
    displayName: string;
    timeFrom: string | null;
    timeTo: string | null;
    predefinedTime: string;
    current: boolean;
    widgets: WidgetDto[];
}

export interface DashboardCreateDTO {
    id: string;
    displayName: string;
}

export interface WidgetLayoutUpdateDTO {
    id: string;
    x: number;
    y: number;
    w: number;
    h: number;
}

export interface WidgetConfigUpdateDTO {
    type: string;
    displayName: string | null;
    resourceId: string | null;
    metricType: string | null;
}
export async function fetchDashboards(): Promise<DashboardDTO[]> {
    return await apiClient<DashboardDTO[]>("/dashboards", {
        method: "GET",
    });
}
export async function createDashboard(payload: DashboardCreateDTO): Promise<DashboardDTO> {
    return await apiClient<DashboardDTO>("/dashboards", {
        method: "POST",
        body: JSON.stringify(payload),
    });
}
export async function deleteDashboard(dashboardId: string): Promise<void> {
    await apiClient<void>(`/dashboards/${dashboardId}`, {
        method: "POST",
    });
}

export async function updateDashboardLayout(
    dashboardId: string,
    layouts: WidgetLayoutUpdateDTO[]
): Promise<void> {
    await apiClient<void>(`/dashboards/${dashboardId}/layout`, {
        method: "POST",
        body: JSON.stringify(layouts),
    });
}

export async function createChartWidget(
    dashboardId: string,
    payload: ChartWidgetDTO
): Promise<ChartWidgetDTO> {
    return await apiClient<ChartWidgetDTO>(`/dashboards/${dashboardId}/widgets`, {
        method: "POST",
        body: JSON.stringify(payload),
    });
}

export async function updateChartWidgetConfig(
    widgetId: string,
    payload: WidgetConfigUpdateDTO
): Promise<ChartWidgetDTO> {
    return await apiClient<ChartWidgetDTO>(`/dashboards/widgets/${widgetId}/config`, {
        method: "POST",
        body: JSON.stringify(payload),
    });
}

export async function deleteWidget(widgetId: string): Promise<void> {
    await apiClient<void>(`/dashboards/widgets/${widgetId}`, {
        method: "POST",
    });
}
