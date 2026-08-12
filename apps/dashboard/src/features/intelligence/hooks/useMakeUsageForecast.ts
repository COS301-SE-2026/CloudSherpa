"use client";

import { useCallback, useState } from "react";

import { UsageForecastData } from "@/features/intelligence/types/dtos";
import apiClient from "@/lib/fetch/api-client";
import { AWS_METRIC_TYPE_BY_NAME_INVERSE } from "@/features/dashboard/stores/metric-store";
import { MetricType } from "@/features/dashboard/types/metric";

export function useMakeUsageForecast() {
    const [isUsageForecastResponseLoading, setIsUsageForecastResponseLoading] = useState(false);
    const [usageForecastRequestError, setUsageForecastRequestError] = useState<string | null>(null);

    const requestUsageForecast = useCallback(
        async (resourceId: string, metricType: MetricType): Promise<UsageForecastData | null> => {
            setIsUsageForecastResponseLoading(true);
            setUsageForecastRequestError(null);

            try {
                const response: UsageForecastData = await apiClient<UsageForecastData>(
                    "/intelligence/forecasting/resource",
                    {
                        method: "POST",
                        body: JSON.stringify({
                            resourceId: resourceId,
                            metricType: AWS_METRIC_TYPE_BY_NAME_INVERSE[metricType],
                            forecastHorizon: "2026-08-11T06:33:44.992Z",
                        }),
                    }
                );

                return response;
            } catch (e) {
                if (e instanceof Error) {
                    setUsageForecastRequestError(
                        "Some HTTP error occured, Gerard will change apiClient to throw custom errors with HTTP status code soon"
                    );
                } else {
                    setUsageForecastRequestError("Unknown error occured");
                }
            } finally {
                setIsUsageForecastResponseLoading(false);
            }

            return null;
        },
        []
    );

    return { requestUsageForecast, isUsageForecastResponseLoading, usageForecastRequestError };
}
