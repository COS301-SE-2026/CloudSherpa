import { Card, CardFooter, CardHeader, CardTitle } from "@/components/atoms/card";
import { Badge } from "@/components/atoms/badge";
import RecDrawer from "@/features/optimization/components/recDrawer";
import { RecommendationGroup } from "@/features/optimization/types/recommendations";
import { Separator } from "@/components/atoms/separator";

interface RecommendationGroupCardProps {
    group: RecommendationGroup;
}

export default function RecommendationGroupCard({ group }: Readonly<RecommendationGroupCardProps>) {
    const recommendationsCount = group.recommendations.length;

    const provider = recommendationsCount > 0 ? group.recommendations[0].provider : "Unknown";

    return (
        <Card className="h-50 flex flex-col justify-between">
            <CardHeader>
                <div className="flex flex-col gap-2">
                    <CardTitle>{group.displayName}</CardTitle>
                    <div className="flex flex-row gap-2">
                        <Badge>{provider}</Badge>
                    </div>
                </div>
            </CardHeader>
            <CardFooter className="w-full flex flex-col justify-start gap-2">
                <Separator />
                <div className="flex flex-row justify-between items-center w-full">
                    <span className="text-muted-foreground text-xs">
                        {recommendationsCount} recommendations
                    </span>
                    <RecDrawer group={group} />
                </div>
            </CardFooter>
        </Card>
    );
}
