"use client";

import { useState } from "react";
import { format } from "date-fns";
import { Calendar as CalendarIcon, ChevronLeft, ChevronDown, Filter, Check } from "lucide-react";
import { DateRange } from "react-day-picker";
import { cn } from "@/lib/utils";
import { Button } from "@/components/atoms/button";
import { Calendar } from "@/components/atoms/calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/atoms/popover";
import { TimeWindowPreset } from "@/features/dashboard/types/timewindow";
import {
    Command,
    CommandEmpty,
    CommandGroup,
    CommandItem,
    CommandList,
    CommandSeparator,
} from "@/components/atoms/command";
import { useDashboardStore } from "../../stores/dashboard-store";
import { ppid } from "process";

export const presets: { id: TimeWindowPreset; label: string }[] = [
    { id: "T_5_MIN", label: "5 min" },
    { id: "T_15_MIN", label: "15 min" },
    { id: "T_30_MIN", label: "30 min" },
    { id: "T_1_HOUR", label: "1 hour" },
    { id: "T_6_HOUR", label: "6 hours" },
    { id: "T_12_HOUR", label: "12 hours" },
    { id: "T_24_HOUR", label: "24 hours" },
    { id: "T_7_DAYS", label: "7 days" },
    { id: "T_30_DAYS", label: "30 days" },
];

export function getPresetRange(presetId: TimeWindowPreset): DateRange | undefined {
    if (presetId == "custom") {
        return undefined;
    }
    const to = new Date();
    const minuteMs = 60 * 1000;
    const hourMs = 60 * minuteMs;
    const dayMs = 24 * hourMs;

    const durationByPreset: Record<TimeWindowPreset, number> = {
        T_5_MIN: 5 * minuteMs,
        T_15_MIN: 15 * minuteMs,
        T_30_MIN: 30 * minuteMs,
        T_1_HOUR: hourMs,
        T_6_HOUR: 6 * hourMs,
        T_12_HOUR: 12 * hourMs,
        T_24_HOUR: 24 * hourMs,
        T_7_DAYS: 7 * dayMs,
        T_30_DAYS: 30 * dayMs,
        custom: 0,
    };

    return {
        from: new Date(to.getTime() - (durationByPreset[presetId] ?? 7 * dayMs)),
        to,
    };
}

export function TimePeriodSelector({
    date,
    onDateChange,
}: Readonly<{
    date: DateRange | undefined;
    onDateChange: (range: DateRange | undefined) => void;
}>) {
    const [open, setOpen] = useState(false);
    const [view, setView] = useState<"presets" | "custom">("presets");
    const setSelectedPreset = useDashboardStore((state) => state.setPreset);
    const selectedPreset = useDashboardStore((state) => state.selectedPreset);

    const getDisplayLabel = () => {
        if (selectedPreset !== "custom") {
            return presets.find((p) => p.id === selectedPreset)?.label;
        }
        if (date?.from) {
            return date.to
                ? `${format(date.from, "LLL dd")} - ${format(date.to, "LLL dd")}`
                : format(date.from, "LLL dd");
        }
        return "Pick a date";
    };

    return (
        <div id="timePeriodSelector ">
            <Popover
                open={open}
                onOpenChange={(val) => {
                    setOpen(val);
                    if (!val) setView("presets");
                }}
            >
                <PopoverTrigger asChild>
                    <Button
                        variant="outline"
                        className="group flex justify-between "
                        aria-label="window-selector"
                    >
                        {/* Mobile View */}
                        <Filter className="h-4 w-4 block md:hidden" />

                        {/* Desktop View */}
                        <div className="flex flex-row gap-2">
                            <CalendarIcon className="h-4 w-4" />
                            <span className="">Last {getDisplayLabel()}</span>
                        </div>
                        <ChevronDown className="ml-2 h-4 w-4 shrink-0 text-muted-foreground transition-transform duration-200 group-data-[state=open]:rotate-180" />
                    </Button>
                </PopoverTrigger>

                <PopoverContent
                    className="p-0 w-auto bg-popover border-border-strong shadow-xl"
                    align="end"
                >
                    {view === "presets" ? (
                        <Command className="w-50">
                            <CommandList className="max-h-none">
                                <CommandEmpty>No preset found.</CommandEmpty>
                                <CommandGroup heading="Range Presets">
                                    {presets.map((p) => (
                                        <CommandItem
                                            aria-label={p.label}
                                            key={p.id}
                                            value={p.label}
                                            onSelect={() => {
                                                setSelectedPreset(p.id);
                                                onDateChange(getPresetRange(p.id));
                                                setOpen(false);
                                            }}
                                            className="cursor-pointer"
                                        >
                                            <Check
                                                className={cn(
                                                    "mr-2 h-4 w-4",
                                                    selectedPreset === p.id
                                                        ? "opacity-100"
                                                        : "opacity-0"
                                                )}
                                            />
                                            {p.label}
                                        </CommandItem>
                                    ))}
                                </CommandGroup>
                                <CommandSeparator />
                                <CommandGroup>
                                    <CommandItem
                                        aria-label="custom range"
                                        onSelect={() => setView("custom")}
                                        className="cursor-pointer flex justify-center"
                                    >
                                        <CalendarIcon className="mr-2 h-4 w-4" />
                                        Custom Range
                                    </CommandItem>
                                </CommandGroup>
                            </CommandList>
                        </Command>
                    ) : (
                        <div className="flex flex-col p-3 ">
                            <div className="relative flex w-full items-center justify-center mb-3 min-h-8">
                                <Button
                                    variant="ghost"
                                    size="icon"
                                    onClick={() => setView("presets")}
                                    className="absolute left-0 h-7 w-7 hover:bg-hover text-foreground-secondary"
                                >
                                    <ChevronLeft className="h-4 w-4" />
                                </Button>

                                <span className="text-sm font-medium text-foreground">
                                    Select custom range
                                </span>
                            </div>

                            <Calendar
                                mode="range"
                                defaultMonth={date?.from}
                                selected={date}
                                onSelect={(range) => {
                                    onDateChange(range);
                                    if (range?.from && range?.to) setSelectedPreset("custom");
                                }}
                                numberOfMonths={2}
                            />
                        </div>
                    )}
                </PopoverContent>
            </Popover>
        </div>
    );
}
