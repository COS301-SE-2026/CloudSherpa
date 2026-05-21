import { MetricDTO } from "./dtos/metrics/MetricDto";

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
    resource_id: string,
    metricType: MetricType,
    timestamp: string,
    value: number
}

// Maps timestamp: record
export type MetricSeries = Record<string, Metric>;

export type MetricStore = {
    seriesByKey: Record<string, MetricSeries>;

    addMetric: (metric: Metric) => void;
    addMetricFromDto: (metricDto : MetricDTO) => void;
    clearStore: () => void;
    getResourceList: () => string[];
    // Maps resource id to its available metrics
    getMetricList: () => Record<string, MetricType[]>;
}

export function metricSeriesToArray(series?: MetricSeries): Metric[] {
    if (!series) {
        return [];
    }

    return Object.values(series).sort(
        (a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime()
    );
}
