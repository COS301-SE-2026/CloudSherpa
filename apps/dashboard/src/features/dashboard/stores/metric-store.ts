"use client"
import { create } from 'zustand'
import { MetricStore, MetricType, Metric } from '@/features/dashboard/types/metric'
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

const AWS_METRIC_TYPE_BY_NAME: Record<AwsMetricName, MetricType> = {
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

function toMetricType(metricName: string): MetricType {
    return AWS_METRIC_TYPE_BY_NAME[metricName as AwsMetricName] ?? "anon";
}

function metricSeriesKey(metric: Metric): string {
    return `${metric.resource_id}:${metric.metricType}`;
}

function upsertMetric(
    seriesByKey: MetricStore["seriesByKey"],
    metric: Metric
): MetricStore["seriesByKey"] {
    const key = metricSeriesKey(metric);

    return {
        ...seriesByKey,
        [key]: {
            ...(seriesByKey[key] ?? {}),
            [metric.timestamp]: metric,
        },
    };
}

export const useMetricStore = create<MetricStore>(
    (set, get) => ({
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
                metricType,
                timestamp: metricDto.periodStart,
                value: metricDto.metricValue
            }

            set((state) => ({
                seriesByKey: upsertMetric(state.seriesByKey, metric),
            }));
        },

        clearStore: () => {
            set(() => ({
                seriesByKey: {}
            }));
        },

        getResourceList: () => {
            const { seriesByKey } = get();

            return Array.from(
                // set removes duplicates
                new Set(
                    Object.keys(seriesByKey).map((key) => key.split(":")[0])
                )
            );
        },

        getMetricList: () => {
            const { seriesByKey } = get();

            const mapMetricTypes: Record<string, Set<MetricType>> = {};

            Object.keys(seriesByKey).forEach((key) => {
                const resourceId = key.split(":")[0];

                if (!mapMetricTypes[resourceId]) {
                    mapMetricTypes[resourceId] = new Set<MetricType>();
                }

                mapMetricTypes[resourceId].add(key.split(":")[1] as MetricType);
            });

            const finalArray: Record<string, MetricType[]> = {};

            for (const [key, value] of Object.entries(mapMetricTypes)) {
                finalArray[key] = Array.from(value);
            }

            return finalArray;
        }
    })
)
