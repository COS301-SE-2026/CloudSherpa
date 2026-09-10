"use client";
import { Recommendation } from "@/features/optimization/types/recommendations";
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from "@/components/atoms/card";
import { useState } from "react";
import { Button } from "@/components/atoms/button";
import { useRecStore } from "@/features/optimization/stores/useRecStore";
import { Badge } from "@/components/atoms/badge";
import { Separator } from "@/components/atoms/separator";
import { toast } from "sonner";
import RecommendationReasoning from "@/features/optimization/utils/recDictionary";

interface RecommendationCardProps {
    recommendation: Recommendation;
}

export default function RecommendationCard({ recommendation }: Readonly<RecommendationCardProps>) {
    const dismissRec = useRecStore((state) => state.dismissRec);
    const applyRec = useRecStore((state) => state.applyRec);
    const reEnableRec = useRecStore((state) => state.reEnableRec);
    const focusedResourceId = useRecStore((state) => state.focusedResourceId);

    const [open, setOpen] = useState(recommendation.resourceId === focusedResourceId);

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

    const handleReEnable = async (e: React.MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();
        try {
            await reEnableRec(recommendation.recommendationId);
            toast.success(`Successfully re-enabled recommendation.`);
        } catch {
            toast.error(`Failed to re-enable recommendation.`);
            return;
        }
    };

    const getStatusBadgeClass = () => {
        switch (recommendation.status) {
            case "ACTIVE":
                return "bg-success text-white";
            case "APPLIED":
                return "bg-primary text-white";
            case "DISMISSED":
                return "bg-destructive text-white";
            default:
                return "variant-secondary";
        }
    };

    const getActionTextColor = () => {
        switch (recommendation.actionType) {
            case "TERMINATE":
                return "text-destructive";
            case "MODERNIZE":
                return "text-primary";
            case "DOWNSIZE":
                return "text-warning";
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

    const getPeriod = () => {
        if (!recommendation?.evidence) return "4";
        const keys = Object.keys(recommendation.evidence);
        if (keys.length === 0) return "4";

        const parsed = parseEvidenceKey(keys[0]);
        return parsed ? parsed.timeframe.replace("d", "") : "4";
    };

    const period = getPeriod();

    const getEvidenceCards = () => {
        if (!recommendation?.evidence) return [];

        const cards: Array<{
            label: string;
            value: string;
            subtitle: string;
        }> = [];

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

    const renderActionButtons = () => {
        if (recommendation.status === "DISMISSED") {
            return (
                <Button
                    type="button"
                    variant="outline"
                    onClick={handleReEnable}
                    className="cursor-pointer"
                >
                    Re-enable
                </Button>
            );
        }

        if (recommendation.status === "APPLIED") {
            return "";
        }

        return (
            <>
                <Button
                    type="button"
                    variant="destructive"
                    onClick={handleDismiss}
                    className="cursor-pointer"
                >
                    Dismiss
                </Button>
                <Button type="button" onClick={handleApply} className="cursor-pointer">
                    Apply
                </Button>
            </>
        );
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
                <div className="h-full w-full space-y-6">
                    <CardContent className="h-full flex flex-col lg:flex-row gap-6">
                        <div className="flex flex-col ">
                            <h3 className="text-sm font-semibold text-muted-foreground mb-2">
                                Monitored Evidence over {period} day
                                {period === "1" ? "" : "s"}
                            </h3>
                            <div className="flex flex-col w-70 gap-3">
                                {getEvidenceCards().map((card) => (
                                    <div key={card.label} className="flex flex-col gap-4">
                                        <div>
                                            <div className="text-2xl font-bold">{card.value}</div>
                                            <div className="text-sm font-semibold text-muted-foreground mb-1">
                                                {card.label}
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                        <Separator orientation="vertical" />
                        <div className="flex flex-col justify-between">
                            <p className="text-base text-foreground leading-relaxed">
                                <RecommendationReasoning recommendation={recommendation} />
                            </p>
                            <div className="flex flex-row justify-start gap-2">
                                {renderActionButtons()}
                            </div>
                        </div>
                    </CardContent>
                </div>
            )}
        </Card>
    );
}
