import { cn } from "@/lib/utils";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/atoms/tooltip";
import { Info, type LucideIcon } from "lucide-react";
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from "@/components/atoms/card";

interface BillingStatisticsCardProps {
    name: string;
    value: string;
    description: string;
    icon?: LucideIcon;
    valueClassName?: string;
    tooltip?: string;
}

export default function BillingStatisticsCard({
    name,
    value,
    description,
    icon: Icon,
    valueClassName,
    tooltip = "...",
}: Readonly<BillingStatisticsCardProps>) {
    return (
        <Card>
            <CardHeader>
                <CardTitle className="flex flex-row justify-between items-center text-sm font-normal text-muted-foreground">
                    {" "}
                    {name}
                    <Tooltip>
                        <TooltipTrigger>
                            {" "}
                            <Info className="h-4 w-4" strokeWidth={1.75} />{" "}
                        </TooltipTrigger>

                        <TooltipContent> {tooltip} </TooltipContent>
                    </Tooltip>
                </CardTitle>
            </CardHeader>

            <CardContent className="flex flex-row items-center gap-2">
                {Icon && <Icon className={cn("h-5 w-5", valueClassName)} strokeWidth={1.75} />}

                <span
                    className={cn("text-2xl font-semibold tracking-tight truncate", valueClassName)}
                >
                    {" "}
                    {value}{" "}
                </span>
            </CardContent>

            <CardFooter className="text-muted-foreground text-sm"> {description} </CardFooter>
        </Card>
    );
}
