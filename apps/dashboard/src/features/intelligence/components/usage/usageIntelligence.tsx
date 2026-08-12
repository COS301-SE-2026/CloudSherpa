"use client";
import SummaryCard, { getMetricUnit } from "@/features/intelligence/components/usage/summaryCard";
import UsageToolbar from "@/features/intelligence/components/usage/usageToolbar";
import UsagePredictionChart from "@/features/intelligence/components/usage/usagePredictionChart";
import { useUsageIntelligenceConfigStore } from "@/features/intelligence/stores/useUsageIntelligenceConfigStore";
import { useUsageIntelligenceStore } from "@/features/intelligence/stores/useUsageIntelligenceStore";
import { useEffect, useMemo } from "react";
import { UsageForecastData } from "@/features/intelligence/types/dtos";
import { TrendingUp, TrendingDown, Minus, AlertCircleIcon } from "lucide-react";
import { useFetchMetrics } from "@/features/dashboard/hooks/useFetchMetrics";
import { Card, CardContent } from "@/components/atoms/card";
import { getArraySummary } from "@/features/intelligence/utils/getUsageSummaries";
import { useMakeUsageForecast } from "../../hooks/useMakeUsageForecast";
import { Alert, AlertDescription, AlertTitle } from "@/components/atoms/alert";
import { timeMs } from "@/lib/timeUtils";
import { useUsageHistoricalData } from "../../hooks/useUsageHistoricalData";

function generateMockForecast(days: number): UsageForecastData {
    const hours = days * 24;
    const now = Date.now();

    const horizonTimestamps: string[] = [];
    const predictedValues: number[] = [];
    const q1Values: number[] = [];
    const q3Values: number[] = [];

    for (let i = 1; i <= hours; i++) {
        horizonTimestamps.push(new Date(now + i * timeMs.hourMs).toISOString());

        const cycle = Math.sin((i / 24) * Math.PI * 2) * 15;

        const noise = (Math.random() - 0.5) * 5; // NOSONAR

        const predicted = Math.max(0, Math.min(100, 50 + cycle + noise));
        predictedValues.push(predicted);

        const uncertainty = 2 + i * 0.3;

        q1Values.push(Math.max(0, predicted - uncertainty));
        q3Values.push(Math.min(100, predicted + uncertainty));
    }

    return { horizonTimestamps, predictedValues, q1Values, q3Values };
}

export default function UsageIntelligence() {
    useFetchMetrics();
    const resourceId = useUsageIntelligenceConfigStore((state) => state.resourceId);
    const metricType = useUsageIntelligenceConfigStore((state) => state.metricType);

    const currentUnit = getMetricUnit(metricType);

    const setUsageForecast = useUsageIntelligenceStore((state) => state.setUsageForecast);

    const {
        requestUsageForecast,
        isUsageForecastResponseLoading, // NOSONAR: wip
        usageForecastRequestError,
    } = useMakeUsageForecast();

    //data
    const usageForecast = useUsageIntelligenceStore((state) => {
        if (!resourceId || !metricType) return null;
        return state.forecasts[resourceId]?.[metricType] ?? null;
    });

    const { historicalUsageSeries } = useUsageHistoricalData();

    const pastSummary = useMemo(() => {
        if (!historicalUsageSeries?.values?.length) {
            return { min: 0, max: 0, avg: 0 };
        }
        const values = historicalUsageSeries.values;
        return getArraySummary(values);
    }, [historicalUsageSeries]);

    const forecastSummary = useMemo(() => {
        if (!usageForecast?.predictedValues) {
            return { min: 0, max: 0, avg: 0 };
        }
        return getArraySummary(usageForecast.predictedValues);
    }, [usageForecast]);

    useEffect(() => {
        if (!resourceId || !metricType) return;

        // Within this scope, typescript knows these are non null
        const selectedResourceId = resourceId;
        const selectedMetricType = metricType;

        const currentForecasts = useUsageIntelligenceStore.getState().forecasts;
        // const isCached = !!currentForecasts[selectedResourceId]?.[selectedMetricType];

        // if (isCached) return;

        async function loadForecast() {
            const forecastData = await requestUsageForecast(selectedResourceId, selectedMetricType);
            // const mockData = generateMockForecast(3);

            if (forecastData) {
                setUsageForecast(selectedResourceId, selectedMetricType, forecastData);
            }
        }

        void loadForecast();
    }, [resourceId, metricType, requestUsageForecast, setUsageForecast]);

    return (
        <div className="flex flex-col h-full w-full p-6 gap-4">
            <UsageToolbar />
            <div className="flex flex-col gap-4 h-full">
                {usageForecastRequestError && (
                    <section>
                        <Alert variant={"destructive"}>
                            <AlertCircleIcon />
                            <AlertTitle>Failed to fetch forecast</AlertTitle>
                            <AlertDescription>{usageForecastRequestError}</AlertDescription>
                        </Alert>
                    </section>
                )}
                <section className="grid grid-cols-1 lg:grid-cols-3 gap-4">
                    <SummaryCard
                        title="Max Usage"
                        unit={currentUnit}
                        Icon={TrendingUp}
                        pastUsage={pastSummary.max}
                        predictedUsage={forecastSummary.max}
                        description="maximum recorded usage"
                        tooltip="This represents the maximum recorded usage for both the past and forecasted usage"
                    />
                    <SummaryCard
                        title="Min Usage"
                        unit={currentUnit}
                        Icon={TrendingDown}
                        pastUsage={pastSummary.min}
                        predictedUsage={forecastSummary.min}
                        description="minimum recorded usage"
                        tooltip="This represents the minimum recorded usage for both the past and forecasted usage"
                    />
                    <SummaryCard
                        title="Average Usage"
                        unit={currentUnit}
                        Icon={Minus}
                        pastUsage={pastSummary.avg}
                        predictedUsage={forecastSummary.avg}
                        description="average recorded usage"
                        tooltip="This represents the average recorded usage for both the past and forecasted usage"
                    />
                </section>
                <section className="w-full flex-1 min-h-0 flex flex-col">
                    {resourceId && metricType ? (
                        <UsagePredictionChart historicalUsageSeries={historicalUsageSeries} />
                    ) : (
                        <Card className="h-full w-full flex flex-col items-center justify-center border-2 border-dashed">
                            <CardContent className="flex items-center justify-center p-0">
                                <p className="text-muted-foreground">
                                    Select a resource and metric to view forecast.
                                </p>
                            </CardContent>
                        </Card>
                    )}
                </section>
            </div>
        </div>
    );
}
