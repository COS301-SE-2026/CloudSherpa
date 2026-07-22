"use client";

import { useState, useEffect } from "react";
import ReactECharts from "echarts-for-react";

import { useChartTheme } from "@/features/dashboard/hooks/useChartTheme";

export default function HeroTelemetry() {
    const { themeName, tokens } = useChartTheme();

    const [data, setData] = useState<number[]>(() =>
        Array.from({ length: 50 }, () => Math.floor(Math.random() * 20) + 10)
    );

    useEffect(() => {
        const prefersReducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
        if (prefersReducedMotion) return;

        const interval = setInterval(() => {
            setData((currentData) => {
                const newData = [...currentData.slice(1)];
                const lastValue = newData[newData.length - 1];
                const variance = Math.floor(Math.random() * 20) - 10;
                const nextValue = Math.max(10, Math.min(90, lastValue + variance));

                newData.push(nextValue);
                return newData;
            });
        }, 3000);

        return () => clearInterval(interval);
    }, []);

    const lineColor = tokens["primary"] || tokens["chart-1"] || "#2b7fff";

    const options = {
        grid: {
            top: 0,
            bottom: 0,
            left: -10,
            right: -10,
        },
        xAxis: {
            type: "category",
            show: false,
            boundaryGap: false,
        },
        yAxis: {
            type: "value",
            show: false,
            min: 0,
            max: 100,
        },
        series: [
            {
                data: data,
                type: "line",
                smooth: 0.4,
                showSymbol: false,
                lineStyle: {
                    color: lineColor,
                    width: 2,
                },
                areaStyle: {
                    color: lineColor,
                    opacity: 0.15,
                },
            },
        ],
        animationDurationUpdate: 3000,
        animationEasingUpdate: "linear",
    };

    return (
        <div className="absolute top-0 left-0 w-full h-100 -z-10 pointer-events-none mask-[linear-gradient(to_bottom,black_20%,transparent_100%)] opacity-80">
            <ReactECharts
                option={options}
                theme={themeName}
                style={{ height: "100%", width: "100%" }}
                notMerge={false}
            />
        </div>
    );
}
