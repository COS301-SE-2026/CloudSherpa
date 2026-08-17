import { Card, CardHeader, CardTitle } from "@/components/atoms/card";
import { cn } from "@/lib/utils";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/atoms/tooltip";

interface RecommendationCardHero {
    name?: string;
    value: string | number;
    className?: string;
    tooltip?: string;
}

export default function RecommendationCardHero({
    name,
    value,
    className,
    tooltip,
}: Readonly<RecommendationCardHero>) {
    return (
        <Tooltip>
            <TooltipTrigger>
                <Card>
                    <CardHeader>
                        <CardTitle className={cn("text-xl flex flex-row justify-start", className)}>
                            {name ?? name}
                            {value}
                        </CardTitle>
                    </CardHeader>
                </Card>
            </TooltipTrigger>
            <TooltipContent>{tooltip}</TooltipContent>
        </Tooltip>
    );
}
