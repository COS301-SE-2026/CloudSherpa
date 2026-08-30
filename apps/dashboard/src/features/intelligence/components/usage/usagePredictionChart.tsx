"use client";

import { useMemo, useRef } from "react";
import ReactECharts from "echarts-for-react";
import { AlertCircleIcon, RotateCcw, ZoomIn, ZoomOut } from "lucide-react";
import type { CallbackDataParams } from "echarts/types/dist/shared";
import { formatChartData } from "@/features/intelligence/hooks/formatChartData";
import { useChartTheme } from "@/features/dashboard/hooks/useChartTheme";
import { useUsageIntelligenceConfigStore } from "@/features/intelligence/stores/useUsageIntelligenceConfigStore";
import { useUsageIntelligenceStore } from "@/features/intelligence/stores/useUsageIntelligenceStore";
import { Card, CardContent, CardHeader } from "@/components/atoms/card";
import { Button } from "@/components/atoms/button";
import { timeMs, durationByPreset } from "@/lib/timeUtils";
import { HistoricalUsageSeriesDto } from "../../types/dtos";
import { UsageError } from "../../types/errors";
import { Spinner } from "@/components/atoms/spinner";

interface UsagePredictionChartProps {
    readonly historicalUsageSeries: HistoricalUsageSeriesDto | null;
    readonly usageError: UsageError | null;
    readonly loading: boolean;
}

const now = Date.now();

