"use client";

import apiClient from "@/lib/fetch/api-client";
import { useState } from "react";
import { BillingForecastDto } from "../types/dtos";

export function useMakeBillingForecast() {
    const [billingForecastLoading, setBillingForecastLoading] = useState(false);
    const [billingForecastError, setBillingForecastError] = useState<string | null>(null);

    async function makeBillingForecast(forecastSteps: number): Promise<BillingForecastDto | null> {
        setBillingForecastLoading(true);
        setBillingForecastError(null);

        if (forecastSteps < 1) {
            setBillingForecastError(
                "Invalid number of forecast steps. Forecast steps have to be >= 1"
            );
            return null;
        }

        try {
            const result: BillingForecastDto = await apiClient(
                "/intelligence/forecasting/billing",
                {
                    method: "POST",
                    body: JSON.stringify({
                        forecastSteps: forecastSteps,
                    }),
                }
            );

            return result;
        } catch (e) {
            if (e instanceof Error) {
                setBillingForecastError("Failed to make the billing forecast request");
            } else {
                setBillingForecastError("An unknown error has occured");
            }

            return null;
        } finally {
            setBillingForecastLoading(false);
        }
    }

    return { makeBillingForecast, billingForecastLoading, billingForecastError };
}
