import { Card, CardHeader, CardTitle } from "@/components/atoms/card";
import { Badge } from "@/components/atoms/badge";
import RecDrawer from "@/features/optimization/components/recDrawer";
import { RecommendationGroup } from "@/features/optimization/types/recommendations";

interface RecommendationGroupCardProps {
    group: RecommendationGroup;
}

export default function RecommendationGroupCard({ group }: Readonly<RecommendationGroupCardProps>) {
    const recommendationsCount = group.recommendations.length;

    const provider = recommendationsCount > 0 ? group.recommendations[0].provider : "Unknown";

    return (
        <Card>
            <CardHeader className="flex flex-row justify-between">
                <div className="h-full flex flex-row gap-4 items-center justify-center">
                    <CardTitle>{group.displayName}</CardTitle>
                    <Badge>{provider}</Badge>
                    <Badge variant={"secondary"}>{recommendationsCount} recommendations</Badge>
                </div>
                <RecDrawer group={group} />
            </CardHeader>
        </Card>
    );
}
