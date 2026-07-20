import { Card } from "@/components/atoms/card";

interface KpiConfigSummaryProps {
    readonly numResources: number;
    readonly aggregationWindowDays: number;
}

export function KpiConfigSummary({ numResources, aggregationWindowDays }: KpiConfigSummaryProps) {
    return (
        <Card className="flex flex-col p-6 bg-muted/40">
            <h1 className="text-lg font-bold">Configuration Summary</h1>
            <div className="grid grid-cols-2">
                <div className="flex flex-col gap-6">
                    <p className="text-muted-foreground">Resources</p>
                    <p className="text-muted-foreground">Time Period</p>
                    <p className="text-muted-foreground">Aggregation</p>
                </div>
                <div className="flex flex-col gap-6">
                    <p className="font-semibold">{numResources} selected</p>
                    <p className="font-semibold">{aggregationWindowDays} days</p>
                    <p className="font-semibold">Total cost (sum)</p>
                </div>
            </div>
        </Card>
    );
}
