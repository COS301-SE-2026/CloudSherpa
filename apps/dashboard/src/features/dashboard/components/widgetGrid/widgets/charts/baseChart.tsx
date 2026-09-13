"use client";

import dynamic from "next/dynamic";
import { useEffect, useRef } from "react";
import type { EChartsOption, ECharts } from "echarts";

const ReactECharts = dynamic(() => import("echarts-for-react"), {
    ssr: false,
    loading: () => <div className="h-full w-full animate-pulse bg-muted rounded-md" />,
});

export function BaseChart({ option, theme }: Readonly<{ option: EChartsOption; theme?: string }>) {
    const containerRef = useRef<HTMLDivElement>(null);
    const chartInstanceRef = useRef<ECharts | null>(null);

    useEffect(() => {
        const el = containerRef.current;
        if (!el) return;

        let rafId: number;
        const observer = new ResizeObserver(() => {
            cancelAnimationFrame(rafId);
            rafId = requestAnimationFrame(() => {
                chartInstanceRef.current?.resize();
            });
        });

        observer.observe(el);
        return () => {
            cancelAnimationFrame(rafId);
            observer.disconnect();
        };
    }, []);

    return (
        <div ref={containerRef} className="h-full w-full">
            <ReactECharts
                option={option}
                theme={theme}
                style={{ height: "100%", width: "100%", minHeight: "100px" }}
                notMerge={true}
                lazyUpdate={true}
                onChartReady={(instance) => {
                    chartInstanceRef.current = instance;
                }}
            />
        </div>
    );
}
