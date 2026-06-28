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
  const chartData = useChartData(resourceId, metricType);
  const { colors } = useChartTheme();
  const fromMs = 0;
  const toMs = 0;
  const visibleWindowMs = toMs && fromMs && toMs > fromMs ? toMs - fromMs : 300_000;
  const options: EChartsOption = useMemo(() => {
    // eslint-disable-next-line react-hooks/purity
    const axisMax = Math.ceil(Date.now() / AXIS_TICK_MS) * AXIS_TICK_MS;
    const axisMin = axisMax - visibleWindowMs;
    return {
      color: colors,
      tooltip: {
        trigger: "axis" as const,
        axisPointer: { type: "line" as const },
      },
      grid: { left: "5%", right: "5%", bottom: "10%", top: "15%", containLabel: true },
      xAxis: {
        type: "time" as const,
        min: axisMin,
        max: axisMax,
        splitLine: { show: false },
        axisLabel: { color: "var(--muted-foreground)" },
      },
      yAxis: {
        type: "value" as const,
        splitLine: {
          show: true,
          lineStyle: { color: "var(--border)", type: "dashed" as const },
        },
        axisLabel: { color: "var(--muted-foreground)" },
      },
      series: [
        {
          data: chartData,
          type: "line" as const,
          smooth: true,
          showSymbol: false,
          areaStyle: { opacity: 0.1 },
          lineStyle: { width: 2 },
        },
      ],
    };
  }, [chartData, colors, visibleWindowMs]);

  return <ReactECharts option={options} style={{ height: "100%", width: "100%" }} notMerge={true} lazyUpdate={true} />;
}
