import { Card, CardHeader, CardTitle } from "@/components/atoms/card";
import { cn } from "@/lib/utils";

interface RecommendationCardHero {
    name?: string;
    value: string | number;
    className?: string;
}

export default function RecommendationCardHero({
    name,
    value,
    className,
}: Readonly<RecommendationCardHero>) {
    return (
        <Card>
            <CardHeader>
                <CardTitle className={cn("text-xl", className)}>
                    {name ?? name}
                    {value}
                </CardTitle>
            </CardHeader>
        </Card>
    );
}
