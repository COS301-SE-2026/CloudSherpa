import { Card, CardHeader, CardTitle, CardContent, CardFooter } from "@/components/atoms/card";
import { Info, LucideIcon } from "lucide-react";
import { Separator } from "@/components/atoms/separator";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/atoms/tooltip";
import { MetricType } from "@/features/dashboard/types/metric";
import { UsageError } from "../../types/errors";

export const METRIC_UNITS: Record<MetricType, string> = {
    cpu: "%",
    memory: "%",
    "storage-used": "GB",
    "storage-available": "GB",
    "object-count": "objs",
    duration: "ms",
    throttles: "events",
    disk: "GB",
    network: "B",
    "read-capacity": "IOPS",
    "write-capacity": "IOPS",
    "first-byte-latency": "ms",
    latency: "ms",
    errors: "err",
    requests: "req",
    connections: "conn",
    invocations: "inv",
    anon: "",
};

export function getMetricUnit(metricType: MetricType | null): string {
    if (!metricType) return "%";
    return METRIC_UNITS[metricType] ?? "";
}

interface SummaryCardProps {
    title: string;
    // unit: string;
    pastUsage: number | null;
    predictedUsage: number | null;
    description: string;
    tooltip: string;
    Icon?: LucideIcon;
    usageError: UsageError | null;
}

export default function SummaryCard({
    title,
    // unit,
    pastUsage,
    predictedUsage,
    description,
    tooltip,
    Icon,
    usageError,
}: Readonly<SummaryCardProps>) {
    const cardContent = (
        <>
            {Icon && <Icon className="h-8 w-8 text-primary" />}
            <div className="flex flex-row gap-4 justify-start items-center">
                <span className="text-4xl">
                    {usageError?.item == "usage" || usageError?.item == "both" ? (
                        "—"
                    ) : (
                        <>{pastUsage?.toLocaleString()}</>
                    )}
                </span>
                <Separator orientation="vertical" />
                <span className="text-4xl">
                    {usageError?.item == "forecast" || usageError?.item == "both" ? (
                        "—"
                    ) : (
                        <>{predictedUsage?.toLocaleString()}</>
                    )}
                </span>
            </div>
        </>
    );

    if (pastUsage == null || predictedUsage == null) {
        return <Card className="h-45 border-2 border-dashed"></Card>;
    } else {
        return (
            <Card>
                <CardHeader>
                    <CardTitle className="flex flex-row justify-between">
                        {title}
                        <Tooltip>
                            <TooltipTrigger>
                                <Info className="h-5 w-5 text-muted-foreground" />
                            </TooltipTrigger>
                            <TooltipContent>{tooltip}</TooltipContent>
                        </Tooltip>
                    </CardTitle>
                </CardHeader>
                <CardContent className="flex flex-row gap-5     justify-start items-center">
                    {cardContent}
                </CardContent>
                <CardFooter className="text-muted-foreground">{description}</CardFooter>
            </Card>
        );
    }
}
