"use client"

import { useState } from "react"

import { UsageForecastData } from "@/features/intelligence/types/metrics"
import apiClient from "@/lib/fetch/api-client";

export function useMakeUsageForecast() {

    const [isUsageForecastResponseLoading, setIsUsageForecastResponseLoading] = useState(false);
    const [usageForecastRequestError, setUsageForecastRequestError] = useState<string | null>(null);

    async function requestUsageForecast(resourceId: string, metricType: string): Promise<UsageForecastData | null> {
        setIsUsageForecastResponseLoading(true);
        setUsageForecastRequestError(null);
        
        try {    
            const response: UsageForecastData = await apiClient<UsageForecastData>("/intelligence/forecasting/resource", {
                method: 'POST',
                body: JSON.stringify({
                    "resourceId": resourceId,
                    "metricType": metricType,
                    "forecastHorizon": "2026-08-11T06:33:44.992Z"
                })
            })

            console.log(response);

            return response;

        } catch (e) {
            if (e instanceof Error) {
                setUsageForecastRequestError("Some HTTP error occured, Gerard will change apiClient to throw custom errors with HTTP status code soon");
            } else {
                setUsageForecastRequestError("Unknown error occured")
            }
        } finally {
            setIsUsageForecastResponseLoading(false);
        }

        return null;
    }

    return {requestUsageForecast, isUsageForecastResponseLoading, usageForecastRequestError};

}