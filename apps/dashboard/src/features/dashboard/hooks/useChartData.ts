// src/features/dashboard/hooks/useChartData.ts
import { useMemo } from "react";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { MetricType } from "@/features/dashboard/types/metric";

export function useChartData(resourceId: string, metricType: MetricType) {
    const series = useMetricStore((state) => state.seriesByKey[`${resourceId}:${metricType}`]);

    return useMemo(() => {
        const rawSeries = series || {};
        const values = Object.values(rawSeries);

        const timeSeriesData = values;

        const latestPoint = values.length > 0 ? values.at(-1) : null;

        const currentValue = latestPoint ? latestPoint.value : 0;

        return {
            timeSeriesData,
            currentValue,
        };
    }, [series]);
}
