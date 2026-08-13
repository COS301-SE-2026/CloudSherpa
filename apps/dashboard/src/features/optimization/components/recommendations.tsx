"use client";
import { useEffect } from "react";
import RecommendationGroupCard from "@/features/optimization/components/recGroupCard";
import { useRecStore } from "@/features/optimization/stores/useRecStore";

export default function Recommendations() {
    const fetchRecGroups = useRecStore((state) => state.fetchRecGroups);
    const recommendationGroups = useRecStore((state) => state.recommendationGroups);
    const isLoading = useRecStore((state) => state.isLoading);

    useEffect(() => {
        fetchRecGroups();
    }, [fetchRecGroups]);

    if (isLoading) {
        return (
            <div className="flex items-center justify-center h-full w-full p-6 text-muted-foreground">
                Loading optimization recommendations...
            </div>
        );
    }

    return (
        <div className="flex flex-col h-full w-full p-6 gap-4">
            <header>
                <h1 className="text-3xl font-semibold">Optimization Recommendations</h1>
            </header>
            {recommendationGroups.length > 0 ? (
                recommendationGroups.map((group) => (
                    <RecommendationGroupCard key={group.accountId ?? "unassigned"} group={group} />
                ))
            ) : (
                <div className="text-muted-foreground">No recommendations found.</div>
            )}{" "}
        </div>
    );
}
