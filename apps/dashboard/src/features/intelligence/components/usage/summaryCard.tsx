import { Card, CardHeader, CardTitle, CardContent, CardFooter } from "@/components/atoms/card";
import { Info, LucideIcon } from "lucide-react";
import { Separator } from "@/components/atoms/separator";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/atoms/tooltip";
import { MetricType } from "@/features/dashboard/types/metric";
import { UsageError } from "../../types/errors";
import { useUsageIntelligenceConfigStore } from "@/features/intelligence/stores/useUsageIntelligenceConfigStore";

export const METRIC_UNITS: Record<string, string> = {
    // metricMapper
    "CPU Utilization": "%",
    "Memory Utilization": "%",
    "CPU Reservation": "%",
    "Memory Reservation": "%",
    "JVM Memory Pressure": "%",
    "Percentage Disk Space Used": "%",
    "DTU Consumption": "%",
    "Container CPU Utilizations": "%",
    "CPU Percent": "%",

    "Network In": "B",
    "Network Out": "B",
    "Disk Read Bytes": "B",
    "Disk Write Bytes": "B",
    "Free Storage Space": "GB",
    "Network Bytes In": "B",
    "Network Bytes Out": "B",
    "Memory used": "B",
    "Memory usage": "B",
    "Stored bytes": "B",
    "Bytes uploaded": "B",
    "Bytes downloaded": "B",
    "Allocated Storage": "GB",
    Ingress: "B",
    Egress: "B",
    "Received Bytes Count": "B",
    "Send Bytes Count": "B",
    "OS Disk Write Bytes": "B",
    "OS Disk Read Bytes": "B",

    Duration: "ms",
    "Read Latency": "ms",
    "Write Latency": "ms",
    "Search Latency": "ms",
    "CPU usage time": "s",
    "Function execution time": "ms",
    "Request latency": "ms",

    "Read IOPS": "IOPS",
    "Write IOPS": "IOPS",

    Throttles: "events",
    Invocations: "inv",
    Errors: "err",
    "Cluster Failed Request Count": "req",
    "Cluster Request Total": "req",
    "HTTP requests": "req",
    "API requests": "req",
    "HTTP Success 2xx": "req",
    "HTTP Error 5xx": "err",

    "Database Connections": "conn",
    "Current Connections": "conn",

    "Cluster Node Count": "nodes",
    "Concurrent Executions": "exec",
    "Freeable Memory": "GB",
    Evictions: "events",
    "Cluster Index Writes Blocked": "events",
    "Health Status": "status",
    "Health Check Failed": "checks",
    "Reserved CPU cores": "cores",
    "Pod restart count": "restarts",
    "Function executions": "exec",
    "Active instances": "instances",
    "Running instances": "instances",
    "Blob Count": "blobs",
    Deadlocks: "count",
};

export function getMetricUnit(metricType: string | null): string {
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
    const { metricName } = useUsageIntelligenceConfigStore();

    const unit = (
        <span className="text-4xl">
            {usageError?.item == "forecast" || usageError?.item == "both" ? (
                "—"
            ) : (
                <>{getMetricUnit(metricName)}</>
            )}
        </span>
    );

    const cardContent = (
        <>
            {Icon && <Icon className="h-8 w-8 text-primary" />}
            <div className="flex flex-row gap-4 justify-start items-center">
                <span className="text-4xl">
                    {usageError?.item == "usage" || usageError?.item == "both" ? (
                        "—"
                    ) : (
                        <>
                            {pastUsage?.toLocaleString()} {unit}
                        </>
                    )}
                </span>
                <Separator orientation="vertical" />
                <span className="text-4xl">
                    {usageError?.item == "forecast" || usageError?.item == "both" ? (
                        "—"
                    ) : (
                        <>
                            {predictedUsage?.toLocaleString()} {unit}
                        </>
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
                <CardContent className="flex flex-row gap-5 justify-start items-center">
                    {cardContent}
                </CardContent>
                <CardFooter className="text-muted-foreground">{description}</CardFooter>
            </Card>
        );
    }
}
