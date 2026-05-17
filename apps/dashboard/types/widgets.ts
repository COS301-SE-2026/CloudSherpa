import { MetricType } from "@/types/metric";

export type WidgetConfig = {
    id: string;
    title: string;
    metricType: MetricType;
    resourceId: string;
    chartType: 'gauge' | 'line' | 'bar';
}

export type LayoutItem = {
    id: string; // Layout ID
    widgetId: string; // Foreign key to WidgetConfig/depends on how we want to use it
    x: number;
    y: number;
    w: number;
    h: number;
}

//idea when user moves widget without changing config, only perisist layout & vice verssa
// dt, POST /api/widgets to create the configuration and 
// get the new widgetId from the server thenPOST /api/dashboards/{id}/layouts or something
//  with the generated widgetId and the default grid coordinates.