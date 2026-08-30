// src/features/dashboard/hooks/useChartData.ts
import { useMemo } from "react";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { metricSeriesToArray } from "@/features/dashboard/types/metric";

export function useChartData(resourceId: string, metricName: string) {
    const series = useMetricStore((state) => state.seriesByKey[`${resourceId}:${metricName}`]);

    return useMemo(() => {
        const values = metricSeriesToArray(series);

        const timeSeriesData = values;

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
