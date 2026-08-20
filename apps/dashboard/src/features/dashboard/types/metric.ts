import { MetricDTO } from "@/features/dashboard/types/dtos/metrics/MetricDto";

export type MetricType = string;

export type Metric = {
    resource_id: string;
    metricType: MetricType;
    timestamp: string;
    value: number;
};

// Maps timestamp: record
export type MetricSeries = Record<string, Metric>;

export type MetricStore = {
    seriesByKey: Record<string, MetricSeries>;

    addMetric: (metric: Metric) => void;
    addMetricFromDto: (metricDto: MetricDTO) => void;
    clearStore: () => void;
    getResourceList: () => string[];
    // Maps resource id to its available metrics
    getMetricList: () => Record<string, MetricType[]>;
};

export function metricSeriesToArray(series?: MetricSeries): Metric[] {
    if (!series) {
        return [];
    }

    return Object.values(series).sort(
        (a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime()
    );
}
