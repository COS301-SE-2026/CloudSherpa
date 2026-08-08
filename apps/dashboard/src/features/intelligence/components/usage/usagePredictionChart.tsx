"use client";

import { useMemo } from "react";
import ReactECharts from "echarts-for-react";
import type { CallbackDataParams } from "echarts/types/dist/shared";
import { formatChartData } from "@/features/intelligence/hooks/formatChartData";
import { ResourceUsageForecastResponseDto } from "@/features/intelligence/types/metrics";
import { useChartTheme } from "@/features/dashboard/hooks/useChartTheme";
import { useChartData } from "@/features/dashboard/hooks/useChartData";
import { MetricType } from "@/features/dashboard/types/metric";
import { useUsageIntelligenceStore } from "@/features/intelligence/stores/useUsageIntelligenceStore";

interface UsagePredictionChartProps {
    resourceId: string;
    metricType: MetricType;
    forecastDto: ResourceUsageForecastResponseDto | null;
    metricTypeLabel: string;
}

const currentTime = Date.now();

export default function UsagePredictionChart({
    resourceId,
    metricType,
    forecastDto,
    metricTypeLabel,
}: Readonly<UsagePredictionChartProps>) {
    const { timeSeriesData } = useChartData(resourceId, metricType);
    const { themeName, tokens } = useChartTheme();

    const pastTimeWindowDays = useUsageIntelligenceStore((state) => state.pastTimeWindowDays);

    const { historicalData, q1Data, q3Data, predictedData } = useMemo(
        () => formatChartData(timeSeriesData, forecastDto),
        [timeSeriesData, forecastDto]
    );

    const oneDayMs = 24 * 60 * 60 * 1000;
    const minXAxisTime = currentTime - pastTimeWindowDays * oneDayMs;
    const maxXAxisTime = currentTime + oneDayMs;

    const option = {
        tooltip: {
            trigger: "axis",
            axisPointer: { type: "cross" },
            formatter: function (params: CallbackDataParams[]) {
                if (!params || params.length === 0) return "";
                const firstPoint = params[0] as CallbackDataParams & { axisValueLabel?: string };
                let tooltipHtml = `<b>${firstPoint.axisValueLabel || firstPoint.name}</b><br/>`;
                params.forEach((param) => {
                    if (param.seriesName === "Lower Bound") return;

                    const paramValue = param.value as [number, number] | undefined;
                    let value = paramValue?.[1] ?? 0;
                    let label = param.seriesName;

                    if (label === "Confidence Band") {
                        const lowerBoundParam = params.find((p) => p.seriesName === "Lower Bound");
                        const lowerBoundValue = lowerBoundParam?.value as
                            [number, number] | undefined;
                        const q1Value = lowerBoundValue?.[1] ?? 0;

                        value = q1Value + value;
                        label = "Upper Bound (Q3)";
                    }

                    tooltipHtml += `${param.marker} ${label}: ${value.toFixed(2)}<br/>`;
                });
                return tooltipHtml;
            },
        },
        legend: {
            data: ["Historical Usage", "Predicted Usage"],
            bottom: 0,
            textStyle: { color: tokens["muted-foreground"] },
        },
        grid: { left: "3%", right: "4%", bottom: "10%", containLabel: true },
        xAxis: {
            type: "time",
            boundaryGap: false,
            min: minXAxisTime,
            max: maxXAxisTime,
        },
        yAxis: {
            type: "value",
            name: metricTypeLabel,
            nameTextStyle: { color: tokens["muted-foreground"] },
        },
        series: [
            {
                name: "Historical Usage",
                type: "line",
                showSymbol: false,
                data: historicalData,
                lineStyle: {
                    width: 2.5,
                    color: tokens["chart-1"],
                },
            },

            {
                name: "Lower Bound",
                type: "line",
                data: q1Data,
                stack: "confidence-band",
                lineStyle: { opacity: 0 },
                showSymbol: false,
            },
            {
                name: "Confidence Band",
                type: "line",
                data: q3Data,
                stack: "confidence-band",
                lineStyle: { opacity: 0 },
                areaStyle: {
                    color: tokens["chart-2"],
                    opacity: 0.15,
                },
                showSymbol: false,
            },

            {
                name: "Predicted Usage",
                type: "line",
                data: predictedData,
                showSymbol: false,
                lineStyle: {
                    width: 2.5,
                    type: "dashed",
                    color: tokens["chart-2"],
                },
                markLine: {
                    symbol: ["none", "none"],
                    label: {
                        formatter: "Now",
                        position: "insideStartTop",
                        color: tokens["muted-foreground"],
                    },
                    lineStyle: {
                        type: "dashed",
                        color: tokens["border"],
                        width: 2,
                    },
                    data: [{ xAxis: currentTime }],
                },
            },
        ],
    };

    return (
        <div className="w-full bg-card rounded-lg border border-border p-4 shadow-sm">
            <ReactECharts
                option={option}
                theme={themeName}
                style={{ height: "600px", width: "100%" }}
                notMerge={true}
            />
        </div>
    );
}
