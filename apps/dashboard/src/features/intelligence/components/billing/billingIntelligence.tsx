"use client";

import { useBillingIntelligenceStore } from "@/features/intelligence/stores/billingIntelligenceStore";
import BillingToolbar from "@/features/intelligence/components/billing/billingToolbar";
import CostBreakdownList from "@/features/intelligence/components/billing/costBreakdownList";
import BillingForecastChart from "@/features/intelligence/components/billing/billingForecastChart";
import BillingStatisticsCard from "@/features/intelligence/components/billing/billingStatisticsCard";
import BillingSummaryCard from "@/features/intelligence/components/billing/billingSummaryCard";
import { TrendingUp } from "lucide-react";
import { useEffect } from "react";
import { useMakeBillingForecast } from "../../hooks/useMakeBillingForecast";

export default function BillingIntelligence() {
    const {
        provider,
        accountId,
        resourceId,
        breakdownSearch,
        setBreakdownSearch,
        pastTimeWindowDays,
        forecastTimeWindowDays,
        billingData,
        isLoading,
        disableFilters,
        setBillingData,
    } = useBillingIntelligenceStore();

    const { makeBillingForecast } = useMakeBillingForecast();

    const selected = disableFilters || (provider && accountId && resourceId);

    useEffect(() => {
        async function laodForecast() {
            const result = await makeBillingForecast(30);
            if (result) {
                setBillingData(result);
            }
        }

        if (selected) {
            void laodForecast();
        }
    }, [forecastTimeWindowDays, selected]);

    if (!selected) {
        return (
            <div className="h-full w-full p-6 flex flex-col gap-4">
                <BillingToolbar />

                <div className="flex-1 flex items-center justify-center">
                    <div className="text-center max-w-md">
                        <div className="mx-auto w-16 h-16 bg-muted rounded-full flex items-center justify-center mb-4">
                            {" "}
                            <TrendingUp className="h-8 w-8 text-muted-foreground" />{" "}
                        </div>

                        <h3 className="text-lg font-semibold mb-2"> No selection made </h3>

                        <p className="text-sm text-muted-foreground">
                            {" "}
                            Select a provider, account and resource to view billing data{" "}
                        </p>
                    </div>
                </div>
            </div>
        );
    }

    if (isLoading) {
        return (
            <div className="h-full w-full p-6 flex flex-col gap-4">
                <BillingToolbar />

                <div className="flex-1 flex items-center justify-center">
                    <div className="text-center">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-4" />

                        <p className="text-muted-foreground"> Loading billing data... </p>
                    </div>
                </div>
            </div>
        );
    }

    const forSummary = billingData?.forSummary;
    const forBreakdown = billingData?.forBreakdown || [];

    return (
        <div className="h-full w-full p-6 flex flex-col gap-4">
            <BillingToolbar />

            <section className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                <BillingSummaryCard
                    name={`cumulative billing for last ${pastTimeWindowDays} days`}
                    value={forSummary ? `${forSummary.cumulativeBilling.toFixed(2)}` : "-"}
                    description={
                        forSummary
                            ? `Based on ${pastTimeWindowDays} day window`
                            : "No data available"
                    }
                    valueClassName="text-primary"
                />

                <BillingSummaryCard
                    name={`projected horizon cost (${forecastTimeWindowDays} days)`}
                    value={forSummary ? `${forSummary.projectedHorizonCost.toFixed(2)}` : "-"}
                    description={
                        forSummary ? `${forecastTimeWindowDays} day forecast` : "No data available"
                    }
                    valueClassName="text-(--chart-4)"
                />
            </section>

            <section className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <BillingStatisticsCard
                    name="forecast vs past variance"
                    value={forSummary ? `${forSummary.forecastVariance.toFixed(2)}%` : "-"}
                    description={
                        forSummary ? "Difference in past and projected spend" : "No data available"
                    }
                    icon={TrendingUp}
                    valueClassName="text-primary"
                />

                <BillingStatisticsCard
                    name="daily burn rate"
                    value={forSummary ? `${forSummary.dailyBurnRate.toFixed(2)}` : "-"}
                    description={forSummary ? "Projected daily spend" : "No data available"}
                    valueClassName="text-primary"
                />

                <BillingStatisticsCard
                    name="Primary cost driver"
                    value={forSummary?.primaryCostDriverLabel || "-"}
                    description={
                        forSummary
                            ? `Charge: ${forSummary.primaryCostDriverId}`
                            : "No data available"
                    }
                    valueClassName="text-(--chart-4)"
                />

                <BillingStatisticsCard
                    name="Highest Cost Acceleration"
                    value={forSummary?.highestCostAccelerationLabel || "-"}
                    description={
                        forSummary
                            ? `Charge: ${forSummary.highestCostAccelerationId}`
                            : "No data available"
                    }
                    valueClassName="text-(--chart-4)"
                />
            </section>

            <section className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                <BillingForecastChart
                    name={`cumulative billing forecast for ${forecastTimeWindowDays} days`}
                    data={forBreakdown.map((breakdown) => ({
                        label: breakdown.label,
                        percent: breakdown.percentage,
                    }))}
                />

                <CostBreakdownList
                    name="cost breakdown"
                    description={`projected charges for ${pastTimeWindowDays} day window`}
                    eachEntry={forBreakdown.map((breakdown) => ({
                        id: breakdown.id,
                        label: breakdown.label,
                        percent: breakdown.percentage,
                    }))}
                    search={breakdownSearch}
                    onSearchChange={setBreakdownSearch}
                />
            </section>
        </div>
    );
}
