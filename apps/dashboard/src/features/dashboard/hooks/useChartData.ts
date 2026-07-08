import { useMemo } from "react";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";

export function useChartData(resourceId: string, metricType: string) {
  const series = useMetricStore((state) => state.seriesByKey[`${resourceId}:${metricType}`]);

  return useMemo(() => {
    const timeSeriesData = Array.isArray(series) ? series : [];

    const latestPoint = timeSeriesData.length > 0 ? timeSeriesData[timeSeriesData.length - 1] : null;

    const currentValue = latestPoint ? latestPoint[1] : 0;

    return {
      timeSeriesData,
      currentValue,
    };
  }, [series]);
}
