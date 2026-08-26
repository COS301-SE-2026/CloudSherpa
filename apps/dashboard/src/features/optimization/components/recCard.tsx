"use client";
import { Recommendation } from "@/features/optimization/types/recommendations";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/atoms/card";
import { useState } from "react";
import { getRecommendationDictionary } from "@/features/optimization/utils/recommendationDictionary";
import { Button } from "@/components/atoms/button";
import RecommendationCardHero from "@/features/optimization/components/recCardHero";
import { useRecStore } from "@/features/optimization/stores/useRecStore";
import { Badge } from "@/components/atoms/badge";
import { toast } from "sonner";
import { formatEvidenceText } from "@/features/optimization/utils/formatEvidence";

interface RecommendationCardProps {
    recommendation: Recommendation;
}

export default function RecommendationCard({ recommendation }: Readonly<RecommendationCardProps>) {
    const [open, setOpen] = useState(false);

    const evidence = Object.entries(recommendation.evidence || {}).find(
        ([key]) => key !== "completenessRatio"
    );

    const evidenceText = evidence ? formatEvidenceText(evidence[0], evidence[1]) : "N/A";

    const acknowledgeRec = useRecStore((state) => state.acknowledgeRec);
    const dismissRec = useRecStore((state) => state.dismissRec);
    const applyRec = useRecStore((state) => state.applyRec);

    const handleAcknowledge = async (e: React.MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();
        try {
            await acknowledgeRec(recommendation.recommendationId);
            toast.success(`Successfully acknowledged recommendation.`);
        } catch {
            toast.error(`Failed to acknowledge recommendation.`);
            return;
        }
    };

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

    const confidence =
        recommendation.evidence?.completenessRatio !== undefined
            ? `${(Number(recommendation.evidence.completenessRatio) * 100).toFixed(0)}%`
            : "N/A";

    return (
        <Card onClick={() => setOpen(!open)}>
            <CardHeader className="flex flex-row justify-between">
                <CardTitle>
                    {recommendation.resourceDisplayName ?? recommendation.resourceId}
                </CardTitle>
                <div className="flex flex-col md:flex-row gap-2">
                    <Badge>{recommendation.actionType}</Badge>
                    <Badge variant="secondary">{recommendation.status}</Badge>
                </div>
            </CardHeader>
            {open && (
                <CardContent className="space-y-4">
                    {/* hero section */}
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                        <RecommendationCardHero
                            value={recommendation.actionType}
                            className="text-chart-1"
                        />
                        <RecommendationCardHero
                            value={evidenceText}
                            className="text-chart-4 truncate"
                        />
                    </div>

                    {/* explanation */}
                    {getRecommendationDictionary(recommendation)}

                    <div className="flex flex-row gap-4 justify-end items-center">
                        <Button type="button" onClick={handleApply}>
                            Apply
                        </Button>
                        <Button type="button" variant={"secondary"} onClick={handleAcknowledge}>
                            Acknowledge
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
