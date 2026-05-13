"use client";

import * as React from "react";
import { useState } from "react";
import { format, subDays } from "date-fns";
import { Calendar as CalendarIcon, ChevronLeft } from "lucide-react";
import { DateRange } from "react-day-picker";
import { cn } from "@/lib/utils";
import { Button } from "@/components/atoms/button";
import { Calendar } from "@/components/atoms/calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/atoms/popover";
import { ChevronDown } from "lucide-react";

const presets = [
  { id: "1m", label: "1 min" },
  { id: "2mm", label: "2 min" },
  { id: "5m", label: "5 min" },
  { id: "1h", label: "1 hour" },
  { id: "24h", label: "24 hours" },
  { id: "7d", label: "7 days" },
  { id: "30d", label: "7 days" },
];

export function TimePeriodSelector() {
  const [open, setOpen] = useState(false);
  const [view, setView] = useState<"presets" | "custom">("presets");
  const [selectedPreset, setSelectedPreset] = useState<string>("7d");
  const [date, setDate] = useState<DateRange | undefined>({
    from: subDays(new Date(), 7),
    to: new Date(),
  });

  const getDisplayLabel = () => {
    if (selectedPreset !== "custom") {
      return presets.find((p) => p.id === selectedPreset)?.label;
    }
    if (date?.from) {
      return date.to ? `${format(date.from, "LLL dd")} - ${format(date.to, "LLL dd")}` : format(date.from, "LLL dd");
    }
    return "Pick a date";
  };

  return (
    <Popover
      open={open}
      onOpenChange={(val) => {
        setOpen(val);
        if (!val) setView("presets");
      }}>
      <PopoverTrigger asChild>
        <Button
          variant="outline"
          className="md:min-w-40 w-fit justify-start text-left font-normal bg-card border-border hover:bg-hover transition-button">
          <div className="w-full h-full flex flex-row items-center">
            <CalendarIcon className="mr-2 h-4 w-4 text-foreground-secondary" />
            <span className="text-foreground">{getDisplayLabel()}</span>
          </div>
          {/* icon rotates on open state to show that it will come back up if closed */}
          <ChevronDown /> 
        </Button>
      </PopoverTrigger>

      <PopoverContent className="p-0 w-auto bg-popover border-border-strong shadow-xl" align="start">
        {view === "presets" ? (
          <div className="flex flex-col p-1 w-44">
            {presets.map((p) => (
              <Button
                key={p.id}
                variant="ghost"
                className={cn(
                  "justify-start font-normal transition-button",
                  selectedPreset === p.id
                    ? "bg-active text-primary-foreground"
                    : "text-foreground-secondary hover:bg-hover hover:text-foreground",
                )}
                onClick={() => {
                  setSelectedPreset(p.id);
                  setOpen(false);
                }}>
                {p.label}
              </Button>
            ))}

            {/* separator */}
            <div className="h-px bg-border-subtle my-1 w-full" />

            <Button
              variant="ghost"
              className="justify-start font-medium text-accent hover:bg-hover transition-button"
              onClick={() => setView("custom")}>
              Custom Range
            </Button>
          </div>
        ) : (
          // custom view
          <div className="flex flex-col animate-in fade-in zoom-in-95 duration-200">
            <div className="flex items-center p-2 border-border-subtle">
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setView("presets")}
                className="h-8 px-2 hover:bg-hover text-foreground-secondary">
                <ChevronLeft className="h-4 w-4 mr-1" />
                Back
              </Button>
              <span className="text-xs font-medium ml-2 text-foreground-muted">Custom Range</span>
            </div>
            <Calendar
              mode="range"
              defaultMonth={date?.from}
              selected={date}
              onSelect={(range) => {
                setDate(range);
                if (range?.from && range?.to) setSelectedPreset("custom");
              }}
              // edit to adjust how many months display at a time
              numberOfMonths={2}
              className="bg-popover text-foreground"
            />
          </div>
        )}
      </PopoverContent>
    </Popover>
  );
}
