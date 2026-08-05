import { Card, CardHeader, CardTitle, CardContent, CardFooter } from "@/components/atoms/card";
import { Info } from "lucide-react";

interface SummaryCardProps {
    title: string;
    unit: string;
    pastUsage: number;
    predictedUsage: number;
    description: string;
}

export default function SummaryCard({
    title,
    unit,
    pastUsage,
    predictedUsage,
    description,
}: Readonly<SummaryCardProps>) {
    return (
        <Card>
            <CardHeader>
                <CardTitle>
                    {title}
                    <Info className="h-4 w-4" />
                </CardTitle>
            </CardHeader>
            <CardContent>
                <span>
                    {pastUsage}
                    {unit}
                </span>
                <span>
                    {predictedUsage}
                    {unit}
                </span>
            </CardContent>
            <CardFooter>{description}</CardFooter>
        </Card>
    );
}
