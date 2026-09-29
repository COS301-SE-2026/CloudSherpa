"use client";

import { useBillingIntelligenceStore } from "@/features/intelligence/stores/billingIntelligenceStore";
import BillingToolbar from "@/features/intelligence/components/billing/billingToolbar";
import CostBreakdownList from "@/features/intelligence/components/billing/costBreakdownList";
import BillingForecastChart from "@/features/intelligence/components/billing/billingForecastChart";
import BillingStatisticsCard from "@/features/intelligence/components/billing/billingStatisticsCard";
import BillingSummaryCard from "@/features/intelligence/components/billing/billingSummaryCard";
import { TrendingUp } from "lucide-react";
import { useEffect, useState } from "react";
import { useMakeBillingForecast } from "../../hooks/useMakeBillingForecast";
import { getCurrencySymbol } from "@/lib/utils";
import { Spinner } from "@/components/atoms/spinner";
import { BillingSummaryDto } from "../../types/dtos";
import BillingForecastErrorPage from "./billingForecastErrorPage";

const getAccelerationFromSummary = (
    summary: BillingSummaryDto | undefined,
    currency: string,
    highestAccelerationCost: number | undefined
) =>
    highestAccelerationCost !== undefined && summary?.accelerationRate
        ? `${currency}${summary?.accelerationRate.toFixed(4)}/day\u00B2`
        : "-";

export default function BillingIntelligence() {
    const {
        breakdownSearch,
        setBreakdownSearch,
        pastTimeWindowDays,
        forecastTimeWindowDays,
        billingData,
        isLoading,
        setBillingData,
    } = useBillingIntelligenceStore();

    const { makeBillingForecast, billingForecastLoading, billingForecastError } =
        useMakeBillingForecast();

    const [emptyForecast, setEmptyForecast] = useState(false);

    useEffect(() => {
        async function laodForecast() {
            setEmptyForecast(false);
            const result = await makeBillingForecast(forecastTimeWindowDays);

            if (result === null && !billingForecastError) {
                setEmptyForecast(true);
            } else if (result) {
                setBillingData(result);
            }
        }

        void laodForecast();
    }, [forecastTimeWindowDays]);

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

    if (emptyForecast && !billingForecastError) {
        return (
            <BillingForecastErrorPage>
                <h2 className="text-xl font-semibold mb-2">No Forecast Available</h2>
                <p className="text-muted-foreground mb-6">
                    This is likely due to insufficient or no billing data.
                </p>
            </BillingForecastErrorPage>
        );
    }

    if (billingForecastError) {
        return (
            <div className="h-full w-full p-6 flex flex-col gap-4">
                <BillingToolbar />
                <BillingForecastErrorPage>
                    <h2 className="text-xl font-semibold mb-2">No Forecast Available</h2>
                    <p className="text-muted-foreground mb-6">
                        An error occured while attempting to make a forecast.
                    </p>
                </BillingForecastErrorPage>
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
                        primaryDriverCost === undefined
                            ? "-"
                            : `${currency}${primaryDriverCost.toFixed(4)}`
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
                    value={getAccelerationFromSummary(
                        forSummary,
                        currency,
                        highestAccelerationCost
                    )}
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
