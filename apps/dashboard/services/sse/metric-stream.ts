import { useEffect, useState } from "react";
import { useMetricStore } from "@/stores/metric-store";
import { MetricDTO } from "@/types/metric-dto";
import { Metric, MetricType } from "@/types/metric";

const MOCK_RESOURCES = [
    {id: "mock-ec2-1", metricType: "cpu" as MetricType},
    {id: "mock-ec2-1", metricType: "memory" as MetricType},
    {id: "mock-ec2-1", metricType: "disk" as MetricType},
    {id: "mock-ec2-1", metricType: "cost" as MetricType},
    {id: "mock-ec2-1", metricType: "anon" as MetricType}
];

const MOCK_VALUES = {
    cpu: [33, 21, 33, 50, 24, 31, 28, 43, 39, 52, 47, 61, 58, 66],
    memory: [45, 48, 42, 55, 51, 49, 53, 58, 52, 56, 54, 60, 57, 62],
    disk: [22, 25, 23, 28, 26, 24, 27, 30, 29, 31, 28, 32, 30, 33],
    cost: [0.45, 0.52, 0.48, 0.63, 0.58, 0.61, 0.55, 0.67, 0.62, 0.59, 0.71, 0.68, 0.73, 0.69],
    anon: [33, 21, 33, 50, 24, 31, 28, 43, 39, 52, 47, 61, 58, 66],
};

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
                
                // metric preprocessing, needs to be minimal
                let metricType: MetricType = "anon";

                if (metricDto.service_category == "CPUUtilization") {
                    metricType = "cpu";
                }

                const metric: Metric = {
                    resource_id: metricDto.resource_id,
                    metricType: metricType,
                    timestamp: metricDto.recorded_at,
                    value: metricDto.usage_amount
                }

                addMetric(metric);
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
        }, [addMetric])

    return { error };
}
