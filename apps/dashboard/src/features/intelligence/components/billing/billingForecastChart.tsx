"use client";

import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/atoms/tooltip";
import { Info } from "lucide-react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/atoms/card";
import { useMemo } from "react";
import ReactECharts from "echarts-for-react";
import type { EChartsOption } from "echarts";

export interface BillingForecastSlices {
    label: string;
    percent: number;
}

interface BillingForecastChartProps {
    name: string;
    data: BillingForecastSlices[];
}

export default function BillingForecastChart({
    name,
    data,
}: Readonly<BillingForecastChartProps>) {
    const option: EChartsOption = useMemo(
        () => ({
            tooltip: {
                trigger: "item",
                backgroundColor: "var(--background)",
                borderColor: "var(--border)",
                borderWidth: 1,
                textStyle: {
                    color: "var(--foreground)",
                },
                formatter: (params: unknown) => {
                    const p = params as { name: string; percent?: number };
                    return `<strong>${p.name || ""}</strong><br/>Percentage: ${p.percent ?? 0}%`;
                },
            },
            color: ["#3b82f6", "#8b5cf6", "#10b981", "#f59e0b", "#f43f5e"],
            series: [
                {
                    type: "pie",
                    radius: ["55%", "80%"],
                    avoidLabelOverlap: true,
                    itemStyle: {
                        borderColor: "var(--card)",
                        borderWidth: 2,
                    },
                    label: {
                        show: true,
                        color: "#e3e9ef",
                        fontSize: 11,
                        fontWeight: 400,
                        textShadowBlur: 0,
                        textShadowColor: "transparent",
                        formatter: (params: unknown) => {
                            const p = params as { name: string; percent?: number };
                            return `${p.name || ""}\n${p.percent ?? 0}%`;
                        },
                    },
                    labelLine: {
                        show: true,
                        lineStyle: {
                            color: "var(--border)",
                        },
                    },
                    emphasis: {
                        label: {
                            color: "var(--foreground)",
                            fontWeight: 500,
                        },
                        itemStyle: {
                            shadowBlur: 10,
                            shadowColor: "var(--primary)",
                        },
                    },
                    data: data.map((forData) => ({
                        name: forData.label,
                        value: forData.percent,
                    })),
                },
            ],
        }),
        [data]
    );

    return (
        <Card>
            <CardHeader>
                <CardTitle className="flex flex-row justify-between items-center text-sm font-normal text-muted-foreground">
                    {name}
                    <Tooltip>
                        <TooltipTrigger>
                            <Info className="h-4 w-4" strokeWidth={1.75} />
                        </TooltipTrigger>
                        <TooltipContent>...</TooltipContent>
                    </Tooltip>
                </CardTitle>
            </CardHeader>
            <CardContent>
                {data.length === 0 ? (
                    <div className="h-[320px] flex items-center justify-center text-muted-foreground text-sm">
                        No chart data available
                    </div>
                ) : (
                    <ReactECharts option={option} style={{ height: 320 }} notMerge />
                )}
            </CardContent>
        </Card>
    );
}