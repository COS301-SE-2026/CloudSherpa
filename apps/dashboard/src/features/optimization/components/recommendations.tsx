"use client";
import { useEffect, useState, useMemo } from "react";
import RecommendationGroupCard from "@/features/optimization/components/recGroupCard";
import { useRecStore } from "@/features/optimization/stores/useRecStore";
import { Input } from "@/components/atoms/input";
import { Search } from "lucide-react";
import { Tabs, TabsTrigger, TabsList } from "@/components/atoms/tabs";

const FilterOptions = [
    { value: "all", label: "ALL" },
    { value: "aws", label: "AWS" },
    { value: "gcp", label: "GCP" },
    { value: "azure", label: "AZURE" },
];

export default function Recommendations() {
    const fetchRecGroups = useRecStore((state) => state.fetchRecGroups);
    const recommendationGroups = useRecStore((state) => state.recommendationGroups);
    const isLoading = useRecStore((state) => state.isLoading);

    const [searchQuery, setSearchQuery] = useState("");
    const [filter, setFilter] = useState("all");

    const filteredRecommendationGroups = useMemo(() => {
        return recommendationGroups.filter((group) => {
            const matchesProvider =
                filter !== "all"
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
            <header className="flex flex-col space-y-4 py-2">
                <h1 className="text-3xl font-semibold">Optimization Recommendations</h1>
            </header>
            {/* filter bar */}
            <div className="flex flex-row w-full justify-between gap-2">
                <Tabs
                    value={filter || undefined}
                    onValueChange={(value) => setFilter(value)}
                    className="mb-4"
                >
                    <TabsList className="self-start inline-flex gap-1 h-auto p-1 bg-muted w-fit">
                        {FilterOptions.map((providers) => {
                            return (
                                <TabsTrigger
                                    key={providers.value}
                                    value={providers.value}
                                    className={` font-medium transition-all bg-transparent`}
                                >
                                    {providers.label}
                                </TabsTrigger>
                            );
                        })}
                    </TabsList>
                </Tabs>
                <div className="relative">
                    <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                    <Input
                        type="text"
                        placeholder="Search by Account..."
                        className="pl-8  w-full lg:w-150"
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                    />
                </div>
            </div>
            {filteredRecommendationGroups.length > 0 ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4">
                    {filteredRecommendationGroups.map((group) => (
                        <RecommendationGroupCard
                            key={group.accountId ?? "unassigned"}
                            group={group}
                        />
                    ))}
                </div>
            ) : (
                <div className="text-muted-foreground h-full w-full flex flex-col justify-center items-center">
                    No recommendations found.
                </div>
            )}
        </div>
    );
}
