export type IntelligenceMetricType =
    | "cpu"
    | "memory"
    | "storage-used"
    | "storage-available"
    | "object-count"
    | "duration"
    | "throttles"
    | "disk"
    | "network"
    | "read-capacity"
    | "write-capacity"
    | "first-byte-latency"
    | "latency"
    | "errors"
    | "requests"
    | "connections"
    | "invocations"
    | "anon";

export interface ResourceUsageForecastRequestDto {
    resourceId: string;
    metricType: string;
    forecastHorizon: string;
}

export interface ResourceUsageForecastResponseDto {
    horizonTimestamps: string[];
    predictedValues: number[];
    q1Values: number[];
    q3Values: number[];
}

export interface BillingForecastRequestDto {
    forecastHorizon: string;
    chargeIds: string[];
}

export interface BillingForecastResponseDto {
    cumalativeBillingForecastValue: number;
    timestamps: string[];
    billingForecastSeries: Record<string, number[]>;
}

export interface ForecastChartData {
    historicalData: [number, number][];
    q1Data: [number, number][];
    q3Data: [number, number][];
    predictedData: [number, number][];
}
