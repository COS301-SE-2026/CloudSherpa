import { useMemo } from "react";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { MetricType, MetricSeries } from "@/features/dashboard/types/metric";

const metricSeriesToArray = (series: MetricSeries) => {
  return Object.values(series)
    .sort((a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime())
    .map((point) => [new Date(point.timestamp).getTime(), point.value]);
};

export function useChartData(resourceId: string, metricType: MetricType) {
  const series = useMetricStore((state) => state.seriesByKey[`${resourceId}:${metricType}`]);

  const chartData = useMemo(() => {
    return metricSeriesToArray(series || {});
  }, [series]);

  return chartData;
}
