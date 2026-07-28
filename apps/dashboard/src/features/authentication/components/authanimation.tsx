"use client";

import React, { useState, useEffect } from "react";
import ReactECharts from "echarts-for-react";
import { cn } from "@/lib/utils";
import { useChartTheme } from "@/features/dashboard/hooks/useChartTheme";

const getNextChartValue = (lastValue: number): number => {
    const variance = Math.floor(Math.random() * 30) - 15; // NOSONAR

    return Math.max(20, Math.min(80, lastValue + variance));
};

const generateHistoricalData = (length: number) => {
    const historicalData: number[] = [20];
    for (let i = 1; i < length; i++) {
        const lastValue = historicalData[i - 1];
        historicalData.push(getNextChartValue(lastValue));
    }
    return historicalData;
};

interface AuthChartProps {
    data: number[];
    className?: string;
}

function AuthChart({ data, className }: Readonly<AuthChartProps>) {
    const { themeName, tokens } = useChartTheme();
    const lineColor = tokens["primary"] || tokens["chart-1"] || "#2b7fff";

    const options = {
        grid: {
            top: 0,
            bottom: 0,
            left: -20,
            right: -20,
        },
        xAxis: {
            type: "category",
            show: false,
            boundaryGap: false,
            axisLine: { show: false },
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
                smooth: false,
                lineStyle: { color: lineColor, width: 3 },
            },
        ],
        animationDurationUpdate: 3000,
        animationEasingUpdate: "linear",
    };

    return (
        <div className={cn("absolute w-full h-1/2", className)}>
            <ReactECharts
                option={options}
                theme={themeName}
                style={{ height: "100%", width: "100%" }}
                notMerge={false}
            />
        </div>
    );
}

export default function AuthAnimation() {
    const [data, setData] = useState<number[]>(() => generateHistoricalData(30));

    useEffect(() => {
        const prefersReducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
        if (prefersReducedMotion) return;

        const interval = setInterval(() => {
            setData((currentData) => {
                const lastValue = currentData.at(-1) ?? 20;

                const nextValue = getNextChartValue(lastValue);

                return [...currentData.slice(1), nextValue];
            });
        }, 3000);

        return () => clearInterval(interval);
    }, []);

    return (
        <div className="absolute top-1/2 left-0 w-full h-[60%] -translate-y-1/2 pointer-events-none mask-[linear-gradient(to_right,transparent_0%,black_30%,black_70%,transparent_100%)] opacity-50">
            <AuthChart data={data} className="top-0" />
            <AuthChart data={data} className="bottom-0 scale-y-[-1] scale-x-[-1]" />
        </div>
    );
}
