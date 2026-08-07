"use client";

import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/atoms/tooltip";
import {Info} from "lucide-react";
import {Card, CardHeader, CardTitle, CardContent} from "@/components/atoms/card";

export interface BillingForecastSlices{
    label : string;
    value : number;
}

interface BillingForecastChartProps{
    name : string;
    data : BillingForecastSlices[];
}

export default function BillingForecastChart({
    name, data,
} : Readonly<BillingForecastChartProps>){
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
                <div className = "h-[320px] flex items-center justify-center text-muted-foreground text-sm"> No chart data available </div>
            </CardContent>

        </Card>
    );
}