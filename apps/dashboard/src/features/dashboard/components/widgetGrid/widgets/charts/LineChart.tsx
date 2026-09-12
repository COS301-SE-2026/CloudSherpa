"use client";
import type {
    DefaultLabelFormatterCallbackParams,
    EChartsOption,
    TooltipComponentOption,
} from "echarts";
import { useMemo, useEffect } from "react";
import { useChartData } from "@/features/dashboard/hooks/useChartData";
import { useChartTheme } from "@/features/dashboard/hooks/useChartTheme";
import { BaseChart } from "./baseChart";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import { ChartColour } from "@/features/dashboard/types/widgets";

type LineChartProps = {
    resourceId: string;
    metricType: string;
    onDataStatusChange?: (isEmpty: boolean) => void;
    chartColour?: ChartColour;
};

const tooltipTimestampOptions: Intl.DateTimeFormatOptions = {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
};

export function LineChart({
    resourceId,
    metricType,
    onDataStatusChange,
    chartColour,
}: Readonly<LineChartProps>) {
    const { timeSeriesData, hasData } = useChartData(resourceId, metricType);
    const { themeName, tokens, activeColour } = useChartTheme(chartColour);
    const fromMs = useDashboardStore((state) => state.fromMs);
    const toMs = useDashboardStore((state) => state.toMs);

    useEffect(() => {
        onDataStatusChange?.(hasData);
    }, [hasData, onDataStatusChange]);

    const options: EChartsOption = useMemo(() => {
        return {
            color: [activeColour],
            tooltip: {
                trigger: "axis",
                appendTo: () => document.body,
                formatter: (params: DefaultLabelFormatterCallbackParams) => {
                    const point = Array.isArray(params) ? params[0] : params;
                    const value = point.value?.value;

                    const formattedPointTimestamp = new Intl.DateTimeFormat(
                        "en-GB",
                        tooltipTimestampOptions
                    )
                        .format(new Date(point.data?.timestamp))
                        .toString();

                    return `${point.marker} ${Number(value).toFixed(2)} <p style="color: ${tokens["muted-foreground"]}">${formattedPointTimestamp}</p>`;
                },
            } as TooltipComponentOption,
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
                    symbol: "circle",
                    showSymbol: false,
                    symbolSize: 6,
                    emphasis: {
                        itemStyle: {
                            color: activeColour,
                            borderWidth: 2,
                        },
                    },
                },
            ],
        };
    }, [timeSeriesData, tokens, fromMs, toMs, activeColour]);

    return <BaseChart option={options} theme={themeName} />;
}
