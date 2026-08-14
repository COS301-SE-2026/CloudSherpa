import { Card, CardHeader, CardTitle } from "@/components/atoms/card";

interface RecommendationCardHero {
    name?: string;
    value: string | number;
}

export default function RecommendationCardHero({ name, value }: Readonly<RecommendationCardHero>) {
    return (
        <Card>
            <CardHeader>
                <CardTitle>
                    {name ?? name}
                    {value}
                </CardTitle>
            </CardHeader>
        </Card>
    );
}
