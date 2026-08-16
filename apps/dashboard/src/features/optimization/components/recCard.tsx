"use client";
import { Recommendation } from "@/features/optimization/types/recommendations";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/atoms/card";
import { useState } from "react";
import { getRecommendationDictionary } from "@/features/optimization/utils/recommendationDictionary";
import { Button } from "@/components/atoms/button";
import RecommendationCardHero from "@/features/optimization/components/recCardHero";
import { useRecStore } from "@/features/optimization/stores/useRecStore";

interface RecommendationCardProps {
    recommendation: Recommendation;
}

export default function RecommendationCard({ recommendation }: Readonly<RecommendationCardProps>) {
    const [open, setOpen] = useState(false);

    const handleAcknowledge = (e: React.MouseEvent) => {
        e.preventDefault();
        console.log("Acknowledged!");
    };

    const handleDismiss = (e: React.MouseEvent) => {
        e.preventDefault();
        console.log("Dismissed!");
    };

    return (
        <Card onClick={() => setOpen(!open)}>
            <CardHeader>
                <CardTitle>
                    {recommendation.resourceDisplayName ?? recommendation.resourceId}
                </CardTitle>
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
                        <Button variant={"secondary"} onClick={() => handleAcknowledge}>
                            Acknowledge
                        </Button>
                        <Button variant={"destructive"} onClick={() => handleDismiss}>
                            Dismiss
                        </Button>
                    </div>
                </CardContent>
            )}
        </Card>
    );
}
