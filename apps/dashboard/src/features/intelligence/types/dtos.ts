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
    cumalativePastForecastValue: number;
    billingForecastSeries: Record<string, BillingForecastSeriesItem>;
    failedForecastCharges: string[];
    pastVariance: number;
    dailyBurnRate: number;
    highestCostDriver: string;
    highestCostAcceleration: string;
}
