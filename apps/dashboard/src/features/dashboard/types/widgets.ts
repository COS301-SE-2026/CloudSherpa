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
export type ChartType = "line" | "gauge";

export type ChartThemeTokens = {
    textColor: string;
    mutedText: string;
    gridLine: string;
    chartColors: string[];
    gridOpacity: number;
};

export type BaseWidgetConfig = {
    id: string;
    title: string;
};

export type ChartWidgetConfig = BaseWidgetConfig & {
    widgetType: "chart";
    chartType: ChartType;
    resourceId: string;
    metricType: MetricType;
};

export type KpiWidgetConfig = BaseWidgetConfig & {
    widgetType: "kpi";
    chargeIds: string[];
    aggregationWindowDays: number;
};

export type WidgetConfig = ChartWidgetConfig | KpiWidgetConfig;
