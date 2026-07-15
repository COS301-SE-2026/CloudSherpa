import { MetricType } from "@/features/dashboard/types/metric";

export type LayoutItem = {
    id: string;
    widgetId: string;
    x: number;
    y: number;
    w: number;
    h: number;
    autoPosition?: boolean;
};

export type WidgetConfig = {
    id: string;
    chartType: ChartType;
    title: string;
    resourceId: string;
    metricType: MetricType;
};

export type DashboardConfig = {
    id: string;
    name: string;
    description?: string;
    layoutItemIds: string[];
};

export type DashboardStub = {
    id: string;
    label: string;
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
