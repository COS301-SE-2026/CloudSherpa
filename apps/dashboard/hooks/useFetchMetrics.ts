import apiClient from "@/lib/fetch/api-client";
import { useMetricStore } from "@/stores/metric-store";
import { useWindowStore } from "@/stores/window-store";
import { MetricDTO } from "@/types/dtos/metrics/MetricDto";
import { useEffect, useState } from "react";

export function useFetchMetrics() {

    const addMetricFromDto = useMetricStore((state) => state.addMetricFromDto);
    const clearMetricStore = useMetricStore((state) => state.clearStore);
    const fromMs = useWindowStore((state) => state.fromMs);
    const toMs = useWindowStore((state) => state.toMs);

    useEffect(() => {
        let ignore = false;

        async function fetchMetrics() {
            clearMetricStore();

            const from = new Date(fromMs);
            const to = new Date(toMs);

            try {
                const metrics: MetricDTO[] = await apiClient(`/analytics/historical?from=${from.toISOString()}&to=${to.toISOString()}`);
                if (ignore) {
                    return;
                }

                // Need to insert metrics in order of period start
                for (const metric of metrics) {
                    addMetricFromDto(metric);
                }
            } catch (error) {
                console.warn(`Failed to fetch metrics: ${error}`);
            }
        }

        fetchMetrics();

        return () => {
            ignore = true;
        };
    }, [addMetricFromDto, clearMetricStore, fromMs, toMs]);
}
