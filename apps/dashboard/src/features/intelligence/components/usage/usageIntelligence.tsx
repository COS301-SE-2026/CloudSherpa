"use client";
import SummaryCard, { getMetricUnit } from "@/features/intelligence/components/usage/summaryCard";
import UsageToolbar from "@/features/intelligence/components/usage/usageToolbar";
import UsagePredictionChart from "@/features/intelligence/components/usage/usagePredictionChart";
import { useUsageIntelligenceConfigStore } from "@/features/intelligence/stores/useUsageIntelligenceConfigStore";
import { useUsageIntelligenceStore } from "@/features/intelligence/stores/useUsageIntelligenceStore";
import { useEffect, useMemo } from "react";
import { usageForecastData } from "@/features/intelligence/types/metrics";
import { TrendingUp, TrendingDown, Minus } from "lucide-react";
import { useFetchMetrics } from "@/features/dashboard/hooks/useFetchMetrics";
import { Card, CardContent } from "@/components/atoms/card";
import { useChartData } from "@/features/dashboard/hooks/useChartData";
import { getArraySummary } from "@/features/intelligence/utils/getUsageSummaries";

function generateMockForecast(days: number): usageForecastData {
    const hours = days * 24;
    const now = Date.now();
    const oneHour = 60 * 60 * 1000;

    const horizonTimestamps: string[] = [];
    const predictedValues: number[] = [];
    const q1Values: number[] = [];
    const q3Values: number[] = [];

    for (let i = 1; i <= hours; i++) {
        horizonTimestamps.push(new Date(now + i * oneHour).toISOString());

        const cycle = Math.sin((i / 24) * Math.PI * 2) * 15;

        const noise = (Math.random() - 0.5) * 5;

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

    //data
    const forecastedMetrics = useUsageIntelligenceStore((state) => {
        if (!resourceId || !metricType) return null;
        return state.forecasts[resourceId]?.[metricType] ?? null;
    });
    const { timeSeriesData } = useChartData(resourceId || "", metricType || "anon");

    const pastSummary = useMemo(() => {
        if (!timeSeriesData || timeSeriesData.length === 0) {
            return { min: 0, max: 0, avg: 0 };
        }
        const values = timeSeriesData.map((d) => d.value);
        return getArraySummary(values);
    }, [timeSeriesData]);

    const forecastSummary = useMemo(() => {
        if (!forecastedMetrics || !forecastedMetrics.predictedValues) {
            return { min: 0, max: 0, avg: 0 };
        }
        return getArraySummary(forecastedMetrics.predictedValues);
    }, [forecastedMetrics]);

    useEffect(() => {
        if (resourceId && metricType) {
            const currentForecasts = useUsageIntelligenceStore.getState().forecasts;
            const isCached = !!currentForecasts[resourceId]?.[metricType];

            if (!isCached) {
                const mockData = generateMockForecast(3);

                setUsageForecast(resourceId, metricType, mockData);
            }
        }
    }, [resourceId, metricType, setUsageForecast]);

    return (
        <div className="flex flex-col h-full w-full p-6 gap-4">
            <UsageToolbar />
            <div className="flex flex-col gap-4 h-full">
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
                        <UsagePredictionChart />
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
