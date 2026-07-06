// src/features/dashboard/hooks/useChartData.ts
import { useMemo } from "react";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { MetricType } from "@/features/dashboard/types/metric";

export function useChartData(resourceId: string, metricType: MetricType) {
  const series = useMetricStore((state) => state.seriesByKey[`${resourceId}:${metricType}`]);

  return useMemo(() => {
    const rawSeries = series || {};
    const values = Object.values(rawSeries);

    if (values.length === 0) {
      return { timeSeriesData: [], currentValue: 0 };
    }

    const sortedValues = values.toSorted((a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime());

    const timeSeriesData = sortedValues.map((point) => [new Date(point.timestamp).getTime(), point.value]);

    const currentValue = sortedValues.at(-1)?.value ?? 0;
    
    return {
      timeSeriesData,
      currentValue,
    };
  }, [series]);
}
