export type MetricDTO = {
    metric_id: string;
    resourceId: string;
    metricName: string;
    metricType: string;
    metricValue: number;
    unit: string | null;
    periodStart: string;
    periodEnd: string;
};
