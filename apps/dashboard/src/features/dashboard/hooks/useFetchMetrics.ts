import apiClient from "@/lib/fetch/api-client";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { useCallback, useEffect, useState } from "react";

export interface AvailableMetricDto {
    resourceId: string;

    metrics: {
        metricName: string;
        metricType: string;
    }[];
}

export function useFetchMetrics() {
    const [metricFetchError, setMetricFetchError] = useState<Error | null>(null);
    const [metricFetchLoad, setMetricFetchLoad] = useState(false);

    const initializeMetricSeries = useMetricStore((state) => state.initializeMetricSeries);


    const fetchMetrics = useCallback(async () => {
        setMetricFetchLoad(true);
        setMetricFetchError(null);

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
        queueMicrotask(() => {
            void fetchMetrics();
        });
    }, [fetchMetrics]);

    return { fetchMetrics, metricFetchError, metricFetchLoad };
}
