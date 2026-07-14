"use client";

import { useState } from "react";
import { format } from "date-fns";
import { Calendar as CalendarIcon, ChevronLeft, ChevronDown, Filter, Check } from "lucide-react";
import { DateRange } from "react-day-picker";
import { cn } from "@/lib/utils";
import { Button } from "@/components/atoms/button";
import { Calendar } from "@/components/atoms/calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/atoms/popover";
import { useWindowStore } from "@/features/dashboard/stores/window-store";
import { TimeWindowPreset } from "@/features/dashboard/types/timewindow";
import {
    Command,
    CommandEmpty,
    CommandGroup,
    CommandInput,
    CommandItem,
    CommandList,
    CommandSeparator,
} from "@/components/atoms/command";

type DurationPreset = Exclude<TimeWindowPreset, "custom">;

const presets: { id: DurationPreset; label: string }[] = [
    { id: "1m", label: "1 min" },
    { id: "2m", label: "2 min" },
    { id: "5m", label: "5 min" },
    { id: "1h", label: "1 hour" },
    { id: "24h", label: "24 hours" },
    { id: "7d", label: "7 days" },
    { id: "30d", label: "30 days" },
];

function getPresetRange(presetId: DurationPreset): DateRange {
    const to = new Date();
    const minuteMs = 60 * 1000;
    const hourMs = 60 * minuteMs;
    const dayMs = 24 * hourMs;

    const durationByPreset: Record<DurationPreset, number> = {
        "1m": minuteMs,
        "2m": 2 * minuteMs,
        "5m": 5 * minuteMs,
        "1h": hourMs,
        "24h": 24 * hourMs,
        "7d": 7 * dayMs,
        "30d": 30 * dayMs,
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
    const setSelectedPreset = useWindowStore((state) => state.setPreset);
    const selectedPreset = useWindowStore((state) => state.selectedPreset);

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
                    className="group md:min-w-40 w-9 md:w-fit px-0 md:px-4 justify-center md:justify-start text-left font-normal bg-card text-foreground border-border hover:bg-hover hover:text-foreground data-[state=open]:text-foreground data-[state=open]:hover:text-foreground transition-button"
                >
                    {/* Mobile View */}
                    <Filter className="h-4 w-4 block md:hidden" />

                    {/* Desktop View */}
                    <div className="hidden md:flex w-full h-full flex-row items-center">
                        <CalendarIcon className="mr-2 h-4 w-4" />
                        <span className="">Last {getDisplayLabel()}</span>
                    </div>
                    <ChevronDown className="hidden md:block ml-auto h-4 w-4 shrink-0 text-muted-foreground transition-transform duration-200 group-data-[state=open]:rotate-180" />
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
                                    onSelect={() => setView("custom")}
                                    className="cursor-pointer bg-primary text-primary-foreground data-[selected=true]:bg-primary/90 data-[selected=true]:text-primary-foreground font-medium flex justify-center py-2.5 mt-1"
                                >
                                    <CalendarIcon className="mr-2 h-4 w-4" />
                                    Custom Range
                                </CommandItem>
                            </CommandGroup>
                        </CommandList>
                    </Command>
                ) : (
                    <div className="flex flex-col p-3 animate-in fade-in zoom-in-95 duration-200">
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
                            className="bg-popover text-foreground"
                        />
                    </div>
                )}
            </PopoverContent>
        </Popover>
    );
}
