"use client";
import type { EChartsOption } from "echarts";
import { useMemo } from "react";
import ReactECharts from "echarts-for-react";
import { useChartTheme } from "@/features/dashboard/hooks/useChartTheme";
import { MetricType } from "@/features/dashboard/types/metric";
import { useChartData } from "@/features/dashboard/hooks/useChartData";

type GaugeChartProps = {
  resourceId: string;
  metricType: MetricType;
};

export function GaugeChart({ resourceId, metricType }: Readonly<GaugeChartProps>) {
  const { themeName, tokens } = useChartTheme();
  const { currentValue } = useChartData(resourceId, metricType);

  const options: EChartsOption = useMemo(() => {
    const primaryColor = tokens["chart-1"] || tokens["primary"] || "#327dcd";
    const textColor = tokens["foreground"] || "auto";
    const isLightMode = themeName === "cloudSherpaLight";
    return {
      series: [
        {
          type: "gauge" as const,
          radius: "90%",
          center: ["50%", "60%"],
          startAngle: 200,
          endAngle: -20,
          min: 0,
          max: 100,
          progress: {
            show: true,
            width: 15,
            roundCap: true,
            itemStyle: {
              color: primaryColor,
              shadowColor: isLightMode ? "transparent" : primaryColor,
              shadowBlur: isLightMode ? 0 : 4,
            },
          },
          pointer: { show: false },
          axisLine: {
            roundCap: true,
            lineStyle: {
              width: 15,
              color: [[1, tokens["border"] || "#33393f"]],
            },
          },
          axisTick: { show: false },
          splitLine: { show: false },
          axisLabel: { show: false },
          detail: {
            show: true,
            offsetCenter: [0, 0],
            valueAnimation: true,
            fontSize: 24,
            fontWeight: "bold",
            color: textColor,
            fontFamily: "sans-serif",
            formatter: (value: number) => `${Math.round(value)}%`,
          },
          data: [{ value: Number(currentValue.toFixed(1)) }],
        },
      ],
    };
  }, [currentValue, tokens]);

  return (
    <ReactECharts
      option={options}
      theme={themeName}
      style={{ height: "100%", width: "100%"}}
      notMerge={true}
      lazyUpdate={true}
    />
  );
}
