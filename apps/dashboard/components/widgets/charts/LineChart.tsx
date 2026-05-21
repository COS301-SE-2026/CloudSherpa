"use client"

import { echarts } from "@/lib/charts/echarts";
import { useMetricStore } from "@/stores/metric-store";
import { useWindowStore } from "@/stores/window-store";
import { metricSeriesToArray, MetricType } from "@/types/metric";
import type { EChartsOption } from "echarts";
import { useEffect, useMemo, useRef, useState } from "react";

type LineChartWidgetProps = {
    title: string,
    resourceId?: string,
    metricType?: MetricType,
}

// This will be replaced by zustand store for dashboard window

// The tick should sync with the ingestion intervals, still need to do system-wide
// investigation regarding this
const AXIS_TICK_MS = 5_000;

export function LineChartWidget({
    title,
    resourceId,
    metricType,
}: Readonly<LineChartWidgetProps>) {
    const chartRef = useRef<HTMLDivElement>(null);
    const series = useMetricStore((state) => state.seriesByKey[`${resourceId}:${metricType}`]);
    const data = useMemo(() => metricSeriesToArray(series), [series]);
    const fromMs = useWindowStore((state) => state.fromMs);
    const toMs = useWindowStore((state) => state.toMs);
    const visibleWindowMs = toMs - fromMs;

    const chartInstance = useRef<echarts.ECharts | null>(null);
    const [lineColor, setLineColor] = useState<string>('#3b82f6'); // fallback blue
    const [gridOpacity, setGridOpacity] = useState<number>(0.15);

    // Extract color token and listen for theme changes
    //note: echarts does not have built in support for css tokens and stuff so we have to use workaround 
    useEffect(() => {
        const updateThemeStyles = () => {
            const style = getComputedStyle(document.documentElement);
            const tokenColor = style.getPropertyValue('--primary').trim();
            
            if (tokenColor) {
                setLineColor(tokenColor);
            }

            // Adjust horizontal grid line opacity based on active theme(just added on logic for theme swapping)
            const isLightMode = document.documentElement.getAttribute('data-theme') === 'light';
            setGridOpacity(isLightMode ? 0.70 : 0.10);
        };

        updateThemeStyles();
        const observer = new MutationObserver(updateThemeStyles);
        observer.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme', 'class'] });
        return () => observer.disconnect();
    }, []);

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
            color: [lineColor],
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
                min: axisMax - visibleWindowMs,
                max: axisMax,
                interval: AXIS_TICK_MS,
                  axisLabel: {
    hideOverlap: true
  }
            },

            yAxis: {
                type: "value",
                axisLabel: {
                    formatter: '{value} %'
                },
                splitLine: {
                    lineStyle: {
                        opacity: gridOpacity // Dynamic opacity based on theme
                    }
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
    }, [title, resourceId, data, visibleWindowMs, lineColor, gridOpacity])

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
