import { Card, CardHeader, CardTitle } from "@/components/atoms/card";
import { Badge } from "@/components/atoms/badge";

interface ConnectionGroupProps {
    connection: string;
    provider: string;
    recommendationsCount: number;
    estSumSavings: number;
}

export default function ConnectionGroups({
    connection,
    provider,
    recommendationsCount,
    estSumSavings,
}: Readonly<ConnectionGroupProps>) {
    const formattedSavings = new Intl.NumberFormat("en-US", {
        style: "currency",
        currency: "USD",
    }).format(estSumSavings);
    return (
        <Card>
            <CardHeader className="flex flex-row justify-between">
                <div className="flex flex-row gap-4 items-center">
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
                    </div>{" "}
                    {/* drawer component that still needs to be made */}
                </div>
            </CardHeader>
        </Card>
    );
}
