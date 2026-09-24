import { useEffect, useRef, useState } from "react";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { MetricDTO } from "@/features/dashboard/types/dtos/metrics/MetricDto";
import { Metric, MetricType } from "@/features/dashboard/types/metric";

const MOCK_RESOURCE_ID = "mock-ec2-1";
const MOCK_RESOURCES: { id: string; metricType: MetricType; metricName: string }[] = [
    { id: MOCK_RESOURCE_ID, metricType: "cpu", metricName: "CPUUTILIZATION" },
    { id: MOCK_RESOURCE_ID, metricType: "memory", metricName: "MEMORYUSAGE" },
    { id: MOCK_RESOURCE_ID, metricType: "disk", metricName: "DISKREADBYTES" },
    { id: MOCK_RESOURCE_ID, metricType: "anon", metricName: "AnonymousMetric" },
];

const MOCK_VALUES: Partial<Record<MetricType, number[]>> = {
    cpu: [33, 21, 33, 50, 24, 31, 28, 43, 39, 52, 47, 61, 58, 66],
    memory: [45, 48, 42, 55, 51, 49, 53, 58, 52, 56, 54, 60, 57, 62],
    disk: [22, 25, 23, 28, 26, 24, 27, 30, 29, 31, 28, 32, 30, 33],
    anon: [33, 21, 33, 50, 24, 31, 28, 43, 39, 52, 47, 61, 58, 66],
};

const MOCK_INTERVAL_MS = 5_000;

let hasSeededMockMetrics = false;

const API_BASE = process.env["NEXT_PUBLIC_API_URL"];

const sseUrl = `${API_BASE}/stream`;

type MetricStreamEvent = {
    metric_id: string;
    account_id: string;
    currency: string | null;
    resource_id: string;
    metric_type: string;
    metric_name: string;
    metric_value: number;
    period_start: string;
    period_end: string;
    recorded_at: string;
    unit: string | null;
};

function toMetricDto(event: MetricStreamEvent): MetricDTO {
    return {
        resourceId: event.resource_id,
        metricName: event.metric_name,
        metricType: event.metric_type,
        metricValue: event.metric_value,
        unit: event.unit,
        periodStart: event.period_start,
        periodEnd: event.period_end,
    };
}

function createMockMetrics(): Metric[] {
    const now = Date.now();
    const metrics: Metric[] = [];

    MOCK_RESOURCES.forEach((resource) => {
        const metricValues = MOCK_VALUES[resource.metricType] ?? [];

        metricValues.forEach((value, index) => {
            metrics.push({
                resource_id: resource.id,
                metricName: resource.metricName,
                metricType: resource.metricType,
                timestamp: new Date(
                    now - (metricValues.length - 1 - index) * MOCK_INTERVAL_MS
                ).toISOString(),
                value: Math.min(100, value),
            });
        });
    });

    return metrics;
}

async function refreshAuthSession(): Promise<boolean> {
    if (!API_BASE) return false;

    try {
        const response = await fetch(`${API_BASE}/auth/refresh`, {
            method: "POST",
            credentials: "include",
        });
        return response.ok;
    } catch {
        return false;
    }
}

export function useMetricStream() {
    const addMetric = useMetricStore((state) => state.addMetric);
    const addMetricFromDto = useMetricStore((state) => state.addMetricFromDto);
    // String or bool?
    const [error, setError] = useState<Error | null>(null);
    const hasRetriedRef = useRef(false);

    useEffect(() => {
        if (!hasSeededMockMetrics) {
            createMockMetrics().forEach(addMetric);
            hasSeededMockMetrics = true;
        }

        let isCleaningUp = false;
        let eventSource: EventSource;

        const connect = () => {
            eventSource = new EventSource(sseUrl, { withCredentials: true });

            eventSource.onopen = () => {
                hasRetriedRef.current = false;
                setError(null);
            };

            eventSource.addEventListener("metric", handleMetric);

            eventSource.onerror = () => {
                if (isCleaningUp) return;
                eventSource.close();
                eventSource.removeEventListener("metric", handleMetric);

                // the connection may have failed because the auth cookie expired
                // while the user was on another page; try to refresh once and reconnect
                if (!hasRetriedRef.current) {
                    hasRetriedRef.current = true;

                    refreshAuthSession().then((refreshed) => {
                        if (isCleaningUp) return;
                        if (refreshed) {
                            connect();
                        } else {
                            setError(new Error(`Failed to open metric stream connection`));
                        }
                    });
                    return;
                }

                setError(new Error(`Failed to open metric stream connection`));
            };
        };

        const handleMetric = (event: MessageEvent<string>) => {
            const metricDto = toMetricDto(JSON.parse(event.data) as MetricStreamEvent);
            addMetricFromDto(metricDto);
        };

        connect();

        return () => {
            isCleaningUp = true;
            eventSource.removeEventListener("metric", handleMetric);
            eventSource.close();
        };
    }, [addMetric, addMetricFromDto]);

    return { error };
}
