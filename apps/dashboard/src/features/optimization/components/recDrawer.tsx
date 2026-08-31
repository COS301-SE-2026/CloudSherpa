"use client";
import {
    Drawer,
    DrawerContent,
    DrawerTitle,
    DrawerHeader,
    DrawerDescription,
} from "@/components/atoms/drawer";
import { Button } from "@/components/atoms/button";
import { RecommendationGroup } from "@/features/optimization/types/recommendations";
import RecommendationCard from "@/features/optimization/components/recCard";
import { useState, useMemo, useEffect } from "react";
import { X, Search } from "lucide-react";
import { Input } from "@/components/atoms/input";
import { Tabs, TabsList, TabsTrigger } from "@/components/atoms/tabs";
import { useRecStore } from "@/features/optimization/stores/useRecStore";

interface RecDrawer {
    group: RecommendationGroup;
    isOpen: boolean;
    setIsOpen: (isOpen: boolean) => void;
}

export default function RecDrawer({ group, isOpen, setIsOpen }: Readonly<RecDrawer>) {
    const [statusTab, setStatusTab] = useState<"active" | "applied" | "dismissed">("active");
    const focusedResourceId = useRecStore((state) => state.focusedResourceId);

    const [searchQuery, setSearchQuery] = useState(() => {
        if (focusedResourceId) {
            const targetRec = group.recommendations.find((r) => r.resourceId === focusedResourceId);
            if (targetRec) {
                return targetRec.resourceDisplayName ?? targetRec.resourceId;
            }
        }
        return "";
    });

    const fetchRecGroups = useRecStore((state) => state.fetchRecGroups);

    useEffect(() => {
        if (!isOpen) return;

        const interval = setInterval(() => {
            fetchRecGroups();
        }, 10800000); // every 3 hours

        return () => clearInterval(interval);
    }, [isOpen, fetchRecGroups]);

    const filteredRecommendations = useMemo(() => {
        let filtered = group.recommendations.filter((rec) => {
            if (statusTab === "dismissed") {
                return rec.status === "DISMISSED";
            } else if (statusTab === "applied") {
                return rec.status === "APPLIED";
            } else {
                return rec.status === "ACTIVE";
            }
        });

        if (searchQuery.trim()) {
            const lowerCaseQuery = searchQuery.toLowerCase();
            filtered = filtered.filter((rec) => {
                const searchableName = rec.resourceDisplayName ?? "";

                return (
                    searchableName.toLowerCase().includes(lowerCaseQuery) ||
                    rec.actionType.toLowerCase().includes(lowerCaseQuery)
                );
            });
        }

        return filtered;
    }, [searchQuery, group.recommendations, statusTab]);

    const activeCount = group.recommendations.filter((rec) => rec.status === "ACTIVE").length;
    const appliedCount = group.recommendations.filter((rec) => rec.status === "APPLIED").length;
    const dismissedCount = group.recommendations.filter((rec) => rec.status === "DISMISSED").length;

    const clearFocusedRecommendation = useRecStore((state) => state.clearFocusedRecommendation);

    useEffect(() => {
        if (!isOpen) {
            clearFocusedRecommendation();
        }
    }, [isOpen, clearFocusedRecommendation]);

    return (
        <Drawer
            direction="right"
            dismissible={true}
            open={isOpen}
            onOpenChange={(open) => {
                setIsOpen(open);
                if (!open) clearFocusedRecommendation();
            }}
        >
            {/* drawer width could be volatile so will keep an eye on it */}
            <DrawerContent className="w-[90vw]! sm:w-[80vw]! lg:w-[60vw]! sm:max-w-[1600px]! p-4">
                <DrawerHeader className="flex flex-col">
                    <div className="flex flex-row lex-row justify-between items-center">
                        <DrawerTitle className="text-xl">{group.displayName}</DrawerTitle>
                        {/* closes drawer */}
                        <Button
                            type="button"
                            variant="ghost"
                            onClick={(e) => {
                                e.preventDefault();
                                e.stopPropagation();
                                setIsOpen(false);
                            }}
                        >
                            <X />
                        </Button>
                    </div>
                    <DrawerDescription>
                        View the recommendations and decide on the action to take
                    </DrawerDescription>
                </DrawerHeader>

                <div className="h-full w-full p-4 overflow-y-auto space-y-4">
                    {/* filters */}
                    <div className="flex flex-row w-full justify-between items-center gap-4 pt-2">
                        <Tabs
                            value={statusTab}
                            onValueChange={(value) =>
                                setStatusTab(value as "active" | "applied" | "dismissed")
                            }
                        >
                            <TabsList className="h-auto p-1">
                                <TabsTrigger value="active" className="text-sm">
                                    Active ({activeCount})
                                </TabsTrigger>
                                <TabsTrigger value="applied" className="text-sm">
                                    Applied ({appliedCount})
                                </TabsTrigger>
                                <TabsTrigger value="dismissed" className="text-sm">
                                    Dismissed ({dismissedCount})
                                </TabsTrigger>
                            </TabsList>
                        </Tabs>
                        <div className="relative w-full max-w-xs ml-auto">
                            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                            <Input
                                type="text"
                                placeholder="Search by resource..."
                                className="pl-8"
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                            />
                        </div>
                    </div>

                    {/* map recommendations to individual cards */}
                    {filteredRecommendations.length > 0 ? (
                        filteredRecommendations.map((rec) => (
                            <RecommendationCard recommendation={rec} key={rec.recommendationId} />
                        ))
                    ) : (
                        <div className="flex justify-center p-8 text-muted-foreground text-sm">
                            No recommendations found matching {searchQuery}
                        </div>
                    )}
                </div>
            </DrawerContent>
        </Drawer>
    );
}
