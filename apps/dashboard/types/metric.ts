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

export type MetricStore = {
    seriesByKey: Record<string, Metric[]>;

    addMetric: (metric: Metric) => void;
    addMetricFromDto: (metricDto : MetricDTO) => void;
    clearStore: () => void;
    getResourceList: () => string[];
    // Maps resource id to its available metrics
    getMetricList: () => Record<string, MetricType[]>;
}
