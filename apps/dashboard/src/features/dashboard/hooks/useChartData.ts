// src/features/dashboard/hooks/useChartData.ts
import { useMemo } from "react";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { Metric, metricSeriesToArray } from "@/features/dashboard/types/metric";
import { timestampIsoStringToTime } from "@/lib/timeUtils";

import { sanitizeDisplaySeries } from "@/lib/displayUtils";

export function useChartData(resourceId: string, metricName: string) {
    const series = useMetricStore((state) => state.seriesByKey[`${resourceId}:${metricName}`]);

    return useMemo(() => {
        const values = metricSeriesToArray(series);

        const timeSeriesData = sanitizeDisplaySeries<Metric>(
            values,
            (point) => timestampIsoStringToTime(point.timestamp),
            (samplePoint) => buildPadMetric(samplePoint)
        );

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

function buildPadMetric(previous: Metric): Metric {
    return {
        resource_id: previous.resource_id,
        metricName: previous.metricName,
        metricType: previous.metricType,
        value: null,
        timestamp: new Date(timestampIsoStringToTime(previous.timestamp) + 1).toISOString(),
    };
}
