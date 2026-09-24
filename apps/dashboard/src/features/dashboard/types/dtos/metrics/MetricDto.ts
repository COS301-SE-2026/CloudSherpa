export type MetricDTO = {
    resourceId: string;
    metricName: string;
    metricType: string;
    metricValue: number | null;
    unit: string | null;
    periodStart: string;
    periodEnd: string;
};
