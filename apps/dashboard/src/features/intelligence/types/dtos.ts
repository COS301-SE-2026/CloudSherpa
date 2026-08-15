export interface UsageForecastData {
    horizonTimestamps: string[];
    predictedValues: number[];
    q1Values: number[];
    q3Values: number[];
}

export interface HistoricalUsageSeriesDto {
    values: number[];
    timestamps: string[];
}

export interface BillingForecastSeriesItem {
    value: number;
    percentageOfTotal: number;
    chargeLabel: string;
}

export interface BillingForecastDto {
    cumalativeBillingForecastValue: number;
    cumalitivePastForecastingValue: number;
    billingForecastSeries: Record<string, BillingForecastSeriesItem>;
    failedForecastCharges: string[];
    pastVariance: number;
    dailyBurnRate: number;
    highestCostDriver: string;
    highestCostAcceleration: string;
}

export interface BillingSummaryDto {
    cumulativeBilling: number;
    projectedHorizonCost: number;
    forecastVariance: number;

    dailyBurnRate: number;
    primaryCostDriverId: string;
    primaryCostDriverLabel: string;

    highestCostAccelerationId: string;
    highestCostAccelerationLabel: string;
    currency: string;
}

export interface CostBreakdownItem {
    id: string;
    chargeId: string;
    label: string;

    percentage: number;
    cost: number;
    serviceType: string;

    resourceId: string;
}
