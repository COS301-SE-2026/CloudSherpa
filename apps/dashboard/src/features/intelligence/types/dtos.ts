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
