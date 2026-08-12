import { Card, CardHeader, CardTitle } from "@/components/atoms/card";
import { Badge } from "@/components/atoms/badge";
import RecDrawer from "@/features/optimization/components/recDrawer";
import { Recommendation } from "@/features/optimization/types/recommendations";

interface ConnectionGroupProps {
    connection: string;
    recommendations: Recommendation[];
}

export default function RecommendationGroup({
    connection,
    recommendations,
}: Readonly<ConnectionGroupProps>) {
    //these operations are temporarily part of component.
    const estSumSavings = recommendations.reduce(
        (sum, rec) => sum + rec.estimated_monthly_savings,
        0
    );

    const recommendationsCount = recommendations.length;

    const provider = recommendations.length > 0 ? recommendations[0].provider : "Unknown";

    const formattedSavings = new Intl.NumberFormat("en-US", {
        style: "currency",
        currency: "USD",
    }).format(estSumSavings);

    return (
        <Card>
            <CardHeader className="flex flex-row justify-between">
                <div className="h-full flex flex-row gap-4 items-center justify-center">
                    <CardTitle>{connection}</CardTitle>
                    <Badge>{provider}</Badge>
                    <Badge variant={"secondary"}>{recommendationsCount} recommendations</Badge>
                </div>
                <div className="flex flex-row gap-4">
                    <div className="flex flex-row items-center gap-2 text-right">
                        <span className="text-sm text-muted-foreground">Est. Savings</span>
                        <span className="text-lg font-bold text-green-600">
                            {formattedSavings}/mo
                        </span>
                    </div>
                    <RecDrawer connection={connection} recommendations={recommendations} />
                </div>
            </CardHeader>
        </Card>
    );
}
