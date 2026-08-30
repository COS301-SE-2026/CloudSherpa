"use client";
import { create } from "zustand";
import { MetricStore, MetricType, Metric } from "@/features/dashboard/types/metric";
/*
    ====EXAMPLE USAGE====
    const cpuMetrics = useMetricStore(
        (state) => {
            metricSeriesToArray(state.seriesByKey[`${resourceId}:cpu`])
        }
    );
*/

type AwsMetricName =
    | "BucketSizeBytes"
    | "Duration"
    | "WriteThrottleEvents"
    | "CPUUtilization"
    | "DiskReadBytes"
    | "NetworkIn"
    | "ConsumedWriteCapacityUnits"
    | "ConsumedReadCapacityUnits"
    | "FirstByteLatency"
    | "ReadLatency"
    | "Errors"
    | "DiskWriteBytes"
    | "WriteLatency"
    | "ReadThrottleEvents"
    | "FreeStorageSpace"
    | "AllRequests"
    | "DatabaseConnections"
    | "Invocations"
    | "Throttles"
    | "NetworkOut"
    | "NumberOfObjects";

export const AWS_METRIC_TYPE_BY_NAME: Record<AwsMetricName, MetricType> = {
    BucketSizeBytes: "storage-used",
    Duration: "duration",
    WriteThrottleEvents: "throttles",
    CPUUtilization: "cpu",
    DiskReadBytes: "disk",
    NetworkIn: "network",
    ConsumedWriteCapacityUnits: "read-capacity",
    ConsumedReadCapacityUnits: "write-capacity",
    FirstByteLatency: "first-byte-latency",
    ReadLatency: "latency",
    Errors: "errors",
    DiskWriteBytes: "disk",
    WriteLatency: "latency",
    ReadThrottleEvents: "throttles",
    FreeStorageSpace: "storage-available",
    AllRequests: "requests",
    DatabaseConnections: "connections",
    Invocations: "invocations",
    Throttles: "throttles",
    NetworkOut: "network",
    NumberOfObjects: "object-count",
};

// Inverse helper for reverse metric lookup
type Invert<T extends Record<PropertyKey, PropertyKey>> = {
    [K in keyof T as T[K]]: K;
};

const invertRecord = <T extends Record<PropertyKey, PropertyKey>>(obj: T): Invert<T> => {
    return Object.fromEntries(Object.entries(obj).map(([key, value]) => [value, key])) as Invert<T>;
};

export const AWS_METRIC_TYPE_BY_NAME_INVERSE = invertRecord(AWS_METRIC_TYPE_BY_NAME);

function toMetricType(metricName: string): MetricType {
    return AWS_METRIC_TYPE_BY_NAME[metricName as AwsMetricName] ?? "anon";
}

function metricSeriesKey(metric: Metric): string {
    return `${metric.resource_id}:${metric.metricName}`;
}

function upsertMetric(
    seriesByKey: MetricStore["seriesByKey"],
    metric: Metric
): MetricStore["seriesByKey"] {
    const key = metricSeriesKey(metric);

    return {
        ...seriesByKey,
        [key]: {
            ...seriesByKey[key],
            [metric.timestamp]: metric,
        },
    };
}

export const useMetricStore = create<MetricStore>((set, get) => ({
    seriesByKey: {},

    addMetric: (metric) => {
        set((state) => ({
            seriesByKey: upsertMetric(state.seriesByKey, metric),
        }));
    },

    addMetricFromDto: (metricDto) => {
        const metricType = toMetricType(metricDto.metricName);

        const metric: Metric = {
            resource_id: metricDto.resourceId,
            metricName: metricDto.metricName,
            metricType,
            timestamp: metricDto.periodStart,
            value: metricDto.metricValue,
        };

        set((state) => ({
            seriesByKey: upsertMetric(state.seriesByKey, metric),
        }));
    },

    initializeMetricSeries: (availableMetrics) => {
        set((state) => {
            const seriesByKey = {
                ...state.seriesByKey,
            };

            for (const resource of availableMetrics) {
                if (!resource.resourceId) {
                    continue;
                }

                for (const metric of resource.metrics) {
                    if (!metric.metricType || metric.metricType === "string") {
                        continue;
                    }

                    const key = `${resource.resourceId}:${metric.metricName}`;

                    if (!seriesByKey[key]) {
                        seriesByKey[key] = {};
                    }
                }
            }
            return { seriesByKey };
        });
    },

    setMetricSeries: (resourceId, metricName, metrics) => {
        const key = `${resourceId}:${metricName}`;

        const series = Object.fromEntries(metrics.map((metric) => [metric.timestamp, metric]));

        set((state) => ({
            seriesByKey: {
                ...state.seriesByKey,
                [key]: series,
            },
        }));
    },

    clearStore: () => {
        set(() => ({
            seriesByKey: {},
        }));
    },

    getResourceList: () => {
        const { seriesByKey } = get();

        return Array.from(
            // set removes duplicates
            new Set(Object.keys(seriesByKey).map((key) => key.split(":")[0]))
        );
    },

    getMetricList: () => {
        const { seriesByKey } = get();
        const mapMetricNames: Record<string, Set<string>> = {};

        Object.keys(seriesByKey).forEach((key) => {
            const [resourceId, metricName] = key.split(":");

            if (!mapMetricNames[resourceId]) {
                mapMetricNames[resourceId] = new Set<string>();
            }
            mapMetricNames[resourceId].add(metricName);
        });

        const finalArray: Record<string, string[]> = {};

        for (const [key, metricNames] of Object.entries(mapMetricNames)) {
            finalArray[key] = Array.from(metricNames);
        }

        return finalArray;
    },
}));
