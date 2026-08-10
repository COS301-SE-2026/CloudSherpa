"use client";

import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/atoms/tooltip";
import {Info} from "lucide-react";
import {Card, CardHeader, CardTitle, CardContent} from "@/components/atoms/card";
import {useMemo} from "react";
import ReactECharts from "echarts-for-react";
import type {EChartsOption} from "echarts";

export interface BillingForecastSlices{
    label : string;
    percent : number;
}

interface BillingForecastChartProps{
    name : string;
    data : BillingForecastSlices[];
}

export default function BillingForecastChart({
    name, data,
} : Readonly<BillingForecastChartProps>){

    const option : EChartsOption = useMemo(() => ({
        tooltip : {trigger : "item"},
        color : ["var(--chart-1)",
                "var(--chart-2)",
                "var(--chart-3)",
                "var(--chart-4)",
                "var(--chart-5)",],
        series : [{
            type : "pie", radius : ["55%", "80%"], avoidLabelOverlap : true,
            itemStyle : {borderColor : "var(--card)", borderWidth : 2,},
            label : {show : true, color : "var(--muted-foreground)", fontSize : 11, fontWeight : 400, textShadowBlur : 0, textShadowColor : "transparent"},
            labelLine : {show : true, lineStyle : {color : "var(--border)"},},
            data : data.map((forData) => ({name : forData.label, value : forData.percent})),
        },],
    }), [data]);

    return(
        <Card>
            <CardHeader>
                <CardTitle className = "flex flex-row justify-between items-center text-sm font-normal text-muted-foreground"> {name}

                    <Tooltip>
                        <TooltipTrigger> <Info className = "h-4 w-4" strokeWidth = {1.75}/> </TooltipTrigger>

                        <TooltipContent> ... </TooltipContent>
                    </Tooltip>

                </CardTitle>
            </CardHeader>

            <CardContent>
                {data.length === 0 ? (
                    <div className = "h-[320px] flex items-center justify-center text-muted-foreground text-sm"> No chart data available </div>
                ) : (
                    <ReactECharts option = {option} style = {{height : 320}} notMerge/>
                )}
            </CardContent>

        </Card>
    );
}