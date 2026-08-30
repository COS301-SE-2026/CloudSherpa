"use client";
import { useEffect, useState, useMemo } from "react";
import RecommendationGroupCard from "@/features/optimization/components/recGroupCard";
import { useRecStore } from "@/features/optimization/stores/useRecStore";
import { Input } from "@/components/atoms/input";
import { Search, ArrowUpDown } from "lucide-react";
import { Tabs, TabsTrigger, TabsList } from "@/components/atoms/tabs";
import RecommendationCardHero from "@/features/optimization/components/recCardHero";
import { Button } from "@/components/atoms/button";
import { RecommendationErrorAlert } from "@/features/optimization/components/recError";
import { Spinner } from "@/components/atoms/spinner";

const FilterOptions = [
    { value: "all", label: "ALL" },
    { value: "aws", label: "AWS" },
    { value: "gcp", label: "GCP" },
    { value: "azure", label: "AZURE" },
];

export default function Recommendations() {
    const fetchRecGroups = useRecStore((state) => state.fetchRecGroups);
    const recommendationGroups = useRecStore((state) => state.recommendationGroups);
    const fetchSummary = useRecStore((state) => state.fetchSummary);
    const summary = useRecStore((state) => state.summary);
    const isLoading = useRecStore((state) => state.isLoading);
    const recErrorState = useRecStore((state) => state.failedLoading);
    const recErrorMessage = useRecStore((state) => state.failedLoadingMessage);

    const [searchQuery, setSearchQuery] = useState("");
    const [filter, setFilter] = useState("all");
    const [sortOrder, setSortOrder] = useState<"desc" | "asc">("desc");

    const filteredRecommendationGroups = useMemo(() => {
        const filtered = recommendationGroups.filter((group) => {
            const matchesProvider =
                filter !== "all"
                    ? group.recommendations.some(
                          (rec) => rec.provider.toLowerCase() === filter.toLowerCase()
                      )
                    : true;

            const query = searchQuery.trim().toLowerCase();

            const matchesSearch = searchQuery.trim()
                ? (group.displayName ?? "").toLowerCase().includes(query) ||
                  group.recommendations.some(
                      (rec) =>
                          (rec.resourceId ?? "").toLowerCase().includes(query) ||
                          (rec.resourceDisplayName ?? "").toLowerCase().includes(query)
                  )
                : true;

            return matchesProvider && matchesSearch;
        });

        return filtered.sort((a, b) => {
            const aCount = a.recommendations.length;
            const bCount = b.recommendations.length;

            if (sortOrder === "desc") {
                return bCount - aCount;
            } else {
                return aCount - bCount;
            }
        });
    }, [searchQuery, filter, recommendationGroups, sortOrder]);

    useEffect(() => {
        fetchRecGroups();
        fetchSummary();
    }, [fetchRecGroups, fetchSummary]);

    if (isLoading) {
        return (
            <div className="flex flex-col h-full w-full p-6 gap-4">
                <header className="flex flex-col space-y-4 py-2">
                    <h1 className="text-3xl font-semibold">Optimization Recommendations</h1>
                </header>
                <div className="h-full w-full flex flex-col justify-center items-center">
                    <Spinner className="h-10 w-10" />
                </div>
            </div>
        );
    } else {
        return (
            <div className="flex flex-col h-full w-full p-6 gap-4">
                <header className="flex flex-col space-y-4 py-2">
                    <h1 className="text-3xl font-semibold">Optimization Recommendations</h1>
                </header>
                {/* recommendation summaries */}
                {recErrorState && <RecommendationErrorAlert recError={recErrorMessage} />}
                {summary && (
                    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4 mb-2">
                        <RecommendationCardHero
                            value={`Total ${summary.total}`}
                            className="text-card-foreground"
                        />
                        <RecommendationCardHero
                            value={`Active ${summary.active}`}
                            className="text-success"
                        />
                        <RecommendationCardHero
                            value={`Applied ${summary.applied}`}
                            className="text-primary"
                        />
                        <RecommendationCardHero
                            value={`Dismissed ${summary.dismissed}`}
                            className="text-destructive"
                        />
                    </div>
                )}
                {/* filter bar */}
                <div className="flex flex-row w-full justify-between gap-2">
                    <Tabs value={filter || undefined} onValueChange={(value) => setFilter(value)}>
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
                    <div className="flex flex-row justify-end items-center gap-2">
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
                        {/* sort highest to lowest */}
                        <Button
                            variant="secondary"
                            onClick={() =>
                                setSortOrder((prev) => (prev === "desc" ? "asc" : "desc"))
                            }
                        >
                            <ArrowUpDown />
                        </Button>
                    </div>
                </div>

                {filteredRecommendationGroups.length > 0 ? (
                    <div className="flex flex-col gap-2">
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
}
