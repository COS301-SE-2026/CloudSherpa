"use client";
import {
    Drawer,
    DrawerTrigger,
    DrawerContent,
    DrawerTitle,
    DrawerHeader,
} from "@/components/atoms/drawer";
import { Button } from "@/components/atoms/button";
import { RecommendationGroup } from "@/features/optimization/types/recommendations";
import RecommendationCard from "@/features/optimization/components/recCard";
import { useState, useMemo, act } from "react";
import { X, Search } from "lucide-react";
import { Input } from "@/components/atoms/input";
import Dropdown from "@/components/molecules/dropdown";

const ACTIONS = [
    { value: "TERMINATE", label: "Terminate" },
    { value: "DOWNSIZE", label: "Downsize" },
    { value: "MODERNIZE", label: "Modernize" },
    { value: "SUSPEND", label: "Suspend" },
];

interface RecDrawer {
    group: RecommendationGroup;
}

export default function RecDrawer({ group }: Readonly<RecDrawer>) {
    const [isOpen, setIsOpen] = useState(false);
    const [searchQuery, setSearchQuery] = useState("");
    const [actionFilter, setActionFilter] = useState("");

    const filteredRecommendations = useMemo(() => {
        let filtered = group.recommendations;

        if (actionFilter) {
            filtered = filtered.filter((rec) => rec.action_type === actionFilter);
        }

        if (searchQuery.trim()) {
            const lowerCaseQuery = searchQuery.toLowerCase();
            filtered = filtered.filter((rec) => {
                const searchableName = rec.resource_displayName ?? "";

                return (
                    searchableName.toLowerCase().includes(lowerCaseQuery) ||
                    rec.action_type.toLowerCase().includes(lowerCaseQuery)
                );
            });
        }

        return filtered;
    }, [searchQuery, actionFilter, group.recommendations]);

    return (
        <Drawer direction="right" dismissible={false} open={isOpen} onOpenChange={setIsOpen}>
            <DrawerTrigger asChild>
                <Button variant="secondary">View</Button>
            </DrawerTrigger>

            {/* drawer width could be volatile so will keep an eye on it */}
            <DrawerContent className="w-[90vw]! sm:w-[40vw]! sm:max-w-[1000px]!">
                <DrawerHeader className="flex flex-col">
                    <div className="flex flex-row lex-row justify-between items-center">
                        <DrawerTitle className="text-xl">{group.displayName}</DrawerTitle>
                        <Button className="w-fit" variant="ghost" onClick={() => setIsOpen(false)}>
                            <X />
                        </Button>
                    </div>
                    <div className="flex flex-row w-full justify-end gap-2">
                        <div className="relative w-full">
                            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                            <Input
                                type="text"
                                placeholder="Search by resource..."
                                className="pl-8 w-full"
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                            />
                        </div>
                        <Dropdown
                            options={ACTIONS}
                            value={actionFilter}
                            onSelect={setActionFilter}
                            disableSearch
                            widthVariant="medium"
                            placeholder="Action..."
                        />
                    </div>
                </DrawerHeader>
                <div className="h-full w-full p-4 overflow-y-auto space-y-4">
                    {filteredRecommendations.length > 0 ? (
                        filteredRecommendations.map((rec) => (
                            <RecommendationCard recommendation={rec} key={rec.resource_id} />
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
