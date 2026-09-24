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
import { getCurrencySymbol } from "@/lib/utils";
import { Spinner } from "@/components/atoms/spinner";

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

    const { makeBillingForecast, billingForecastLoading } = useMakeBillingForecast();

    const selected = disableFilters || (provider && accountId && resourceId);

    useEffect(() => {
        async function laodForecast() {
            const result = await makeBillingForecast(forecastTimeWindowDays);
            if (result) {
                setBillingData(result);
            }
        }

        if (selected) {
            void laodForecast();
        }
    }, [forecastTimeWindowDays, selected]);

    const forSummary = billingData?.forSummary;
    const forBreakdown = billingData?.forBreakdown || [];

    const currency = getCurrencySymbol(forSummary?.currency ?? "USD");
    const loading = isLoading || billingForecastLoading;

    const primaryDriverCost = forBreakdown.find(
        (item) => item.chargeId === forSummary?.primaryCostDriverId
    )?.cost;
    const highestAccelerationCost = forBreakdown.find(
        (item) => item.chargeId === forSummary?.highestCostAccelerationId
    )?.cost;

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

    if (loading) {
        return (
            <div className="h-full w-full p-6 flex flex-col gap-4">
                <BillingToolbar />

                <div className="h-full w-full flex flex-col justify-center items-center ">
                    <Spinner className="h-10 w-10" />
                </div>
            </div>
        );
    }

    return (
        <div className="h-full w-full p-6 flex flex-col gap-4">
            <BillingToolbar />

            <section className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                <BillingSummaryCard
                    name={`Cumulative billing for last ${forecastTimeWindowDays} days`}
                    value={
                        forSummary ? `${currency}${forSummary.cumulativeBilling.toFixed(4)}` : "-"
                    }
                    description={
                        forSummary
                            ? `Based on ${pastTimeWindowDays} day window`
                            : "No data available"
                    }
                    valueClassName="text-primary"
                    tooltip={`Cumulative billed amount over the past ${pastTimeWindowDays} days`}
                />

                <BillingSummaryCard
                    name={`Projected horizon cost (${forecastTimeWindowDays} days)`}
                    value={
                        forSummary
                            ? `${currency}${forSummary.projectedHorizonCost.toFixed(4)}`
                            : "-"
                    }
                    description={
                        forSummary ? `${forecastTimeWindowDays} day forecast` : "No data available"
                    }
                    valueClassName="text-(--chart-4)"
                    tooltip={`Total estimated cost for the upcoming ${forecastTimeWindowDays} days`}
                />
            </section>

            <section className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <BillingStatisticsCard
                    name="Forecast vs past variance"
                    value={forSummary ? `${forSummary.forecastVariance.toFixed(2)}%` : "-"}
                    description={
                        forSummary ? "Difference in past and projected spend" : "No data available"
                    }
                    icon={TrendingUp}
                    valueClassName="text-primary"
                    tooltip={`Percentage difference between the past ${pastTimeWindowDays} days of spending and the projected ${forecastTimeWindowDays} days`}
                />

                <BillingStatisticsCard
                    name="Daily burn rate"
                    value={forSummary ? `${currency}${forSummary.dailyBurnRate.toFixed(4)}` : "-"}
                    description={forSummary ? "Projected daily spend" : "No data available"}
                    valueClassName="text-primary"
                    tooltip={`Estimated average daily cost over the next ${forecastTimeWindowDays} days`}
                />

                <BillingStatisticsCard
                    name="Primary cost driver"
                    value={
                        primaryDriverCost !== undefined
                            ? `${currency}${primaryDriverCost.toFixed(4)}`
                            : "-"
                    }
                    description={
                        forSummary
                            ? `Charge: ${forSummary.primaryCostDriverLabel}`
                            : "No data available"
                    }
                    valueClassName="text-(--chart-4)"
                    tooltip={`The specific charge expected to be the most expensive over the next ${forecastTimeWindowDays} days`}
                />

                <BillingStatisticsCard
                    name="Highest cost acceleration"
                    value={
                        highestAccelerationCost !== undefined
                            ? `${currency}${forSummary?.accelerationRate.toFixed(4)}/day\u00B2`
                            : "-"
                    }
                    description={
                        forSummary
                            ? `Charge: ${forSummary?.highestCostAccelerationLabel || "-"}`
                            : "No data available"
                    }
                    valueClassName="text-(--chart-4)"
                    tooltip={`The charge expected to increase in cost the fastest over the next ${forecastTimeWindowDays} days`}
                />
            </section>

            <section className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                <BillingForecastChart
                    name={`Cumulative billing forecast for ${forecastTimeWindowDays} days`}
                    data={forBreakdown.map((breakdown) => ({
                        label: breakdown.label,
                        percent: breakdown.percentage,
                    }))}
                />

                <CostBreakdownList
                    name="Individual charge cost breakdown"
                    description={`Projected charges for ${forecastTimeWindowDays} day window`}
                    eachEntry={forBreakdown}
                    search={breakdownSearch}
                    onSearchChange={setBreakdownSearch}
                />
            </section>
        </div>
    );
}
