import { MetricDTO } from "@/features/dashboard/types/dtos/metrics/MetricDto";

export type MetricType =
    | "cpu"
    | "memory"
    | "storage-used"
    | "storage-available"
    | "object-count"
    | "duration"
    | "throttles"
    | "disk"
    | "network"
    | "read-capacity"
    | "write-capacity"
    | "first-byte-latency"
    | "latency"
    | "errors"
    | "requests"
    | "connections"
    | "invocations"
    | "anon";

export type Metric = {
    resource_id: string;
    metricName: string;
    metricType: MetricType;
    timestamp: string;
    value: number;

    metricName?: string;
    unit?: string;
    currency?: string;
    periodStart?: string;
    periodEnd?: string;
};

// Maps timestamp: record
export type MetricSeries = Record<string, Metric>;

export interface AvailableMetric {
    resourceId: string;

    metrics: { metricName: string; metricType: string }[];
}

export type MetricStore = {
    seriesByKey: Record<string, MetricSeries>;

    addMetric: (metric: Metric) => void;
    addMetricFromDto: (metricDto: MetricDTO) => void;

    initializeMetricSeries: (availableMetrics: AvailableMetric[]) => void;

    setMetricSeries: (resourceId: string, metricType: MetricType, metrics: Metric[]) => void;

    clearStore: () => void;
    getResourceList: () => string[];
    // Maps resource id to its available metrics
    getMetricList: () => Record<string, string[]>;
};

export function metricSeriesToArray(series?: MetricSeries): Metric[] {
    if (!series) {
        return [];
    }

    return Object.values(series).sort(
        (a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime()
    );
}
