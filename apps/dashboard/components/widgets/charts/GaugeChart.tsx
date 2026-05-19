"use client"
import { echarts } from "@/lib/charts/echarts";
import { useMetricStore } from "@/stores/metric-store";
import { Metric, MetricType } from "@/types/metric";
import type { EChartsOption } from "echarts";
import { useEffect, useRef, useState } from "react";

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

    const latestValue = forData.length > 0 ? Math.round(forData[forData.length - 1].value) : 0;

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
        if (!forChartReference.current) return;
        forChartInstance.current = echarts.init(forChartReference.current);
        
        const handleResize = () => {
            forChartInstance.current?.resize();
        };

        const handleWidgetResize = () => {
            setTimeout(() => {
                forChartInstance.current?.resize();
            }, 10);
        };

        window.addEventListener('resize', handleResize);
        window.addEventListener('widget-resize', handleWidgetResize);
        
        const resizeObserver = new ResizeObserver(() => {
            forChartInstance.current?.resize();
        });

        resizeObserver.observe(forChartReference.current);

        return () => {
            window.removeEventListener('resize', handleResize);
            window.removeEventListener('widget-resize', handleWidgetResize);
            resizeObserver.disconnect();
            forChartInstance.current?.dispose();
        };
    }, []);

    useEffect(() => {
        if(!forChartInstance.current){
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
                    name: title,
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
                        color: 'rgb(148, 163, 184)',
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

                    title: {
                        show: false,
                        offsetCenter: [0, -35],
                        fontSize: 13,
                        color: 'rgb(148, 163, 184)',
                        fontFamily: 'sans-serif',
                    },
                    
                    data: [{ value: latestValue, name: title }]
                }
            ]
        };

        forChartInstance.current.setOption(option);
    }, [title, latestValue, textColor, primaryColor, trackColor]);

    useEffect(() => {
        if(!forChartInstance.current){
            return;
        }

        const handleResize = () => {
            forChartInstance.current?.resize();
        };

        const handleWidgetResize = () => {
            setTimeout(() => {
                forChartInstance.current?.resize();
            }, 10);
        };

        window.addEventListener('resize', handleResize);
        window.addEventListener('widget-resize', handleWidgetResize);
        
        const resizeObserver = new ResizeObserver(() => {
            forChartInstance.current?.resize();
        });

        if(forChartReference.current){
            resizeObserver.observe(forChartReference.current);
        }

        return () => {
            window.removeEventListener('resize', handleResize);
            window.removeEventListener('widget-resize', handleWidgetResize);
            resizeObserver.disconnect();
        };
    }, []);

    return(
        <div ref={forChartReference} className="h-full w-full" />
    );
}