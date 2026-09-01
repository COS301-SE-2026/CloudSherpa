// src/features/dashboard/hooks/useChartData.ts
import { useMemo } from "react";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { Metric, metricSeriesToArray } from "@/features/dashboard/types/metric";
import { timeMs, timestampIsoStringToTime } from "@/lib/timeUtils";

const GAP_THRESHOLD_MILLISECONDS = timeMs.minuteMs * 5;

export function useChartData(resourceId: string, metricName: string) {
    const series = useMetricStore((state) => state.seriesByKey[`${resourceId}:${metricName}`]);

    return useMemo(() => {
        const values = metricSeriesToArray(series);

        const timeSeriesData = sanitizeDisplaySeries(values);

        const latestPoint = values.length > 0 ? values.at(-1) : null;

        const currentValue = latestPoint ? latestPoint.value : 0;

        const hasData = values.length > 0;

        return {
            timeSeriesData,
            currentValue,
            hasData,
        };
    }, [series]);
}

function sanitizeDisplaySeries(dirtyValues: Metric[]): Metric[] {
    const sanatizedSeries: Metric[] = [];

    for (let i = 0; i < dirtyValues.length - 1; i++) {
        const current = dirtyValues[i];
        const next = dirtyValues[i + 1];

        sanatizedSeries.push(current);

        if (
            timestampIsoStringToTime(next.timestamp) - timestampIsoStringToTime(current.timestamp) >
            GAP_THRESHOLD_MILLISECONDS
        ) {
            sanatizedSeries.push(
                buildPadMetric(current, timestampIsoStringToTime(current.timestamp) + 1)
            );
        }
    }

    const lastMetric = dirtyValues.at(-1);

    if (lastMetric !== undefined) {
        sanatizedSeries.push(lastMetric);
    }

    console.log(sanatizedSeries);

    return sanatizedSeries;
}

function buildPadMetric(seriesSampledMetric: Metric, nullPadTimestamp: number): Metric {
    return {
        resource_id: seriesSampledMetric.resource_id,
        metricName: seriesSampledMetric.metricName,
        metricType: seriesSampledMetric.metricType,
        value: null,
        timestamp: new Date(nullPadTimestamp).toISOString(),
    };
}
