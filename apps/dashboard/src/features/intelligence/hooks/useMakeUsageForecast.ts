"use client";

import { useCallback, useState } from "react";

import { UsageForecastData } from "@/features/intelligence/types/dtos";
import apiClient from "@/lib/fetch/api-client";

export function useMakeUsageForecast() {
    const [isUsageForecastResponseLoading, setIsUsageForecastResponseLoading] = useState(false);
    const [usageForecastRequestError, setUsageForecastRequestError] = useState<string | null>(null);

    const requestUsageForecast = useCallback(
        async (resourceId: string, metricName: string): Promise<UsageForecastData | null> => {
            setIsUsageForecastResponseLoading(true);
            setUsageForecastRequestError(null);

            try {
                const response: UsageForecastData = await apiClient<UsageForecastData>(
                    "/intelligence/forecasting/resource",
                    {
                        method: "POST",
                        body: JSON.stringify({
                            resourceId: resourceId,
                            metricType: metricName,
                            forecastHorizon: "2026-08-11T06:33:44.992Z",
                        }),
                    }
                );

                return response;
            } catch (e) {
                if (e instanceof Error) {
                    setUsageForecastRequestError(e.message);
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
