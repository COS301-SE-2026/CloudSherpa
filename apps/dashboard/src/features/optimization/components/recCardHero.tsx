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
                <Card className="w-full group">
                    <CardHeader>
                        <CardTitle
                            className={cn("text-xl flex flex-row justify-center", className)}
                        >
                            {name ?? name}
                            {value}
                        </CardTitle>
                    </CardHeader>
                </Card>
            </TooltipTrigger>
            {tooltip && <TooltipContent>{tooltip}</TooltipContent>}
        </Tooltip>
    );
}
