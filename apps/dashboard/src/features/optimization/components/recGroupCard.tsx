import { Card, CardHeader, CardTitle } from "@/components/atoms/card";
import { Badge } from "@/components/atoms/badge";
import RecDrawer from "@/features/optimization/components/recDrawer";
import { RecommendationGroup } from "@/features/optimization/types/recommendations";

interface RecommendationGroupCardProps {
    group: RecommendationGroup;
}

export default function RecommendationGroupCard({ group }: Readonly<RecommendationGroupCardProps>) {
    const estSumSavings = group.recommendations.reduce(
        (sum, rec) => sum + rec.estimated_monthly_savings,
        0
    );

    const recommendationsCount = group.recommendations.length;

    const provider = recommendationsCount > 0 ? group.recommendations[0].provider : "Unknown";

    const formattedSavings = new Intl.NumberFormat("en-US", {
        style: "currency",
        currency: "USD",
    }).format(estSumSavings);

    return (
        <Card>
            <CardHeader className="flex flex-row justify-between">
                <div className="h-full flex flex-row gap-4 items-center justify-center">
                    <CardTitle>{group.displayName}</CardTitle>
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
                    <RecDrawer group={group} />
                </div>
            </CardHeader>
        </Card>
    );
}
