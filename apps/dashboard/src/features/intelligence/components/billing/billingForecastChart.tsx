"use client";

import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/atoms/tooltip";
import { Info} from "lucide-react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/atoms/card";
import { useMemo } from "react";
import ReactECharts from "echarts-for-react";
import type { EChartsOption } from "echarts";
import { useChartTheme } from "@/features/dashboard/hooks/useChartTheme";

export interface BillingForecastSlices {
    label: string;
    percent: number;
}

interface BillingForecastChartProps {
    name: string;
    data: BillingForecastSlices[];
    threshold?: number;
}

const LABEL_FOR_OTHER = "Other";

export default function BillingForecastChart({
    name,
    data,
    threshold = 10,
}: Readonly<BillingForecastChartProps>) {
    const { themeName, tokens } = useChartTheme();

    const CHART_COLOUR_SCALE = [
        tokens["primary-950"],
        tokens["primary-900"],
        tokens["primary-800"],
        tokens["primary-700"],
        tokens["primary-600"],
        tokens["primary-500"],
        tokens["primary-400"],
        tokens["primary-300"],
        tokens["primary-200"],
        tokens["primary-100"],
    ];

    const groupingData = useMemo(() => {
        const aboveTenPercent = data.filter((slice) => slice.percent >= threshold);

        const belowTenPercent = data.filter((slice) => slice.percent < threshold);

        const totalForOther = belowTenPercent.reduce((total, slice) => total + slice.percent, 0);

        const result = [...aboveTenPercent];

        if (totalForOther > 0) {
            result.push({ label: LABEL_FOR_OTHER, percent: Math.round(totalForOther * 100) / 100 });
        }

        return result;
    }, [data, threshold]);

    const option: EChartsOption = useMemo(
        () => ({
            tooltip: {
                trigger: "item",
                backgroundColor: tokens["card"],
                borderColor: tokens["border"],
                borderWidth: 1,
                textStyle: {
                    color: tokens["foreground"],
                },
                formatter: (params: unknown) => {
                    const forParameter = params as { name: string; dataIndex: number };

                    const percentage = groupingData[forParameter.dataIndex]?.percent ?? 0;

                    return `<strong>${forParameter.name || ""}</strong><br/>Percentage: ${percentage}%`;
                },
            },

            color: groupingData.map(
                (_, index) => CHART_COLOUR_SCALE[index % CHART_COLOUR_SCALE.length]
            ),

            series: [
                {
                    type: "pie",
                    radius: ["55%", "80%"],
                    avoidLabelOverlap: true,
                    itemStyle: {
                        borderColor: tokens["card"],
                        borderWidth: 2,
                    },
                    label: {
                        show: true,
                        color: tokens["foreground"],
                        fontSize: 11,
                        fontWeight: 400,
                        textShadowBlur: 0,
                        textShadowColor: "transparent",
                        formatter: (params: unknown) => {
                            const forParameter = params as { name: string; dataIndex: number };

                            const percentage = groupingData[forParameter.dataIndex]?.percent ?? 0;

                            return `${forParameter.name || ""}\n${percentage}%`;
                        },
                    },
                    labelLine: {
                        show: true,
                        lineStyle: {
                            color: tokens["border"],
                        },
                    },
                    emphasis: {
                        label: {
                            color: tokens["foreground"],
                            fontWeight: 500,
                        },
                        itemStyle: {
                            shadowBlur: 10,
                            shadowColor: tokens["primary"],
                        },
                    },
                    data: groupingData.map((forData) => ({
                        name: forData.label,
                        value: forData.percent,
                    })),
                },
            ],
        }),
        [groupingData, tokens]
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
                    <ReactECharts
                        option={option}
                        style={{ height: 320 }}
                        notMerge
                        theme={themeName}
                    />
                )}
            </CardContent>
        </Card>
    );
}
