"use client";
import SummaryCard, { getMetricUnit } from "@/features/intelligence/components/usage/summaryCard";
import UsageToolbar from "@/features/intelligence/components/usage/usageToolbar";
import UsagePredictionChart from "@/features/intelligence/components/usage/usagePredictionChart";
import { useUsageIntelligenceConfigStore } from "@/features/intelligence/stores/useUsageIntelligenceConfigStore";
import { useUsageIntelligenceStore } from "@/features/intelligence/stores/useUsageIntelligenceStore";
import { useEffect } from "react";
import { usageForecastData } from "@/features/intelligence/types/metrics";
import { TrendingUp, TrendingDown, Minus } from "lucide-react";
import { useFetchMetrics } from "@/features/dashboard/hooks/useFetchMetrics";

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

    //summarycard values
    const pastMaxUsage = 90;
    const forecastedMaxUsage = 190;
    const pastMinUsage = 50;
    const forecastedMinUsage = 60;
    const pastAverageUsage = 70;
    const forecastedAverageUsage = 85;

    const setUsageForecast = useUsageIntelligenceStore((state) => state.setUsageForecast);

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
        <div className="h-full w-full p-6">
            <UsageToolbar />
            <div className="flex flex-col gap-4">
                <section className="grid grid-cols-1 lg:grid-cols-3 gap-4">
                    <SummaryCard
                        title="Max Usage"
                        unit={currentUnit}
                        Icon={TrendingUp}
                        pastUsage={pastMaxUsage}
                        predictedUsage={forecastedMaxUsage}
                        description="maximum recorded usage"
                        tooltip="This represents the maximum recorded usage for both the past and forecasted usage"
                    />
                    <SummaryCard
                        title="Min Usage"
                        unit={currentUnit}
                        Icon={TrendingDown}
                        pastUsage={pastMinUsage}
                        predictedUsage={forecastedMinUsage}
                        description="minimum recorded usage"
                        tooltip="This represents the minimum recorded usage for both the past and forecasted usage"
                    />
                    <SummaryCard
                        title="Average Usage"
                        unit={currentUnit}
                        Icon={Minus}
                        pastUsage={pastAverageUsage}
                        predictedUsage={forecastedAverageUsage}
                        description="average recorded usage"
                        tooltip="This represents the average recorded usage for both the past and forecasted usage"
                    />
                </section>
                <section className="w-full h-full">
                    {resourceId && metricType ? (
                        <UsagePredictionChart />
                    ) : (
                        <div className="flex h-[400px] w-full items-center justify-center rounded-lg border-2 border-dashed border-border bg-card">
                            <p className="text-muted-foreground">
                                Select a resource and metric to view forecast.
                            </p>
                        </div>
                    )}
                </section>
            </div>
        </div>
    );
}
