export type MetricType = "cpu" | "memory" | "disk" | "cost";

export type Metric = {
    resource_id: string,
    metricType: MetricType,
    timestamp: string,
    value: number
}

export type MetricStore = {
    seriesByKey: Record<string, Metric[]>;

    addMetric: (metric: Metric) => void;
}