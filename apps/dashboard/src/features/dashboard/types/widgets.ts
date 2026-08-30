import { MetricType } from "@/features/dashboard/types/metric";

export type LayoutItem = {
    id: string;
    x: number;
    y: number;
    w: number;
    h: number;
    autoPosition?: boolean;
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

export type BaseWidgetConfig = {
    id: string;
    displayName: string | null;
};

export type ChartWidgetConfig = BaseWidgetConfig & {
    widgetType: "CHART";
    chartType: ChartType;
    provider: string | null;
    accountId: string | null;
    resourceId: string | null;
    metricName: string | null;
    metricType: MetricType | null;
};

export type KpiWidgetConfig = BaseWidgetConfig & {
    widgetType: "KPI";
    chargeIds: string[];
    aggregationWindowDays: number;
};

export type WidgetConfig = ChartWidgetConfig | KpiWidgetConfig;
