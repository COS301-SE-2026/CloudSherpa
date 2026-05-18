import { MetricType } from "./metric";

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
    chartType: string;
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