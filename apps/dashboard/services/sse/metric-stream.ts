import { useEffect, useState } from "react";
import { useMetricStore } from "@/stores/metric-store";
import { MetricDTO } from "@/types/metric-dto";
import { Metric, MetricType } from "@/types/metric";

export function useMetricStream() {

    const addMetric = useMetricStore((state) => state.addMetric);
    // String or bool?
    const [error, setError] = useState<Error | null>(null);

    useEffect(() => {
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