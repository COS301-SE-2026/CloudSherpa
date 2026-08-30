import apiClient from "@/lib/fetch/api-client";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { useCallback, useEffect, useState } from "react";
import { useAuthContext } from "@/features/authentication/providers/AuthContext";

/*type MetricStoreState = ReturnType<typeof useMetricStore.getState>;*/
export interface AvailableMetricDto {
    resourceId: string;

    metrics: {
        metricName: string;
        metricType: string;
    }[];
}

export function useFetchMetrics() {
    const { isAuthReady, isAuthenticated } = useAuthContext();
    const [metricFetchError, setMetricFetchError] = useState<Error | null>(null);
    const [metricFetchLoad, setMetricFetchLoad] = useState(false);

    const initializeMetricSeries = useMetricStore((state) => state.initializeMetricSeries);

    //const clearMetricStore = useMetricStore((state: MetricStoreState) => state.clearStore);

    const fetchMetrics = useCallback(async () => {
        setMetricFetchLoad(true);
        setMetricFetchError(null);

        //clearMetricStore();

        try {
            const availableMetrics = await apiClient<AvailableMetricDto[]>(
                "/analytics/resource-metrics"
            );

            initializeMetricSeries(availableMetrics);
        } catch (error) {
            console.warn(`Failed to fetch metrics: ${error}`);

            setMetricFetchError(error instanceof Error ? error : new Error(String(error)));
        } finally {
            setMetricFetchLoad(false);
        }
    }, [initializeMetricSeries]);

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
