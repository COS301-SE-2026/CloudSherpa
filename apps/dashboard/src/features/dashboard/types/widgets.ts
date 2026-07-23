import { MetricType } from "@/features/dashboard/types/metric";

export type LayoutItem = {
    id: string;
    x: number;
    y: number;
    w: number;
    h: number;
    autoPosition?: boolean;
};

export type WidgetConfig = {
    id: string;
    type: ChartType;
    displayName: string | null;
    resourceId: string | null;
    metricType: MetricType | null;
};

export type DashboardConfig = {
    id: string;
    displayName: string;
    timeFrom: string | null;
    timeTo: string | null;
    predefinedTime: string;
    current: boolean;
    layoutItemIds: string[];
};

export type DashboardStub = {
    id: string;
    displayName: string;
};

// update every time a new chart is added
export type ChartType = "line_chart" | "gauge_chart";

export type ChartThemeTokens = {
    textColor: string;
    mutedText: string;
    gridLine: string;
    chartColors: string[];
    gridOpacity: number;
};
