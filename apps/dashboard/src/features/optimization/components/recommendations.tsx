"use client";
import { useEffect, useState, useMemo } from "react";
import RecommendationGroupCard from "@/features/optimization/components/recGroupCard";
import { useRecStore } from "@/features/optimization/stores/useRecStore";
import { Input } from "@/components/atoms/input";
import Dropdown from "@/components/molecules/dropdown";

const FilterOptions = [
    { value: "aws", label: "AWS" },
    { value: "gcp", label: "GCP" },
    { value: "azure", label: "AZURE" },
];

export default function Recommendations() {
    const fetchRecGroups = useRecStore((state) => state.fetchRecGroups);
    const recommendationGroups = useRecStore((state) => state.recommendationGroups);
    const isLoading = useRecStore((state) => state.isLoading);

    const [searchQuery, setSearchQuery] = useState("");
    const [filter, setFilter] = useState("");

    const filteredRecommendationGroups = useMemo(() => {
        return recommendationGroups.filter((group) => {
            const matchesProvider = filter
                ? group.recommendations.some(
                      (rec) => rec.provider.toLowerCase() === filter.toLowerCase()
                  )
                : true;

            const matchesSearch = searchQuery.trim()
                ? (group.displayName ?? "").toLowerCase().includes(searchQuery.toLowerCase())
                : true;

            return matchesProvider && matchesSearch;
        });
    }, [searchQuery, filter, recommendationGroups]);

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
            <header className="flex flex-col space-y-4">
                <h1 className="text-3xl font-semibold">Optimization Recommendations</h1>

                {/* filter bar */}
                <div className="flex flex-row w-full justify-end gap-2">
                    <Input
                        type="text"
                        placeholder="Search by Account..."
                        className="pl-8  w-full lg:w-1/3"
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                    />
                    <Dropdown
                        options={FilterOptions}
                        value={filter}
                        disableSearch
                        onSelect={setFilter}
                        widthVariant="medium"
                        placeholder="select resource.."
                    />
                </div>
            </header>
            {filteredRecommendationGroups.length > 0 ? (
                filteredRecommendationGroups.map((group) => (
                    <RecommendationGroupCard key={group.accountId ?? "unassigned"} group={group} />
                ))
            ) : (
                <div className="text-muted-foreground">No recommendations found.</div>
            )}
        </div>
    );
}
