import { useEffect, useState } from "react";
import { useMetricStore } from "@/stores/metric-store";
import { MetricDTO } from "@/types/dtos/metrics/MetricDto";
import { Metric, MetricType } from "@/types/metric";

const MOCK_RESOURCE_ID = "mock-ec2-1";
const MOCK_METRIC_TYPE: MetricType = "anon";
const MOCK_VALUES = [33, 21, 33, 50, 24, 31, 28, 43, 39, 52, 47, 61, 58, 66];
const MOCK_INTERVAL_MS = 5_000;

let hasSeededMockMetrics = false;

const API_BASE = process.env['NEXT_PUBLIC_API_URL'];

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
    unit: string;
};

function toMetricDto(event: MetricStreamEvent): MetricDTO {
    return {
        metricId: event.metric_id,
        accountId: event.account_id,
        currency: event.currency,
        resourceId: event.resource_id,
        metricType: event.metric_type,
        metricName: event.metric_name,
        metricValue: event.metric_value,
        periodStart: event.period_start,
        periodEnd: event.period_end,
        recordedAt: event.recorded_at,
        unit: event.unit,
    };
}

function createMockMetrics(): Metric[] {
    const now = Date.now();

    return MOCK_VALUES.map((value, index) => ({
        resource_id: MOCK_RESOURCE_ID,
        metricType: MOCK_METRIC_TYPE,
        timestamp: new Date(now - (MOCK_VALUES.length - 1 - index) * MOCK_INTERVAL_MS).toISOString(),
        value,
    }));
}

export function useMetricStream() {

    const addMetric = useMetricStore((state) => state.addMetric);
    const addMetricFromDto = useMetricStore((state) => state.addMetricFromDto);
    // String or bool?
    const [error, setError] = useState<Error | null>(null);

    useEffect(() => {
            if (!hasSeededMockMetrics) {
                createMockMetrics().forEach(addMetric);
                hasSeededMockMetrics = true;
            }

            const eventSource = new EventSource(sseUrl);
    
            eventSource.onopen = () => {
                console.log("SSE connected");
                setError(null);
            };
    
            const handleMetric = (event: MessageEvent<string>) => {
                const metricDto = toMetricDto(JSON.parse(event.data) as MetricStreamEvent);
                addMetricFromDto(metricDto);
            };
    
            eventSource.onerror = () => {
                setError(new Error(`Failed to open metric stream connection`));
                eventSource.close();
            }
    
            eventSource.addEventListener("metric", handleMetric);
    
            return () => {
                eventSource.removeEventListener("metric", handleMetric);
                eventSource.close();
            }
        }, [addMetric, addMetricFromDto])

    return { error };
}
