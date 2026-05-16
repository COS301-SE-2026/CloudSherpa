"use client"
import { echarts } from "@/lib/charts/echarts";
import { useMetricStore } from "@/stores/metric-store";
import { Metric, MetricType } from "@/types/metric";
import type { EChartsOption } from "echarts";
import { useEffect, useRef } from "react";

type GaugeWidgetProps = {
    title: string,
    resourceId: string,
    metricType: MetricType,
}

const EMPTY_METRICS: Metric[] = [];

export function GaugeWidget({
    title,
    resourceId,
    metricType,

}: Readonly<GaugeWidgetProps>){
    const forChartReference = useRef<HTMLDivElement>(null);
    const forData = useMetricStore((state) => (state.seriesByKey[`${resourceId}:${metricType}`] ?? EMPTY_METRICS));
    const forChartInstance = useRef<echarts.ECharts | null>(null);

    const latestValue = forData.length > 0 ? forData[forData.length - 1].value : 0;

    useEffect(() => {
        if (!forChartReference.current) return;
        forChartInstance.current = echarts.init(forChartReference.current);

        return () => {
            forChartInstance.current?.dispose();
        };
    }, []);

    return(
        <div className="flex h-full flex-col items-center justify-center">
            <div className="w-full h-full min-h-[280px]">
                <div
                    ref={forChartReference}
                    className="h-full w-full"
                />
            </div>
        </div>
    );
}