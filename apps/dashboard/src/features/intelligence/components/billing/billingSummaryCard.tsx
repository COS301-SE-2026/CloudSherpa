import {cn} from "@/lib/utils";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/atoms/tooltip";
import {Info} from "lucide-react";
import {Card, CardHeader, CardTitle, CardContent, CardFooter} from "@/components/atoms/card";

interface BillingSummaryCardProps{
    name : string;
    value : string;
    description : string;
    valueClassName?: string;
    tooltip?: string;
}

export default function BillingSummaryCard({
    name, value, description, valueClassName, tooltip = "...",
} : Readonly<BillingSummaryCardProps>){
    return(
        <Card>
            <CardHeader>
                <CardTitle className = "flex flex-row justify-between items-center text-sm font-normal text-muted-foreground"> {name}

                    <Tooltip>
                        <TooltipTrigger> <Info className = "h-4 w-4" strokeWidth = {1.75}/> </TooltipTrigger>

                        <TooltipContent> {tooltip} </TooltipContent>
                    </Tooltip>
                    
                </CardTitle>
            </CardHeader>

            <CardContent>
                <span className = {cn("text-4xl font-semibold tracking-tight", valueClassName)}> {value} </span>
            </CardContent>

            <CardFooter className = "text-muted-foreground text-sm"> {description} </CardFooter>
            
        </Card>
    );
}