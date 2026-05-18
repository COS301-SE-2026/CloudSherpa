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
                const metricDto = JSON.parse(event.data) as MetricDTO;

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
