"use client";

import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/atoms/tooltip";
import { Info, ListFilter, Search } from "lucide-react";
import { Button } from "@/components/atoms/button";
import { Input } from "@/components/atoms/input";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/atoms/card";
import { CostBreakdownItem } from "../../types/dtos";
import { useState } from "react";

interface CostBreakdownListProps {
    name: string;
    description: string;
    eachEntry?: CostBreakdownItem[];
    search: string;
    onSearchChange: (value: string) => void;
}

export default function CostBreakdownList({
    name,
    description,
    eachEntry = [],
    search,
    onSearchChange,
}: Readonly<CostBreakdownListProps>) {
    const [sortAscending, setSortAscending] = useState(false);

    const forFilteredEntries = eachEntry
        .filter((forEntry) => forEntry.label.toLowerCase().includes(search.toLowerCase()))
        .toSorted((a, b) =>
            sortAscending ? a.percentage - b.percentage : b.percentage - a.percentage
        );

    return (
        <Card>
            <CardHeader>
                <CardTitle className="flex flex-row justify-between items-center text-sm font-normal text-muted-foreground">
                    {" "}
                    {name}
                    <Tooltip>
                        <TooltipTrigger>
                            {" "}
                            <Info className="h-4 w-4" strokeWidth={1.75} />{" "}
                        </TooltipTrigger>

                        <TooltipContent> ... </TooltipContent>
                    </Tooltip>
                </CardTitle>

                <p className="text-xs text-muted-foreground"> {description} </p>
            </CardHeader>

            <CardContent className="flex flex-col gap-4">
                <div className="flex flex-row gap-2">
                    <div className="relative flex-1">
                        <Search
                            className="absolute left-2.5 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground"
                            strokeWidth={1.75}
                        />

                        <Input
                            value={search}
                            onChange={(change) => onSearchChange(change.target.value)}
                            placeholder="Search..."
                            className="pl-8"
                        />
                    </div>

                    <Button
                        variant="outline"
                        size="icon"
                        onClick={() => {
                            setSortAscending((current) => !current);
                        }}
                    >
                        {" "}
                        <ListFilter className="h-4 w-4" strokeWidth={1.75} />{" "}
                    </Button>
                </div>

                <div
                    className="flex flex-col gap-4 max-h-72 overflow-y-auto pr-1"
                    style={{ scrollbarWidth: "none" }}
                >
                    {forFilteredEntries.length === 0 ? (
                        <div className="text-center text-muted-foreground text-sm py-8">
                            {" "}
                            No breakdown items available{" "}
                        </div>
                    ) : (
                        forFilteredEntries.map((entry) => (
                            <div key={entry.id} className="flex flex-col gap-1.5">
                                <span className="text-sm"> {entry.label} </span>

                                <div className="flex flex-row items-center gap-3">
                                    <div className="h-1.5 w-full bg-muted rounded-full overflow-hidden">
                                        <div
                                            className="h-full bg-primary rounded-full"
                                            style={{ width: `${entry.percentage}%` }}
                                        />
                                    </div>

                                    <span className="text-xs text-muted-foreground w-9 text-right">
                                        {" "}
                                        {entry.percentage.toFixed(2)}%{" "}
                                    </span>
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </CardContent>
        </Card>
    );
}
