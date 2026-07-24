import { ChartType } from "@/features/dashboard/types/widgets";
import apiClient from "@/lib/fetch/api-client";

export interface BaseWidgetDTO {
    id: string;
    widgetType: "KPI" | "CHART";
    displayName: string | null;
    startX: number;
    startY: number;
    width: number;
    height: number;
}

export interface ChartWidgetDTO extends BaseWidgetDTO {
    widgetType: "CHART";
    chartType: ChartType;
    resourceId: string | null;
    metricType: string | null;
}

export interface KpiWidgetDto extends BaseWidgetDTO {
    widgetType: "KPI";
    chargeIds: string[];
    aggregationWindowDays: number;
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

export interface ChartWidgetConfigUpdateDTO {
    id: string;
    widgetType: "CHART";
    chartType: ChartType;
    displayName: string | null;
    resourceId: string | null;
    metricType: string | null;
}

export interface KpiWidgetConfigUpdateDTO {
    widgetType: "KPI";
    aggregationWindowDays: number;
    chargeIds: string[];
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
        method: "DELETE",
    });
}

export async function updateDashboardLayout(
    dashboardId: string,
    layouts: WidgetLayoutUpdateDTO[]
): Promise<void> {
    await apiClient<void>(`/dashboards/${dashboardId}/layout`, {
        method: "PUT",
        body: JSON.stringify(layouts),
    });
}

export async function createWidget(dashboardId: string, payload: WidgetDto): Promise<WidgetDto> {
    return await apiClient<WidgetDto>(`/dashboards/${dashboardId}/widgets`, {
        method: "POST",
        body: JSON.stringify(payload),
    });
}

export async function updateChartWidgetConfig(
    widgetId: string,
    payload: ChartWidgetConfigUpdateDTO
): Promise<ChartWidgetDTO> {
    return await apiClient<ChartWidgetDTO>(`/dashboards/widgets/${widgetId}/config`, {
        method: "PATCH",
        body: JSON.stringify(payload),
    });
}

export async function deleteWidget(widgetId: string): Promise<void> {
    await apiClient<void>(`/dashboards/widgets/${widgetId}`, {
        method: "DELETE",
    });
}
