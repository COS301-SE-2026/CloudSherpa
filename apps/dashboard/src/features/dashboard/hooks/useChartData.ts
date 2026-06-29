// src/features/dashboard/hooks/useChartData.ts
import { useMemo } from "react";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { MetricType } from "@/features/dashboard/types/metric";

export function useChartData(resourceId: string, metricType: MetricType) {
  const series = useMetricStore((state) => state.seriesByKey[`${resourceId}:${metricType}`]);

  return useMemo(() => {
    const rawSeries = series || {};
    const values = Object.values(rawSeries);

    const timeSeriesData = values
      .toSorted((a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime())
      .map((point) => [new Date(point.timestamp).getTime(), point.value]);

    const latestPoint = values.length > 0 
      ? values.reduce((latest, current) => 
          new Date(current.timestamp).getTime() > new Date(latest.timestamp).getTime() ? current : latest
        ) 
      : null;
    
    const currentValue = latestPoint ? latestPoint.value : 0;

    return { 
      timeSeriesData, 
      currentValue 
    };
  }, [series]);
}