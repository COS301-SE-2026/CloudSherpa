"use client";

import { useEffect, useRef, useState } from "react";
import { Metric, MetricType } from "@/features/dashboard/types/metric";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import apiClient from "@/lib/fetch/api-client";
import { TimeWindowPreset } from "../types/timewindow";

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
    selectedPreset: TimeWindowPreset;
}

export function useFetchMetricHistoricalData({
    resourceId,
    fromMs,
    toMs,
    metricName = "",
    selectedPreset,
}: PropsForUseFetchHistoricalDataProps) {
    const setMetricSeries = useMetricStore((state) => state.setMetricSeries);

    const [isLoading, setIsLoading] = useState(false);

    const [forErrors, setForErrors] = useState<Error | null>(null);

    const request = useRef<string | null>(null);
    const currentWindow = useRef({ fromMs, toMs });

    const customFromMs = selectedPreset === "custom" ? fromMs : undefined;
    const customToMs = selectedPreset === "custom" ? toMs : undefined;

    useEffect(() => {
        currentWindow.current = { fromMs, toMs };
    }, [fromMs, toMs]);

    useEffect(() => {
        const { fromMs: requestFromMs, toMs: requestToMs } = currentWindow.current;

        if (!resourceId || !metricName || !Number.isFinite(requestFromMs)) {
            return;
        }

        const finalMetricNames = metricName || "default";

        const keyForRequest = `${resourceId}:${finalMetricNames}:${requestFromMs}:${requestToMs}`;

        if (request.current === keyForRequest) {
            return;
        }

        request.current = keyForRequest;

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
                            from: new Date(requestFromMs).toISOString(),
                            to: requestToMs
                                ? new Date(requestToMs).toISOString()
                                : new Date().toISOString(),
                        }),
                    }
                );

                if (request.current !== keyForRequest) {
                    return;
                }

                if (!response || !Array.isArray(response) || response.length === 0) {
                    setMetricSeries(resourceId, metricName, []);
                    return;
                }

                const metrics: Metric[] = response.map((item) => ({
                    resource_id: item.resourceId,
                    metricType: item.metricType as MetricType,
                    timestamp: item.periodStart,
                    value: item.metricValue || null,
                    metricName: item.metricName,
                    unit: item.unit,
                    currency: item.currency,
                    periodStart: item.periodStart,
                    periodEnd: item.periodEnd,
                }));

                setMetricSeries(resourceId, metricName, metrics);
            } catch (error) {
                if (request.current !== keyForRequest) {
                    return;
                }

                const fetchError = error instanceof Error ? error : new Error(String(error));

                setForErrors(fetchError);

                setMetricSeries(resourceId, metricName, []);
            } finally {
                if (request.current === keyForRequest) {
                    setIsLoading(false);
                }
            }
        }

        void fetchHistoricalData();
    }, [resourceId, metricName, selectedPreset, customFromMs, customToMs, setMetricSeries]);

    return { isLoading, forErrors };
}
