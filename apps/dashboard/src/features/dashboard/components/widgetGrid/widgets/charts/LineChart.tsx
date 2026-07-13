"use client";
import { MetricType } from "@/features/dashboard/types/metric";
import type { EChartsOption } from "echarts";
import { useMemo } from "react";
import { useChartData } from "@/features/dashboard/hooks/useChartData";
import { useChartTheme } from "@/features/dashboard/hooks/useChartTheme";
import { BaseChart } from "./baseChart";
import { useWindowStore } from "@/features/dashboard/stores/window-store";

type LineChartProps = {
    resourceId: string;
    metricType: MetricType;
};

export function LineChart({ resourceId, metricType }: Readonly<LineChartProps>) {
    const { timeSeriesData } = useChartData(resourceId, metricType);
    const { themeName, tokens } = useChartTheme();
    const fromMs = useWindowStore((state) => state.fromMs);
    const toMs = useWindowStore((state) => state.toMs);
    const options: EChartsOption = useMemo(() => {
        return {
            grid: { left: "1%", right: "4%", bottom: "2%", top: "10%", containLabel: true },
            xAxis: {
                type: "time" as const,
                min: fromMs,
                max: toMs,
                axisLabel: {
                    hideOverlap: true,
                    formatter: {
                        year: "{yyyy}",
                        month: "{MMM}",
                        day: "{ee} {d}",
                        hour: "{HH}:{mm}",
                        minute: "{HH}:{mm}",
                        second: "{HH}:{mm}:{ss}",
                    },
                },
            },
            yAxis: {
                type: "value" as const,
            },
            dataset: {
                dimensions: [
                    { name: "timestamp", type: "time" },
                    { name: "value", type: "number" },
                ],
                source: timeSeriesData,
            },
            series: [
                {
                    type: "line" as const,
                    encode: {
                        x: "timestamp",
                        y: "value",
                    },
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
    }, [timeSeriesData, tokens, fromMs, toMs]);

    return <BaseChart option={options} theme={themeName} />;
}
