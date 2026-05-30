"use client"
import { Spinner } from "@/components/atoms/spinner";
import { echarts } from "@/lib/charts/echarts";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { metricSeriesToArray, MetricType } from "@/features/dashboard/types/metric";
import type { EChartsOption } from "echarts";
import { useEffect, useMemo, useRef, useState } from "react";

type GaugeChartProps = {
    resourceId?: string,
    metricType?: MetricType,
    metricFetchLoad?: boolean,
}

export function GaugeChart({
    resourceId,
    metricType,
    metricFetchLoad = false,

}: Readonly<GaugeChartProps>){
    const chartRef = useRef<HTMLDivElement>(null);
    const series = useMetricStore((state) => state.seriesByKey[`${resourceId}:${metricType}`]);
    const data = useMemo(() => metricSeriesToArray(series), [series]);
    const chartInstance = useRef<echarts.ECharts | null>(null);

    const latestValue = data.length > 0 ? Math.round(data[data.length - 1].value) : 0;

    const [textColor, setTextColor] = useState<string>('rgb(255, 255, 255)');
    const [primaryColor, setPrimaryColor] = useState<string>('rgb(59, 130, 246)');
    const [trackColor, setTrackColor] = useState<string>('rgb(30, 41, 59)');

    // Extract color tokens and listen for theme changes
    useEffect(() => {
        const updateThemeStyles = () => {
            const style = getComputedStyle(document.documentElement);
            const foregroundToken = style.getPropertyValue('--foreground').trim();
            const primaryToken = style.getPropertyValue('--primary').trim();
            const mutedToken = style.getPropertyValue('--muted').trim();
            
            const isLightMode = document.documentElement.getAttribute('data-theme') === 'light';
            
            if (foregroundToken) setTextColor(foregroundToken);
            else setTextColor(isLightMode ? '#020617' : 'rgb(255, 255, 255)');
            
            if (primaryToken) setPrimaryColor(primaryToken);
            
            if (mutedToken) setTrackColor(mutedToken);
            else setTrackColor(isLightMode ? '#e2e8f0' : 'rgb(30, 41, 59)');
        };

        updateThemeStyles();
        const observer = new MutationObserver(updateThemeStyles);
        observer.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme', 'class'] });
        return () => observer.disconnect();
    }, []);

    useEffect(() => {
        if (!chartRef.current) return;
        chartInstance.current = echarts.init(chartRef.current);

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

        const resizeObserver = new ResizeObserver(() => {
            chartInstance.current?.resize();
        });

        resizeObserver.observe(chartRef.current);

        return () => {
            window.removeEventListener('resize', handleResize);
            window.removeEventListener('widget-resize', handleWidgetResize);
            resizeObserver.disconnect();
            chartInstance.current?.dispose();
        };
    }, []);

    useEffect(() => {
        if(!chartInstance.current){
            return;
        }

        const option: EChartsOption = {
            tooltip: {
                formatter: '{b}: {c}%',
                backgroundColor: trackColor,
                borderColor: trackColor,
                borderWidth: 1,
                textStyle: {
                    color: textColor,
                    fontSize: 12,
                    fontFamily: 'sans-serif',
                },
            },
            series: [
                {
                    type: 'gauge',
                    startAngle: 180,
                    endAngle: 0,
                    min: 0,
                    max: 100,
                    splitNumber: 4,
                    radius: '100%',
                    center: ['50%', '60%'],
                    progress: {
                        show: true,
                        width: 15,
                        roundCap: true,
                        itemStyle: {
                            color: primaryColor,
                        }
                    },

                    axisLine: {
                        roundCap: true,
                        lineStyle: {
                            width: 15,
                            color: [[1, trackColor]]
                        }
                    },

                    axisTick: {
                        show: false
                    },

                    splitLine: {
                        show: true,
                        length: 8,
                        lineStyle: {
                            width: 2,
                            color: 'rgb(148, 163, 184)',
                        }
                    },

                    axisLabel: {
                        show: true,
                        distance: 18,
                        color: textColor,
                        fontSize: 11,
                        fontFamily: 'sans-serif',
                        formatter: (value: number) => `${value}%`
                    },

                    pointer: {
                        show: true,
                        length: '60%',
                        width: 8,
                        itemStyle: {
                            color: 'rgb(249, 115, 22)',
                        }
                    },
                    
                    detail: {
                        show: true,
                        offsetCenter: [0, 30],
                        valueAnimation: true,
                        fontSize: 24,
                        fontWeight: 'bold',
                        color: textColor,
                        fontFamily: 'sans-serif',
                        formatter: (value: number) => `${Math.round(value)}%`
                    },
                    
                    data: [{ value: latestValue }]
                }
            ]
        };

        chartInstance.current.setOption(option);
    }, [latestValue, textColor, primaryColor, trackColor]);

    useEffect(() => {
        if(!chartInstance.current){
            return;
        }

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

        const resizeObserver = new ResizeObserver(() => {
            chartInstance.current?.resize();
        });

        if(chartRef.current){
            resizeObserver.observe(chartRef.current);
        }

        return () => {
            window.removeEventListener('resize', handleResize);
            window.removeEventListener('widget-resize', handleWidgetResize);
            resizeObserver.disconnect();
        };
    }, []);

    return(
        <div className="relative h-full w-full">
            <div ref={chartRef} className="h-full w-full" />
            {metricFetchLoad && (
                <div className="absolute inset-0 z-10 flex items-center justify-center bg-card/60 backdrop-blur-[1px]">
                    <Spinner className="size-8" />
                </div>
            )}
        </div>
    );
}
