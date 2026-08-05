import { Card, CardHeader, CardTitle, CardContent, CardFooter } from "@/components/atoms/card";
import { Info } from "lucide-react";
import { Separator } from "@/components/atoms/separator";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/atoms/tooltip";

interface SummaryCardProps {
    title: string;
    unit: string;
    pastUsage: number;
    predictedUsage: number;
    description: string;
}

export default function SummaryCard({
    title,
    unit,
    pastUsage,
    predictedUsage,
    description,
}: Readonly<SummaryCardProps>) {
    return (
        <Card>
            <CardHeader>
                <CardTitle className="flex flex-row justify-between">
                    {title}
                    <Tooltip>
                        <TooltipTrigger>
                            <Info className="h-4 w-4" />
                        </TooltipTrigger>
                        <TooltipContent>some stuff</TooltipContent>
                    </Tooltip>
                </CardTitle>
            </CardHeader>
            <CardContent className="flex flex-row gap-2 justify-start">
                <span className="text-3xl">
                    {pastUsage}
                    {unit}
                </span>
                <Separator orientation="vertical" />
                <span className="text-3xl">
                    {predictedUsage}
                    {unit}
                </span>
            </CardContent>
            <CardFooter className="text-muted-foreground">{description}</CardFooter>
        </Card>
    );
}
