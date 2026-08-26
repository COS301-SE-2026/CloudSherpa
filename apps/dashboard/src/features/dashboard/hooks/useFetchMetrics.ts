import apiClient from "@/lib/fetch/api-client";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { MetricDTO } from "@/features/dashboard/types/dtos/metrics/MetricDto";
import { TimeWindowPreset } from "@/features/dashboard/types/timewindow";
import { useCallback, useEffect, useState } from "react";
import { useAuthContext } from "@/features/authentication/providers/AuthContext";
import { useDashboardStore } from "../stores/dashboard-store";

type MetricStoreState = ReturnType<typeof useMetricStore.getState>;
type DashboardStoreState = ReturnType<typeof useDashboardStore.getState>;

function toAggregationInterval(preset: TimeWindowPreset): "daily" | "weekly" | "monthly" {
    switch (preset) {
        case "T_7_DAYS":
            return "weekly";
        case "T_30_DAYS":
        case "custom":
            return "monthly";
        case "T_5_MIN":
        case "T_15_MIN":
        case "T_30_MIN":
        case "T_1_HOUR":
        case "T_6_HOUR":
        case "T_12_HOUR":
        case "T_24_HOUR":
        default:
            return "daily";
    }
}

export function useFetchMetrics() {
    const { isAuthReady, isAuthenticated } = useAuthContext();
    const [metricFetchError, setMetricFetchError] = useState<Error | null>(null);
    const [metricFetchLoad, setMetricFetchLoad] = useState(false);

    const addMetricFromDto = useMetricStore((state: MetricStoreState) => state.addMetricFromDto);
    const clearMetricStore = useMetricStore((state: MetricStoreState) => state.clearStore);

    const fromMs = useDashboardStore((state: DashboardStoreState) => state.fromMs);
    const toMs = useDashboardStore((state: DashboardStoreState) => state.toMs);
    const selectedPreset = useDashboardStore((state: DashboardStoreState) => state.selectedPreset);

    const fetchMetrics = useCallback(async () => {
        console.log("started fetch");
        setMetricFetchLoad(true);
        setMetricFetchError(null);

        clearMetricStore();

        const from = new Date(fromMs);
        const to = new Date(toMs);
        const interval = toAggregationInterval(selectedPreset);

        const url = `/analytics/historical?from=${from.toISOString()}&to=${to.toISOString()}&interval=${interval}`;

        try {
            const metrics: MetricDTO[] = await apiClient(url);
            for (const metric of metrics) {
                addMetricFromDto(metric);
            }
        } catch (error) {
            console.warn(`Failed to fetch metrics: ${error}`);
            setMetricFetchError(error instanceof Error ? error : new Error(String(error)));
        } finally {
            setMetricFetchLoad(false);
        }
    }, [addMetricFromDto, clearMetricStore, selectedPreset]);

    useEffect(() => {
        if (!isAuthReady || !isAuthenticated) {
            return;
        }

        queueMicrotask(() => {
            void fetchMetrics();
        });
    }, [fetchMetrics, isAuthReady, isAuthenticated]);

    return { fetchMetrics, metricFetchError, metricFetchLoad };
}
