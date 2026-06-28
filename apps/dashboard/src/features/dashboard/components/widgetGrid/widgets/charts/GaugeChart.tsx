"use client";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import type { EChartsOption } from "echarts";
import { useMemo } from "react";
import ReactECharts from "echarts-for-react";
import { useChartTheme } from "@/features/dashboard/hooks/useChartTheme";
import { MetricType } from "@/features/dashboard/types/metric";

type GaugeChartProps = {
  resourceId: string;
  metricType: MetricType;
};

export function GaugeChart({ resourceId, metricType }: Readonly<GaugeChartProps>) {
  const { colors } = useChartTheme();
  const series = useMetricStore((state) => state.seriesByKey[`${resourceId}:${metricType}`]);
  const currentValue = useMemo(() => {
    if (!series) return 0;
    const values = Object.values(series);
    if (values.length === 0) return 0;

    const latestPoint = values.toSorted((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime())[0];

    return latestPoint ? latestPoint.value : 0;
  }, [series]);

  const options: EChartsOption = useMemo(
    () => ({
      series: [
        {
          type: "gauge" as const,
          startAngle: 210,
          endAngle: -30,
          min: 0,
          max: 100,
          itemStyle: {
            color: colors[0] || "var(--primary)",
            shadowColor: "rgba(0,0,0,0.1)",
            shadowBlur: 10,
            shadowOffsetY: 4,
          },
          progress: {
            show: true,
            width: 14,
            roundCap: true,
          },
          pointer: {
            show: false,
          },
          axisLine: {
            lineStyle: {
              width: 14,
              color: [[1, "var(--muted)"]],
            },
          },
          axisTick: { show: false },
          splitLine: { show: false },
          axisLabel: { show: false },
          detail: {
            valueAnimation: true,
            fontSize: 28,
            fontWeight: "bold",
            color: "var(--foreground)",
            offsetCenter: [0, 0],
            formatter: "{value}%",
          },
          data: [
            {
              value: Number(currentValue.toFixed(1)),
            },
          ],
        },
      ],
    }),
    [currentValue, colors],
  );

  return <ReactECharts option={options} style={{ height: "100%", width: "100%" }} notMerge={true} lazyUpdate={true} />;
}
