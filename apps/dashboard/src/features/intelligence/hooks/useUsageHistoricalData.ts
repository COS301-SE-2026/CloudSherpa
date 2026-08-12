"use client";

import { useEffect, useState } from "react";
import { useUsageIntelligenceConfigStore } from "../stores/useUsageIntelligenceConfigStore";
import { HistoricalUsageSeriesDto } from "../types/dtos";
import apiClient from "@/lib/fetch/api-client";
import { AWS_METRIC_TYPE_BY_NAME_INVERSE } from "@/features/dashboard/stores/metric-store";
import { timeMs } from "@/lib/timeUtils";

export function useUsageHistoricalData() {
    const resourceId = useUsageIntelligenceConfigStore((state) => state.resourceId);
    const metricType = useUsageIntelligenceConfigStore((state) => state.metricType);

    const [historicalUsageSeries, setHistoricalUsageSeries] =
        useState<HistoricalUsageSeriesDto | null>(null);
    const [historicalUsageError, setHistoricalUsageError] = useState<string | null>(null);
    const [isHistoricalUsageLoading, setIsHistoricalUsageLoading] = useState<boolean>(false);

    useEffect(() => {
        async function fetchHistoricalUsageData() {
            if (!resourceId || !metricType) {
                return;
            }

            setIsHistoricalUsageLoading(true);
            setHistoricalUsageError(null);

            try {
                const awsMetricName = AWS_METRIC_TYPE_BY_NAME_INVERSE[metricType];
                const historicalWindowStartMs = Date.now() - timeMs.dayMs * 30;

                const response: HistoricalUsageSeriesDto = await apiClient(
                    "/analytics/historical-resource-metric",
                    {
                        method: "POST",
                        body: JSON.stringify({
                            resourceId: resourceId,
                            metricType: awsMetricName,
                            fromDate: new Date(historicalWindowStartMs),
                        }),
                    }
                );

                setHistoricalUsageSeries(response);
            } catch (e) {
                // NOSONAR TODO: Better error handling
                if (e instanceof Error) {
                    setHistoricalUsageError(e.message);
                } else {
                    setHistoricalUsageError("Unknown error occured during historical fetch");
                }
            } finally {
                setIsHistoricalUsageLoading(false);
            }
        }

        void fetchHistoricalUsageData();
    }, [resourceId, metricType]);

    return { historicalUsageSeries, historicalUsageError, isHistoricalUsageLoading };
}
