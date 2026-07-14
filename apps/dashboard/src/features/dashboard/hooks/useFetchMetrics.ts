import apiClient from "@/lib/fetch/api-client";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { useWindowStore } from "@/features/dashboard/stores/window-store";
import { MetricDTO } from "@/features/dashboard/types/dtos/metrics/MetricDto";
import { useCallback, useEffect, useState } from "react";

export function useFetchMetrics() {
    const [metricFetchError, setMetricFetchError] = useState<Error | null>(null);
    const [metricFetchLoad, setMetricFetchLoad] = useState(true);

    const addMetricFromDto = useMetricStore((state) => state.addMetricFromDto);
    const clearMetricStore = useMetricStore((state) => state.clearStore);

    const fromMs = useWindowStore((state) => state.fromMs);
    const toMs = useWindowStore((state) => state.toMs);

    const fetchMetrics = useCallback(async () => {
        setMetricFetchLoad(true);
        setMetricFetchError(null);

        clearMetricStore();

        const from = new Date(fromMs);
        const to = new Date(toMs);

        try {
            console.log("Attempting fetch");
            const metrics: MetricDTO[] = await apiClient(
                `/analytics/historical?from=${from.toISOString()}&to=${to.toISOString()}`
            );

            // Need to insert metrics in order of period start
            for (const metric of metrics) {
                addMetricFromDto(metric);
            }
        } catch (error) {
            console.warn(`Failed to fetch metrics: ${error}`);

            setMetricFetchError(error instanceof Error ? error : new Error(String(error)));
        } finally {
            setMetricFetchLoad(false);
        }
    }, [addMetricFromDto, clearMetricStore, fromMs, toMs]);

    useEffect(() => {
        queueMicrotask(() => {
            void fetchMetrics();
        });
    }, [fetchMetrics]);

    return { fetchMetrics, metricFetchError, metricFetchLoad };
}
