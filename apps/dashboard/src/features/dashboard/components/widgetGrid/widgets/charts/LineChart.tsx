"use client";
import { MetricType } from "@/features/dashboard/types/metric";
import type { EChartsOption } from "echarts";
import { useMemo } from "react";
import { useChartData } from "@/features/dashboard/hooks/useChartData";
import { useChartTheme } from "@/features/dashboard/hooks/useChartTheme";
import ReactECharts from "echarts-for-react";

type LineChartProps = {
  resourceId: string;
  metricType: MetricType;
};

const AXIS_TICK_MS = 6000;

export function LineChart({ resourceId, metricType }: Readonly<LineChartProps>) {
  const { timeSeriesData } = useChartData(resourceId, metricType);
  const { themeName, tokens } = useChartTheme();
  const fromMs = 0;
  const toMs = 0;
  const visibleWindowMs = toMs && fromMs && toMs > fromMs ? toMs - fromMs : 300_000;
  const options: EChartsOption = useMemo(() => {
    // eslint-disable-next-line react-hooks/purity
    const axisMax = Math.ceil(Date.now() / AXIS_TICK_MS) * AXIS_TICK_MS;
    const axisMin = axisMax - visibleWindowMs;
    return {
      grid: { left: "1%", right: "4%", bottom: "2%", top: "10%", containLabel: true },
      xAxis: {
        type: "time" as const,
        min: axisMin,
        max: axisMax,
      },
      yAxis: {
        type: "value" as const,
      },
      series: [
        {
          data: timeSeriesData,
          type: "line" as const,
          areaStyle: {
            opacity: 0.2,
            color: {
              type: "linear",
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: tokens["chart-1"] || tokens["primary"] },
                { offset: 1, color: "transparent" },
              ],
            },
          },
        },
      ],
    };
  }, [timeSeriesData, tokens, visibleWindowMs]);

  return (
    <ReactECharts
      option={options}
      theme={themeName} 
      style={{ height: "100%", width: "100%" }}
      notMerge={true}
      lazyUpdate={true}
    />
  );
}