export default function UsagePredictionChart({
    historicalUsageSeries,
    usageError,
    loading,
}: UsagePredictionChartProps) {
    //styles
    const { themeName, tokens } = useChartTheme();

    //config
    const resourceId = useUsageIntelligenceConfigStore((state) => state.resourceId);
    const metricName = useUsageIntelligenceConfigStore((state) => state.metricName);
    const pastTimeWindowPreset = useUsageIntelligenceConfigStore(
        (state) => state.pastTimeWindowPreset
    );

    //data
    const usageForecast = useUsageIntelligenceStore((state) => {
        if (!resourceId || !metricName) return null;
        return state.forecasts[resourceId]?.[metricName] ?? null;
    });

    const {
        historicalUsagePoints,
        lowerConfidenceBoundPoints,
        confidenceBandRangePoints,
        predictedUsagePoints,
    } = useMemo(
        () => formatChartData(historicalUsageSeries, usageForecast),
        [historicalUsageSeries, usageForecast]
    );

    // 3. X-AXIS MATH HOOK
    const { currentTime, minXAxisTime, maxXAxisTime } = useMemo(() => {
        const minTime = now - durationByPreset[pastTimeWindowPreset];
        let maxTime: number;

        if (usageForecast && usageForecast.horizonTimestamps.length > 0) {
            const lastForecastIndex = usageForecast.horizonTimestamps.length - 1;
            const lastForecastIso = usageForecast.horizonTimestamps[lastForecastIndex];
            maxTime = new Date(lastForecastIso).getTime();
        } else {
            maxTime = now + timeMs.dayMs;
        }

        return {
            currentTime: now,
            minXAxisTime: minTime,
            maxXAxisTime: maxTime,
        };
    }, [pastTimeWindowPreset, usageForecast]);

    const echartsRef = useRef<ReactECharts>(null);

    //reset zoom btn
    const handleResetZoom = () => {
        const echartsInstance = echartsRef.current?.getEchartsInstance();
        if (!echartsInstance) return;

        echartsInstance.dispatchAction({
            type: "dataZoom",
            start: 0,
            end: 100,
        });
    };

    //icnrease and decrease zoom btns
    const handleZoom = (direction: "in" | "out") => {
        const echartsInstance = echartsRef.current?.getEchartsInstance();
        if (!echartsInstance) return;

        const option = echartsInstance.getOption() as {
            dataZoom?: Array<{ start?: number; end?: number }>;
        };

        const dz = option?.dataZoom?.[0];
        const currentStart = dz?.start ?? 0;
        const currentEnd = dz?.end ?? 100;

        const currentSpan = currentEnd - currentStart;
        const center = (currentStart + currentEnd) / 2;

        const factor = direction === "in" ? 0.7 : 1.3;
        let newSpan = currentSpan * factor;

        if (newSpan < 1) newSpan = 1;
        if (newSpan > 100) newSpan = 100;

        let newStart = center - newSpan / 2;
        let newEnd = center + newSpan / 2;

        if (newStart < 0) {
            newStart = 0;
            newEnd = Math.min(100, newSpan);
        }
        if (newEnd > 100) {
            newEnd = 100;
            newStart = Math.max(0, 100 - newSpan);
        }

        echartsInstance.dispatchAction({
            type: "dataZoom",
            start: newStart,
            end: newEnd,
        });
    };

    if (!resourceId || !metricName) return null;

    const option = {
        tooltip: {
            trigger: "axis",
            axisPointer: {
                type: "cross",
                animation: false,
                snap: true,
                label: {
                    backgroundColor: tokens["chart-1"],
                },
            },
            formatter: function (params: CallbackDataParams[]) {
                if (!params || params.length === 0) return "";
                const firstPoint = params[0] as CallbackDataParams & { axisValueLabel?: string };
                let tooltipHtml = `<b>${firstPoint.axisValueLabel || firstPoint.name}</b><br/>`;

                let predictedValue: number | null = null;
                let hasForecast = false;

                for (const param of params) {
                    const paramValue = param.value as [number, number] | undefined;
                    const value = paramValue?.[1] ?? 0;

                    if (param.seriesName === "Historical Usage") {
                        tooltipHtml += `${param.marker} Historical Usage: ${value.toFixed(2)}<br/>`;
                    } else if (param.seriesName === "Predicted Usage") {
                        predictedValue = value;
                        hasForecast = true;
                    }
                }

                if (hasForecast && predictedValue !== null) {
                    const primaryColor = tokens["chart-2"];
                    const solidMarker = `<span style="display:inline-block;margin-right:4px;border-radius:10px;width:10px;height:10px;background-color:${primaryColor};"></span>`;

                    tooltipHtml += `${solidMarker} Predicted Usage: ${predictedValue.toFixed(2)}<br/>`;
                }

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
        dataZoom: [
            {
                type: "inside",
                xAxisIndex: 0,
                start: 0,
                end: 100,
                moveOnMouseMove: true,
                moveOnMouseWheel: true,
                zoomOnMouseWheel: "ctrl",
            },
        ],
        series: [
            {
                name: "Historical Usage",
                type: "line",
                symbol: "circle",
                symbolSize: 6,
                showSymbol: false,
                emphasis: {
                    focus: "series",
                    scale: true,
                },
                data: historicalUsagePoints,
                lineStyle: {
                    color: tokens["chart-1"],
                },
            },
            {
                name: "Lower Bound",
                type: "line",
                data: lowerConfidenceBoundPoints,
                stack: "confidence-band",
                lineStyle: { opacity: 0 },
                symbol: "none",
            },
            {
                name: "Upper Bound",
                type: "line",
                data: confidenceBandRangePoints,
                stack: "confidence-band",
                symbol: "none",
                lineStyle: { opacity: 0 },
                areaStyle: {
                    color: tokens["chart-2"],
                    opacity: 0.15,
                },
            },
            {
                name: "Predicted Usage",
                type: "line",
                data: predictedUsagePoints,
                showSymbol: false,
                symbol: "circle",
                symbolSize: 6,
                emphasis: {
                    focus: "series",
                    scale: true,
                    itemStyle: {
                        color: tokens["chart-2"],
                    },
                },
                lineStyle: {
                    width: 2.5,
                    color: tokens["chart-2"],
                },
                markLine: {
                    //mark start forecast
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

    const cardContent = !loading ? (
        <ReactECharts
            ref={echartsRef}
            option={option}
            theme={themeName}
            style={{ height: "100%", width: "100%" }}
            notMerge={true}
        />
    ) : (
        <Spinner className="w-10 h-10" />
    );

    return (
        <Card className="h-full w-full gap-0 overflow-hidden">
            <CardHeader className="flex flex-row justify-end items-center gap-1 ">
                <Button
                    onClick={() => handleZoom("in")}
                    variant="ghost"
                    size="icon"
                    title="Zoom In"
                >
                    <ZoomIn className="h-4 w-4" />
                </Button>
                <Button
                    onClick={() => handleZoom("out")}
                    variant="ghost"
                    size="icon"
                    title="Zoom Out"
                >
                    <ZoomOut className="h-4 w-4" />
                </Button>
                <Button onClick={handleResetZoom} variant="ghost" size="icon" title="Reset Zoom">
                    <RotateCcw className="h-4 w-4" />
                </Button>
            </CardHeader>
            <CardContent className="h-full p-0">
                {usageError?.item == "both" ? (
                    <div className="flex h-full w-full flex-col justify-center items-center gap-6">
                        <AlertCircleIcon className="text-destructive" />
                        <strong>No data available</strong>
                    </div>
                ) : (
                    cardContent
                )}
            </CardContent>
        </Card>
    );
}
