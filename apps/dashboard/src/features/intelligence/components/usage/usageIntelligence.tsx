"use client";
import SummaryCard from "@/features/intelligence/components/usage/summaryCard";
import UsageToolbar from "@/features/intelligence/components/usage/usageToolbar";
import UsagePredictionChart from "@/features/intelligence/components/usage/usagePredictionChart";
import { useUsageIntelligenceStore } from "@/features/intelligence/stores/useUsageIntelligenceStore";

//mock
import { ResourceUsageForecastResponseDto } from "@/features/intelligence/types/metrics";
const now = Date.now();
const oneHour = 60 * 60 * 1000;

const mockForecastDto: ResourceUsageForecastResponseDto = {
    horizonTimestamps: [
        new Date(now + 1 * oneHour).toISOString(),
        new Date(now + 2 * oneHour).toISOString(),
        new Date(now + 3 * oneHour).toISOString(),
        new Date(now + 4 * oneHour).toISOString(),
    ],
    predictedValues: [52, 55, 59, 63],
    q1Values: [48, 49, 50, 48],

    q3Values: [56, 61, 68, 78],
};

export default function UsageIntelligence() {
    const resourceId = useUsageIntelligenceStore((state) => state.resourceId);
    const metricType = useUsageIntelligenceStore((state) => state.metricType);

    return (
        <div className="h-full w-full p-6">
            <UsageToolbar />
            <div className="flex flex-col gap-4">
                <section className="w-full">
                    {resourceId && metricType ? (
                        <UsagePredictionChart
                            resourceId={resourceId}
                            metricType={metricType}
                            forecastDto={mockForecastDto}
                            metricTypeLabel={metricType.toUpperCase()}
                        />
                    ) : (
                        <div className="flex h-[400px] w-full items-center justify-center rounded-lg border-2 border-dashed border-border bg-card">
                            <p className="text-muted-foreground">
                                Select a resource and metric to view forecast.
                            </p>
                        </div>
                    )}
                </section>
                <section className="grid grid-cols-1 lg:grid-cols-3 gap-4">
                    <SummaryCard
                        title="Max Usage"
                        unit="%"
                        pastUsage={60}
                        predictedUsage={90}
                        description="max usage over time period days"
                    />
                    <SummaryCard
                        title="Min Usage"
                        unit="%"
                        pastUsage={20}
                        predictedUsage={45}
                        description="max usage over time period days"
                    />
                    <SummaryCard
                        title="Average Usage"
                        unit="%"
                        pastUsage={50}
                        predictedUsage={60}
                        description="max usage over time period days"
                    />
                </section>
            </div>
        </div>
    );
}
