import { useEffect, useState } from "react";
import { useMetricStore } from "@/stores/metric-store";
import { MetricDTO } from "@/types/metric-dto";
import { Metric, MetricType } from "@/types/metric";

const MOCK_RESOURCE_ID = "mock-ec2-1";
const MOCK_METRIC_TYPE: MetricType = "anon";
const MOCK_VALUES = [33, 21, 33, 50, 24, 31, 28, 43, 39, 52, 47, 61, 58, 66];
const MOCK_INTERVAL_MS = 5_000;

let hasSeededMockMetrics = false;

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

            const eventSource = new EventSource("http://localhost:8083/stream");
    
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
