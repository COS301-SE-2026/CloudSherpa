"use client";
import {
    Drawer,
    DrawerTrigger,
    DrawerContent,
    DrawerTitle,
    DrawerHeader,
    DrawerDescription,
} from "@/components/atoms/drawer";
import { Button } from "@/components/atoms/button";
import { RecommendationGroup } from "@/features/optimization/types/recommendations";
import RecommendationCard from "@/features/optimization/components/recCard";
import { useState, useMemo } from "react";
import { X, Search } from "lucide-react";
import { Input } from "@/components/atoms/input";
import Dropdown from "@/components/molecules/dropdown";

const ACTIONS = [
    { value: "ALL", label: "All Actions" },
    { value: "TERMINATE", label: "Terminate" },
    { value: "DOWNSIZE", label: "Downsize" },
    { value: "MODERNIZE", label: "Modernize" },
    { value: "SUSPEND", label: "Suspend" },
];

interface RecDrawer {
    group: RecommendationGroup;
    isOpen: boolean;
    setIsOpen: (isOpen: boolean) => void;
}

export default function RecDrawer({ group, isOpen, setIsOpen }: Readonly<RecDrawer>) {
    const [searchQuery, setSearchQuery] = useState("");
    const [actionFilter, setActionFilter] = useState("ALL");

    const filteredRecommendations = useMemo(() => {
        let filtered = group.recommendations;

        if (actionFilter && actionFilter !== "ALL") {
            filtered = filtered.filter((rec) => rec.actionType === actionFilter);
        }

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
    }, [searchQuery, actionFilter, group.recommendations]);

    return (
        <Drawer direction="right" dismissible={true} open={isOpen} onOpenChange={setIsOpen}>
            {/* drawer width could be volatile so will keep an eye on it */}
            <DrawerContent className="w-[90vw]! sm:w-[40vw]! lg:w-[45vw]! sm:max-w-[1400px]! p-4">
                <DrawerHeader className="flex flex-col">
                    <div className="flex flex-row lex-row justify-between items-center">
                        <DrawerTitle className="text-xl">{group.displayName}</DrawerTitle>
                        {/* closes drawer */}
                        <Button className="w-fit" variant="ghost" onClick={() => setIsOpen(false)}>
                            <X />
                        </Button>
                    </div>
                    <DrawerDescription>
                        View the recommendations and decide on the action to take
                    </DrawerDescription>
                </DrawerHeader>

                <div className="h-full w-full p-4 overflow-y-auto space-y-4">
                    {/* filters */}
                    <div className="flex flex-row w-full justify-between gap-2 pt-2">
                        {/* search, uses relative and absolute to layer icon on input */}
                        <div className="relative w-full">
                            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                            <Input
                                type="text"
                                placeholder="Search by resource..."
                                className="pl-8"
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                            />
                        </div>
                        {/* filter by action */}
                        <Dropdown
                            options={ACTIONS}
                            value={actionFilter}
                            onSelect={setActionFilter}
                            disableSearch
                            widthVariant="large"
                            placeholder="Action..."
                        />
                    </div>

                    {/* map recommendations to individual cards */}
                    {filteredRecommendations.length > 0 ? (
                        filteredRecommendations.map((rec) => (
                            <RecommendationCard recommendation={rec} key={rec.resourceId} />
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
