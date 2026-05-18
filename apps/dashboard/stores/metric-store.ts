"use client"
import { create } from 'zustand'
import { MetricStore, MetricType, Metric } from '@/types/metric'
/*
    ====EXAMPLE USAGE====
    const cpuMetrics = useMetricStore(
        (state) => {
            state.seriesByKey[`${resourceId}:cpu`] ?? []
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
    ConsumedWriteCapacityUnits: "capacity",
    ConsumedReadCapacityUnits: "capacity",
    FirstByteLatency: "latency",
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

 export const useMetricStore = create<MetricStore>(
    (set) => ({
        seriesByKey: {},

        addMetric: (metric) => {
            const key = `${metric.resource_id}:${metric.metricType}`;

            set((state) => ({
                seriesByKey: {
                    ...state.seriesByKey,

                    [key]: [
                        ...(state.seriesByKey[key] ?? []),
                        metric,
                    ]
                },
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
            
            const key = `${metric.resource_id}:${metric.metricType}`;

            set((state) => ({
                seriesByKey: {
                    ...state.seriesByKey,

                    [key]: [
                        ...(state.seriesByKey[key] ?? []),
                        metric,
                    ]
                },
            }));
        },

        clearStore: () => {
            set(() => ({
                seriesByKey: {}
            }));
        }
    })
 )
