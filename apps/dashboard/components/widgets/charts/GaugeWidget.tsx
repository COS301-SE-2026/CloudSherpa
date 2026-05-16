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

    useEffect(() => {
        if(!forChartInstance.current){
            return;
        }

        const option: EChartsOption = {
            tooltip: {
                formatter: '{b}: {c}%',
                backgroundColor: 'var(--color-bg-main)',
                borderColor: 'var(--color-action-primary)',
                borderWidth: 1,
                textStyle: {
                    color: 'var(--color-text-primary)',
                    fontSize: 12,
                    fontFamily: 'var(--font-family-main)',
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
                            color: 'var(--color-action-primary)',
                        }
                    },

                    axisLine: {
                        roundCap: true,
                        lineStyle: {
                            width: 15,
                            color: [[1, 'rgba(47, 47, 228, 0.2)']]
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
                            color: 'var(--color-text-muted)',
                        }
                    },

                    axisLabel: {
                        show: true,
                        distance: 18,
                        color: 'var(--color-text-muted)',
                        fontSize: 11,
                        fontFamily: 'var(--font-family-main)',
                        formatter: (value: number) => `${value}%`
                    },

                    pointer: {
                        show: true,
                        length: '60%',
                        width: 8,
                        itemStyle: {
                            color: 'var(--color-action-accent)',
                        }
                    },
                    
                    detail: {
                        show: true,
                        offsetCenter: [0, 30],
                        valueAnimation: true,
                        fontSize: 24,
                        fontWeight: 'bold',
                        color: 'var(--color-text-primary)',
                        fontFamily: 'var(--font-family-main)',
                        formatter: '{value}%'
                    },

                    title: {
                        show: true,
                        offsetCenter: [0, -35],
                        fontSize: 13,
                        color: 'var(--color-text-muted)',
                        fontFamily: 'var(--font-family-main)',
                    },
                    
                    data: [{ value: latestValue, name: title }]
                }
            ]
        };

        forChartInstance.current.setOption(option);
    }, [title, latestValue]);

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