"use client";
import { Recommendation } from "@/features/optimization/types/recommendations";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/atoms/card";
import { useState } from "react";
import { getRecommendationDictionary } from "@/features/optimization/utils/recommendationDictionary";
import { Button } from "@/components/atoms/button";
import RecommendationCardHero from "@/features/optimization/components/recCardHero";
import { useRecStore } from "@/features/optimization/stores/useRecStore";
import { Badge } from "@/components/atoms/badge";

interface RecommendationCardProps {
    recommendation: Recommendation;
}

export default function RecommendationCard({ recommendation }: Readonly<RecommendationCardProps>) {
    const [open, setOpen] = useState(false);

    const acknowledgeRec = useRecStore((state) => state.acknowledgeRec);
    const dismissRec = useRecStore((state) => state.dismissRec);
    const applyRec = useRecStore((state) => state.applyRec);

    const handleAcknowledge = async (e: React.MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();
        await acknowledgeRec(recommendation.recommendationId);
    };

    const handleDismiss = async (e: React.MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();
        await dismissRec(recommendation.recommendationId);
    };

    const handleApply = async (e: React.MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();
        await applyRec(recommendation.recommendationId);
    };

    return (
        <Card onClick={() => setOpen(!open)}>
            <CardHeader className="flex flex-row justify-between">
                <CardTitle>
                    {recommendation.resourceDisplayName ?? recommendation.resourceId}
                </CardTitle>
                <div className="flex flex-row gap-2">
                    <Badge>{recommendation.actionType}</Badge>
                    <Badge variant="secondary">{recommendation.status}</Badge>
                </div>
            </CardHeader>
            {open && (
                <CardContent className="space-y-4">
                    {/* hero section */}
                    <div className="grid grid-cols-2 gap-4">
                        <RecommendationCardHero
                            value={recommendation.actionType}
                            className="text-chart-1"
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
