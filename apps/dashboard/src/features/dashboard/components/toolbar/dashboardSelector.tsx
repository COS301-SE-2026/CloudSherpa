"use client";

import { useState } from "react";
import { Plus, ChevronLeft, ChevronDown, Check, Trash } from "lucide-react";
import { cn } from "@/lib/utils";
import { Button } from "@/components/atoms/button";
import { Input } from "@/components/atoms/input";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/atoms/popover";
import {
    Command,
    CommandEmpty,
    CommandGroup,
    CommandInput,
    CommandItem,
    CommandList,
    CommandSeparator,
} from "@/components/atoms/command";

interface DashboardStub {
    id: string;
    displayName: string;
}

interface DashboardSelectorProps {
    dashboards: DashboardStub[];
    selectedId: string;
    onSelect: (id: string) => void;
    onCreate: (displayName: string) => void;
    onDelete: (id: string) => void;
}

export function DashboardSelector({
    dashboards,
    selectedId,
    onSelect,
    onCreate,
    onDelete,
}: Readonly<DashboardSelectorProps>) {
    const [open, setOpen] = useState(false);
    const [view, setView] = useState<"list" | "create">("list");
    const [newDashboardName, setNewDashboardName] = useState("");
    const [searchQuery, setSearchQuery] = useState("");

    const selectedDashboard = dashboards.find((d) => d.id === selectedId);

    const normalizedQuery = searchQuery.trim();
    const hasExactNameMatch = dashboards.some(
        (d) => d.displayName.trim().toLowerCase() === normalizedQuery.toLowerCase()
    );
    const canCreateFromSearch = normalizedQuery.length > 0 && !hasExactNameMatch;

    const handleCreateFromSearch = () => {
        if (!canCreateFromSearch) return;
        onCreate(normalizedQuery);
        setSearchQuery("");
        setNewDashboardName("");
        setView("list");
        setOpen(false);
    };

    const handleCreate = () => {
        if (newDashboardName.trim()) {
            onCreate(newDashboardName);
            setNewDashboardName("");
            setView("list");
            setOpen(false);
        }
    };

    return (
        <Popover
            open={open}
            onOpenChange={(val) => {
                setOpen(val);
                if (!val) setView("list");
                setSearchQuery("");
            }}
        >
            <PopoverTrigger asChild>
                <Button variant="outline" className="group flex justify-between">
                    {selectedDashboard?.displayName || "Select Dashboard"}
                    <ChevronDown className="ml-2 h-4 w-4 shrink-0 text-muted-foreground transition-transform duration-200 group-data-[state=open]:rotate-180" />
                </Button>
            </PopoverTrigger>

            <PopoverContent
                className="p-0 w-72 bg-popover border-border-strong shadow-xl"
                align="start"
            >
                {view === "list" ? (
                    <Command>
                        <CommandInput
                            placeholder="Search dashboards..."
                            className="h-9"
                            value={searchQuery}
                            onValueChange={setSearchQuery}
                        />
                        <CommandList>
                            <CommandEmpty>No dashboard found.</CommandEmpty>
                            <CommandGroup heading="My Dashboards">
                                {dashboards.map((d) => (
                                    <CommandItem
                                        key={d.id}
                                        value={d.displayName}
                                        className="group flex items-center justify-between cursor-pointer"
                                        onSelect={() => {
                                            onSelect(d.id);
                                            setOpen(false);
                                        }}
                                    >
                                        <div className="flex items-center flex-1 overflow-hidden w-full gap-2">
                                            <Check
                                                className={cn(
                                                    " h-4 w-4 shrink-0",
                                                    selectedId === d.id
                                                        ? "opacity-100"
                                                        : "opacity-0"
                                                )}
                                            />
                                            <span className="truncate">{d.displayName}</span>
                                        </div>

                                        <Button
                                            variant="ghost"
                                            size="icon"
                                            className={cn(
                                                "h-6 w-6 shrink-0 opacity-0 group-hover:opacity-100"
                                            )}
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                onDelete(d.id);
                                            }}
                                        >
                                            <Trash className="h-3.5 w-3.5" />
                                        </Button>
                                    </CommandItem>
                                ))}
                            </CommandGroup>
                            {canCreateFromSearch && (
                                <CommandGroup>
                                    <CommandItem
                                        value={"create " + normalizedQuery}
                                        onSelect={handleCreateFromSearch}
                                        className="cursor-pointer"
                                    >
                                        <Plus className="mr-2 h-4 w-4" />
                                        Create dashboard &quot;{normalizedQuery}&quot;
                                    </CommandItem>
                                </CommandGroup>
                            )}
                            <CommandSeparator />
                            <CommandGroup>
                                {!canCreateFromSearch && (
                                    <CommandItem
                                        onSelect={() => setView("create")}
                                        className="cursor-pointer"
                                    >
                                        <Plus className="mr-2 h-4 w-4" />
                                        Create New Dashboard
                                    </CommandItem>
                                )}
                            </CommandGroup>
                        </CommandList>
                    </Command>
                ) : (
                    // create new dashboard view
                    <div className="flex flex-col p-3">
                        <div className="grid grid-cols-[30px_1fr_30px] items-center mb-3">
                            <Button variant="ghost" size="icon" onClick={() => setView("list")}>
                                <ChevronLeft className="h-4 w-4" />
                            </Button>

                            <span className="text-center">New Dashboard</span>

                            <div className="w-7" />
                        </div>

                        <Input
                            autoFocus
                            placeholder="e.g. Production AWS Costs"
                            value={newDashboardName}
                            onChange={(e) => setNewDashboardName(e.target.value)}
                            onKeyDown={(e) => e.key === "Enter" && handleCreate()}
                        />

                        <Button
                            className="mt-3 w-full bg-primary text-primary-foreground hover:bg-primary/90"
                            onClick={handleCreate}
                            disabled={!newDashboardName.trim()}
                        >
                            Create Dashboard
                        </Button>
                    </div>
                )}
            </PopoverContent>
        </Popover>
    );
}
