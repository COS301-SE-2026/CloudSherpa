import { Metric } from "@/features/dashboard/types/metric";
import { UsageForecastData } from "../types/metrics";

function toBrowserTimezoneTimestamp(isoString: string): number {
    const timestamp = new Date(isoString).getTime();
    const timezoneOffsetMs = new Date(timestamp).getTimezoneOffset() * 60 * 1000;
    return timestamp - timezoneOffsetMs;
}

export function formatChartData(historicalMetrics: Metric[], forecastDto: UsageForecastData | null) {
    const historicalData: [number, number][] = historicalMetrics.map((m) => [
        new Date(m.timestamp).getTime(),
        m.value,
    ]);

    const q1Data: [number, number][] = [];
    const q3Data: [number, number][] = [];
    const predictedData: [number, number][] = [];
    if (forecastDto) {
        forecastDto.horizonTimestamps.forEach((isoString, index) => {
            const timestamp = toBrowserTimezoneTimestamp(isoString);
            const q1 = forecastDto.q1Values[index];
            const q3 = forecastDto.q3Values[index];
            const pred = forecastDto.predictedValues[index];

            q1Data.push([timestamp, q1]);
            q3Data.push([timestamp, q3 - q1]);
            predictedData.push([timestamp, pred]);
        });
    }

    return { historicalData, q1Data, q3Data, predictedData };
}
