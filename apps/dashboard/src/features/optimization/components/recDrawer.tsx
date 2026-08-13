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
import { useState, useMemo } from "react";
import { X, Search } from "lucide-react";
import { Input } from "@/components/atoms/input";

interface RecDrawer {
    group: RecommendationGroup;
}

export default function RecDrawer({ group }: Readonly<RecDrawer>) {
    const [isOpen, setIsOpen] = useState(false);
    const [searchQuery, setSearchQuery] = useState("");

    const filteredRecommendations = useMemo(() => {
        if (!searchQuery.trim()) return group.recommendations;

        const lowerCaseQuery = searchQuery.toLowerCase();

        return group.recommendations.filter(
            (rec) =>
                rec.resource_id.toLowerCase().includes(lowerCaseQuery) ||
                rec.action_type.toLowerCase().includes(lowerCaseQuery)
        );
    }, [searchQuery, group.recommendations]);

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
                    <div className="relative">
                        <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                        <Input
                            type="text"
                            placeholder="Search by resource ID or action type..."
                            className="pl-8"
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
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
