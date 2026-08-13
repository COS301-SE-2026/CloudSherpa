"use client";
import { useEffect, useState, useMemo } from "react";
import RecommendationGroupCard from "@/features/optimization/components/recGroupCard";
import { useRecStore } from "@/features/optimization/stores/useRecStore";
import { Input } from "@/components/atoms/input";

export default function Recommendations() {
    const fetchRecGroups = useRecStore((state) => state.fetchRecGroups);
    const recommendationGroups = useRecStore((state) => state.recommendationGroups);
    const isLoading = useRecStore((state) => state.isLoading);

    const [searchQuery, setSearchQuery] = useState("");

    const filteredRecommendationGroups = useMemo(() => {
        if (!searchQuery.trim()) return recommendationGroups;

        const lowerCaseQuery = searchQuery.toLowerCase();

        return recommendationGroups.filter((rec) => {
            const searchableName = rec.displayName ?? "";
            return searchableName.toLowerCase().includes(lowerCaseQuery);
        });
    }, [searchQuery, recommendationGroups]);

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
                <Input
                    type="text"
                    placeholder="Search Connecition"
                    className="pl-8"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                />
            </header>
            {filteredRecommendationGroups.length > 0 ? (
                filteredRecommendationGroups.map((group) => (
                    <RecommendationGroupCard key={group.accountId ?? "unassigned"} group={group} />
                ))
            ) : (
                <div className="text-muted-foreground">No recommendations found.</div>
            )}{" "}
        </div>
    );
}
