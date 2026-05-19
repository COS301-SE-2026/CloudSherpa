import apiClient from "@/lib/fetch/api-client";
import { useMetricStore } from "@/stores/metric-store";
import { useWindowStore } from "@/stores/window-store";
import { MetricDTO } from "@/types/dtos/metrics/MetricDto";
import { useEffect, useRef } from "react";

export function useFetchMetrics() {

    const addMetricFromDto = useMetricStore((state) => state.addMetricFromDto);
    const clearMetricStore = useMetricStore((state) => state.clearStore);
    const fromMs = useWindowStore((state) => state.fromMs);
    const toMs = useWindowStore((state) => state.toMs);

    const ignore = useRef(false);
    const busy = useRef(false);

    const sleep = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

    async function fetchMetrics() {
            // Abort if unmounted
            if (ignore.current) {
                return;
            }

            while(busy.current) {
                await sleep(500);
            }

            clearMetricStore();

            const from = new Date(fromMs);
            const to = new Date(toMs);

            try {
                busy.current = true;
                const metrics: MetricDTO[] = await apiClient(`/analytics/historical?from=${from.toISOString()}&to=${to.toISOString()}`);
                
                // Need to insert metrics in order of period start
                for (const metric of metrics) {
                    addMetricFromDto(metric);
                }
                busy.current = false;
                
            } catch (error) {
                console.warn(`Failed to fetch metrics: ${error}`);
            }
        }

    useEffect(() => {
        fetchMetrics();

        return () => {
            ignore.current = true;
        };
    }, [fromMs, toMs]);

    return { fetchMetrics };
}
