"use client";

import { useMemo } from "react";
import ReactECharts from "echarts-for-react";
import type { CallbackDataParams } from "echarts/types/dist/shared";
import { formatChartData } from "@/features/intelligence/hooks/formatChartData";
import { useChartTheme } from "@/features/dashboard/hooks/useChartTheme";
import { useChartData } from "@/features/dashboard/hooks/useChartData";
import { useUsageIntelligenceConfigStore } from "@/features/intelligence/stores/useUsageIntelligenceConfigStore";
import { useUsageIntelligenceStore } from "@/features/intelligence/stores/useUsageIntelligenceStore";

const now = Date.now();

export default function UsagePredictionChart() {
    //styles
    const { themeName, tokens } = useChartTheme();

    //config
    const resourceId = useUsageIntelligenceConfigStore((state) => state.resourceId);
    const metricType = useUsageIntelligenceConfigStore((state) => state.metricType);
    const pastTimeWindowDays = useUsageIntelligenceConfigStore((state) => state.pastTimeWindowDays);

    //data
    const forecastedMetrics = useUsageIntelligenceStore((state) => {
        if (!resourceId || !metricType) return null;
        return state.forecasts[resourceId]?.[metricType] ?? null;
    });

    const { timeSeriesData } = useChartData(resourceId || "", metricType || "anon");

    const { historicalData, q1Data, q3Data, predictedData } = useMemo(
        () => formatChartData(timeSeriesData, forecastedMetrics),
        [timeSeriesData, forecastedMetrics]
    );

    // 3. X-AXIS MATH HOOK
    const { currentTime, minXAxisTime, maxXAxisTime } = useMemo(() => {
        const oneDayMs = 24 * 60 * 60 * 1000;
        const minTime = now - pastTimeWindowDays * oneDayMs;
        let maxTime = now;

        if (forecastedMetrics && forecastedMetrics.horizonTimestamps.length > 0) {
            const lastForecastIndex = forecastedMetrics.horizonTimestamps.length - 1;
            const lastForecastIso = forecastedMetrics.horizonTimestamps[lastForecastIndex];
            maxTime = new Date(lastForecastIso).getTime();
        } else {
            maxTime = now + oneDayMs;
        }

        return {
            currentTime: now,
            minXAxisTime: minTime,
            maxXAxisTime: maxTime,
        };
    }, [pastTimeWindowDays, forecastedMetrics]);

    if (!resourceId || !metricType) return null;

    const metricTypeLabel = metricType.toUpperCase();

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
            icon: "circle",
            bottom: 0,
            textStyle: {
                fontSize: 12,
                color: tokens["muted-foreground"],
            },
            data: [
                {
                    name: "Historical Usage",
                    itemStyle: { color: tokens["chart-1"] },
                },
                {
                    name: "Predicted Usage",
                    itemStyle: { color: tokens["chart-2"] },
                },
            ],
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
            nameTextStyle: { color: tokens["muted-foreground"] },
        },
        series: [
            {
                name: "Historical Usage",
                type: "line",
                showSymbol: false,
                data: historicalData,
                lineStyle: {
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
