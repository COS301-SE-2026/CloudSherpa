"use client";

import { useEffect, useRef, useState } from "react";
import { Metric, MetricType } from "@/features/dashboard/types/metric";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import apiClient from "@/lib/fetch/api-client";

interface DownsampledHistoricalMetricResponseDto {
    metricId: string;
    resourceId: string;
    recordedAt: string;
    metricType: string;
    metricName: string;
    metricValue: number;
    unit: string;
    currency: string;
    periodStart: string;
    periodEnd: string;
}

type HistoricalMetricResponseDto = DownsampledHistoricalMetricResponseDto[];

interface PropsForUseFetchHistoricalDataProps {
    resourceId: string;
    fromMs: number;
    toMs?: number;
    metricName?: string;
}

export function useFetchMetricHistoricalData({
    resourceId,
    fromMs,
    toMs,
    metricName = "",
}: PropsForUseFetchHistoricalDataProps) {
    const setMetricSeries = useMetricStore((state) => state.setMetricSeries);

    const [isLoading, setIsLoading] = useState(false);

    const [forErrors, setForErrors] = useState<Error | null>(null);

    const request = useRef<string | null>(null);

    useEffect(() => {
        if (!resourceId || !metricName || !Number.isFinite(fromMs)) {
            return;
        }

        const finalMetricNames = metricName || "default";

        const keyForRequest = `${resourceId}:${finalMetricNames}:${fromMs}:${toMs}`;

        if (request.current === keyForRequest) {
            return;
        }

        request.current = keyForRequest;

        let cancelled = false;

        async function fetchHistoricalData() {
            setIsLoading(true);

            setForErrors(null);

            try {
                const response = await apiClient<HistoricalMetricResponseDto>(
                    "/analytics/downsampled-historical-series",

                    {
                        method: "POST",
                        body: JSON.stringify({
                            resourceId: resourceId,
                            metricName: finalMetricNames,
                            from: new Date(fromMs).toISOString(),
                            to: toMs ? new Date(toMs).toISOString() : new Date().toISOString(),
                        }),
                    }
                );

                console.log("Downsampled historical metric response:", response);

                if (cancelled) {
                    return;
                }

                if (!response || !Array.isArray(response) || response.length === 0) {
                    setMetricSeries(resourceId, metricName, []);
                    return;
                }

                const metrics: Metric[] = response.map((item) => ({
                    resource_id: item.resourceId,
                    metricType: item.metricType as MetricType,
                    timestamp: item.recordedAt || item.periodStart,
                    value: item.metricValue || 0,
                    metricName: item.metricName,
                    unit: item.unit,
                    currency: item.currency,
                    periodStart: item.periodStart,
                    periodEnd: item.periodEnd,
                }));

                setMetricSeries(resourceId, metricName, metrics);
            } catch (error) {
                if (cancelled) {
                    return;
                }

                const fetchError = error instanceof Error ? error : new Error(String(error));

                setForErrors(fetchError);

                setMetricSeries(resourceId, metricName, []);
            } finally {
                if (!cancelled) {
                    setIsLoading(false);
                }
            }
        }

        void fetchHistoricalData();

        return () => {
            cancelled = true;
        };
    }, [resourceId, fromMs, toMs, metricName, setMetricSeries]);

    return { isLoading, forErrors };
}
