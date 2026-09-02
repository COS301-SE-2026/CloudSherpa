import { Card, CardHeader, CardTitle, CardContent } from "@/components/atoms/card";

interface KpiConfigSummaryProps {
    readonly numResources: number;
    readonly aggregationWindowDays: number;
}

export function KpiConfigSummary({ numResources, aggregationWindowDays }: KpiConfigSummaryProps) {
    return (
        <Card className="bg-muted/40">
            <CardHeader className="flex flex-row justify-start">
                <CardTitle className="text-lg font-bold">Configuration Summary</CardTitle>
            </CardHeader>
            <CardContent className="grid grid-cols-3">
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
            </CardContent>
        </Card>
    );
}
