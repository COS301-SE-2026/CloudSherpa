"use client"

import { echarts } from "@/lib/charts/echarts";
import { useMetricStore } from "@/stores/metric-store";
import { Metric, MetricType } from "@/types/metric";
import type { EChartsOption } from "echarts";
import { useEffect, useRef } from "react";

type LineChartWidgetProps = {
    title: string,
    resourceId: string,
    metricType: MetricType,
}

const EMPTY_METRICS: Metric[] = [];
// This will be replaced by zustand store for dashboard window
const VISIBLE_WINDOW_MS = 60_000;
// The tick should sync with the ingestion intervals, still need to do system-wide
// investigation regarding this
const AXIS_TICK_MS = 5_000;

export function LineChartWidget({
    title,
    resourceId,
    metricType,
}: Readonly<LineChartWidgetProps>) {
    const chartRef = useRef<HTMLDivElement>(null);
    const data = useMetricStore((state) => (state.seriesByKey[`${resourceId}:${metricType}`] ?? EMPTY_METRICS));
    const chartInstance = useRef<echarts.ECharts | null>(null);

    useEffect(() => {
        if (!chartRef.current) return;
        chartInstance.current = echarts.init(chartRef.current);

        return () => {
            chartInstance.current?.dispose();
        }
    }, []);

    useEffect(() => {
        if (!chartRef.current) return;

        // Convert to unix timestamp in ms, should consider doing this during normalization
        const points = data
            .map((metric): [number, number] => [new Date(metric.timestamp).getTime(), metric.value]);

        // Suppose value of 121 with interval of 50s, 121 / 50 = 2.43, ceil takes that to 3, multiplied with 50 gives
        // you nice boundary of 150
        const axisMax = Math.ceil(Date.now() / AXIS_TICK_MS) * AXIS_TICK_MS;

        const option: EChartsOption = {
            tooltip: {
                trigger: "axis"
            },

            // animate on render, do not animate on update
            animationDuration: 600,
            animationDurationUpdate: 0,
            
            // controls the spacing around the actual plotting area.
            // containLabel makes ECharts reserve space so axis labels are not clipped.
            grid: {
                left: 20,
                right: 20,
                top: 40,
                bottom: 20,

                containLabel: true,
            },

            // setting to time, series data has to be [number, number], with [timestamp, value]
            xAxis: {
                type: "time",
                min: axisMax - VISIBLE_WINDOW_MS,
                max: axisMax,
                interval: AXIS_TICK_MS,
            },

            yAxis: {
                type: "value",
                axisLabel: {
                    formatter: '{value} %'
                }
            },

            series: [
                {
                    name: title,
                    type: "line",
                    data: points,
                    // symbol = visual marker
                    showSymbol: false,
                },
                    
            ],
        };

        chartInstance.current?.setOption(option);
    }, [title, resourceId, data])

    useEffect(() => {
        if(!chartInstance.current){
            return;
        }

        //this will recalc its size
        const handleResize = () => {
            chartInstance.current?.resize();
        };

        const handleWidgetResize = () => {
            setTimeout(() => {
                chartInstance.current?.resize();
            }, 10);
        };

        window.addEventListener('resize', handleResize);
        window.addEventListener('widget-resize', handleWidgetResize);

        const forResizing = new ResizeObserver(() => {
            chartInstance.current?.resize();
        });

        if(chartRef.current){
            forResizing.observe(chartRef.current);
        }
        
        return () => {
            window.removeEventListener('resize', handleResize);
            window.removeEventListener('widget-resize', handleWidgetResize);
            forResizing.disconnect();
        };
    }, []);

    return (
        <div ref={chartRef} className="h-full w-full" />
    );
}
