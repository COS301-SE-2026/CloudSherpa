import { Card } from "@/components/atoms/card";

interface KpiConfigSummaryProps {
    readonly connections: string[];
    readonly numResources: number;
    readonly aggregationWindowDays: number;
}

export function KpiConfigSummary({
    connections,
    numResources,
    aggregationWindowDays,
}: KpiConfigSummaryProps) {
    const connectionSet = new Set(connections);
    const connectionCount = connectionSet.size;

    return (
        <Card className="flex flex-col p-6 bg-muted/40">
            <h1 className="text-lg font-bold">Configuration Summary</h1>
            <div className="grid grid-cols-2">
                <div className="flex flex-col gap-6">
                    <p className="text-muted-foreground">Connections</p>
                    <p className="text-muted-foreground">Resources</p>
                    <p className="text-muted-foreground">Time Period</p>
                    <p className="text-muted-foreground">Aggregation</p>
                </div>
                <div className="flex flex-col gap-6">
                    <p className="font-semibold">Over {connectionCount} Connections</p>
                    <p className="font-semibold">{numResources} selected</p>
                    <p className="font-semibold">{aggregationWindowDays} days</p>
                    <p className="font-semibold">Total cost (sum)</p>
                </div>
            </div>
        </Card>
    );
}
