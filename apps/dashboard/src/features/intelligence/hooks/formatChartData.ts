import { HistoricalUsageSeriesDto, UsageForecastData } from "../types/dtos";
import { timeMs } from "@/lib/timeUtils";

type TimeValuePoint = [number, number];

function toBrowserTimezoneTimestamp(isoString: string): number {
    const timestamp = new Date(isoString).getTime();
    const timezoneOffsetMs = new Date(timestamp).getTimezoneOffset() * timeMs.minuteMs;
    return timestamp - timezoneOffsetMs;
}

export function formatChartData(
    historicalUsageSeries: HistoricalUsageSeriesDto | null,
    usageForecast: UsageForecastData | null
) {
    const historicalUsagePoints: TimeValuePoint[] = [];

    if (historicalUsageSeries) {
        const pointCount = Math.min(
            historicalUsageSeries.values.length,
            historicalUsageSeries.timestamps.length
        );
        for (let i = 0; i < pointCount; i++) {
            const value = historicalUsageSeries.values[i];
            const isoString = historicalUsageSeries.timestamps[i];

            const timestamp = toBrowserTimezoneTimestamp(isoString);

            historicalUsagePoints.push([timestamp, value]);
        }
    }

    const lowerConfidenceBoundPoints: TimeValuePoint[] = [];
    const confidenceBandRangePoints: TimeValuePoint[] = [];
    const predictedUsagePoints: TimeValuePoint[] = [];
    if (usageForecast) {
        usageForecast.horizonTimestamps.forEach((isoString, index) => {
            const timestamp = toBrowserTimezoneTimestamp(isoString);
            const lowerBound = usageForecast.q1Values[index];
            const upperBound = usageForecast.q3Values[index];
            const predictedValue = usageForecast.predictedValues[index];

            lowerConfidenceBoundPoints.push([timestamp, lowerBound]);
            confidenceBandRangePoints.push([timestamp, upperBound - lowerBound]);
            predictedUsagePoints.push([timestamp, predictedValue]);
        });
    }

    return {
        historicalUsagePoints,
        lowerConfidenceBoundPoints,
        confidenceBandRangePoints,
        predictedUsagePoints,
    };
}
