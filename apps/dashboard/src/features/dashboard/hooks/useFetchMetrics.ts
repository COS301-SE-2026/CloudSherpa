import apiClient from "@/lib/fetch/api-client";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { useWindowStore } from "@/features/dashboard/stores/window-store";
import { MetricDTO } from "@/features/dashboard/types/dtos/metrics/MetricDto";
import { TimeWindowPreset } from "@/features/dashboard/types/timewindow";
import { useCallback, useEffect, useState } from "react";

function toAggregationInterval(preset: TimeWindowPreset): "daily" | "weekly" | "monthly" {
    switch (preset) {
        case "7d":
            return "weekly";
        case "30d":
        case "custom":
            return "monthly";
        case "1m":
        case "2m":
        case "5m":
        case "1h":
        case "24h":
        default:
            return "daily";
    }
}

export function useFetchMetrics() {

    const [metricFetchError, setMetricFetchError] = useState<Error | null>(null);
    const [metricFetchLoad, setMetricFetchLoad] = useState(true);

    const addMetricFromDto = useMetricStore((state) => state.addMetricFromDto);
    const clearMetricStore = useMetricStore((state) => state.clearStore);

    const fromMs = useWindowStore((state) => state.fromMs);
    const toMs = useWindowStore((state) => state.toMs);
    const selectedPreset = useWindowStore((state) => state.selectedPreset);

    const fetchMetrics = useCallback(async () => {
        setMetricFetchLoad(true);
        setMetricFetchError(null);

        clearMetricStore();

        const from = new Date(fromMs);
        const to = new Date(toMs);
        const interval = toAggregationInterval(selectedPreset);

        try {
            console.log("Attempting fetch");
            const metrics: MetricDTO[] = await apiClient(
                `/analytics/historical?from=${from.toISOString()}&to=${to.toISOString()}&interval=${interval}`
            );

            // Need to insert metrics in order of period start
            for (const metric of metrics) {
                addMetricFromDto(metric);
            }

        } catch (error) {
            console.warn(`Failed to fetch metrics: ${error}`);

            setMetricFetchError(
                error instanceof Error
                    ? error
                    : new Error(String(error))
            );

        } finally {
            setMetricFetchLoad(false);
        }
    }, [addMetricFromDto, clearMetricStore, fromMs, toMs, selectedPreset]);

    useEffect(() => {
        queueMicrotask(() => {
            void fetchMetrics();
        });
    }, [fetchMetrics]);

    return { fetchMetrics, metricFetchError, metricFetchLoad };
}