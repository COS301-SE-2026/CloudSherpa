"use client";
import { Recommendation } from "@/features/optimization/types/recommendations";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/atoms/card";
import { useState } from "react";
import { getRecommendationDictionary } from "@/features/optimization/utils/recommendationDictionary";
import { Button } from "@/components/atoms/button";
import { useRecStore } from "@/features/optimization/stores/useRecStore";
import { Badge } from "@/components/atoms/badge";
import { toast } from "sonner";

interface RecommendationCardProps {
    recommendation: Recommendation;
}

export default function RecommendationCard({ recommendation }: Readonly<RecommendationCardProps>) {
    const [open, setOpen] = useState(false);

    const dismissRec = useRecStore((state) => state.dismissRec);
    const applyRec = useRecStore((state) => state.applyRec);

    const handleDismiss = async (e: React.MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();
        try {
            await dismissRec(recommendation.recommendationId);
            toast.success(`Successfully dismissed recommendation.`);
        } catch {
            toast.error(`Failed to dismiss recommendation.`);
            return;
        }
    };

    const handleApply = async (e: React.MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();
        try {
            await applyRec(recommendation.recommendationId);
            toast.success(`Successfully flagged recommendation as applied.`);
        } catch {
            toast.error(`Failed to flag recommendation as applied.`);
            return;
        }
    };

    const getStatusBadgeClass = () => {
        switch (recommendation.status) {
            case "ACTIVE":
                return "bg-green-600 text-white";
            case "APPLIED":
                return "bg-blue-600 text-white";
            case "DISMISSED":
                return "bg-red-600 text-white";
            default:
                return "variant-secondary";
        }
    };

    const getActionTextColor = () => {
        switch (recommendation.actionType) {
            case "TERMINATE":
                return "text-red-600";
            case "MODERNIZE":
                return "text-blue-600";
            case "DOWNSIZE":
                return "text-orange-600";
            case "SUSPEND":
                return "text-yellow-600";
            default:
                return "";
        }
    };

    const parseEvidenceKey = (key: string) => {
        const parts = key.split("_");

        if (parts.length < 3) return null;

        const timeframe = parts.pop()!;
        const aggregation = parts.pop()!;

        const metricName = parts.join(" ");

        if (!timeframe.endsWith("d")) return null;

        return { metricName, aggregation, timeframe };
    };

    const isPercentageMetric = (metricName: string): boolean => {
        const nameLower = metricName.toLowerCase();
        return (
            nameLower.includes("utilization") ||
            nameLower.includes("percentage") ||
            nameLower.includes("pressure")
        );
    };

    const getEvidenceCards = () => {
        if (!recommendation?.evidence) return [];

        const cards: Array<{ label: string; value: string; subtitle: string }> = [];

        for (const [key, value] of Object.entries(recommendation.evidence)) {
            const parsed = parseEvidenceKey(key);
            if (!parsed) continue;

            const { metricName, aggregation, timeframe } = parsed;

            const formattedValue = typeof value === "number" ? value.toFixed(2) : String(value);
            const displayValue = isPercentageMetric(metricName)
                ? `${formattedValue}%`
                : formattedValue;

            const aggDisplay = aggregation.charAt(0).toUpperCase() + aggregation.slice(1);
            const label = `${metricName} (${aggDisplay})`;

            const days = timeframe.replace("d", "");
            const subtitle = `Over the last ${days} day${days === "1" ? "" : "s"}`;

            cards.push({ label, value: displayValue, subtitle });
        }

        return cards;
    };

    return (
        <Card onClick={() => setOpen(!open)} className="cursor-pointer">
            <CardHeader className="flex flex-row justify-between items-center gap-2">
                <div className="flex flex-row items-center gap-2">
                    <span className={`font-bold text-lg ${getActionTextColor()}`}>
                        {recommendation.actionType}
                    </span>
                    <CardTitle className="text-lg">
                        {recommendation.resourceDisplayName ?? recommendation.resourceId}
                    </CardTitle>
                </div>
                <Badge className={`text-sm px-3 py-1 ${getStatusBadgeClass()}`}>
                    {recommendation.status}
                </Badge>
            </CardHeader>
            {open && (
                <CardContent className="space-y-4">
                    <div>
                        <h3 className="text-sm font-semibold text-muted-foreground mb-3">
                            Monitored Evidence
                        </h3>
                        <div className="grid grid-cols-1 lg:grid-cols-2 gap-3">
                            {getEvidenceCards().map((card, index) => (
                                <div
                                    key={index}
                                    className="bg-muted/80 dark:bg-muted/60 rounded-lg p-4"
                                >
                                    <p className="text-sm font-bold text-foreground mb-1">
                                        {card.label}
                                    </p>
                                    <p className="text-2xl font-bold mb-1">{card.value}</p>
                                    <p className="text-xs text-muted-foreground">{card.subtitle}</p>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* explanation */}
                    {getRecommendationDictionary(recommendation)}

                    <div className="flex flex-row gap-4 justify-end items-center">
                        <Button type="button" onClick={handleApply}>
                            Apply
                        </Button>
                        <Button type="button" variant={"destructive"} onClick={handleDismiss}>
                            Dismiss
                        </Button>
                    </div>
                </CardContent>
            )}
        </Card>
    );
}
