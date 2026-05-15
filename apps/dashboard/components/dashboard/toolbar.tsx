"use client";

import { Button } from "@/components/atoms/button";
import { TimePeriodSelector } from "@/components/molecules/timePeriodSelector";
import { DashboardSelector } from "../molecules/dashboardSelector";
import { Tooltip } from "@/components/molecules/tooltip";
import { DateRange } from "react-day-picker";
import { SidebarTrigger } from "@/components/atoms/sidebar";

import { Pencil } from "lucide-react";
import { cn } from "@/lib/utils";

interface ToolbarProps {
  isEditMode: boolean;
  setIsEditMode: (val: boolean) => void;
  selectedDashboardId: string;
  onDashboardChange: (id: string) => void;
  dateRange: DateRange | undefined;
  onDateRangeChange: (range: DateRange | undefined) => void;
}

export default function Toolbar({
  isEditMode,
  setIsEditMode,
  selectedDashboardId,
  onDashboardChange,
  dateRange,
  onDateRangeChange,
}: ToolbarProps) {
  const dashboards = [
    { id: "ds-1", label: "Global Cost Overview" },
    { id: "ds-2", label: "AWS Production Metrics" },
    { id: "ds-3", label: "Azure Spending Forecast" },
  ];

  return (
    <div className="w-full flex flex-row items-center justify-between gap-4 p-4 transition-card">
      <div className="flex flex-row items-end gap-2">
        {/* Toggle Group */}
          <div className="flex flex-col items-start">
            <SidebarTrigger className="bg-card border-border hover:bg-hover hover:text-secondary h-9 w-9 border" />
          </div>
        {/* vertical seperator as to not confuse user of sidebar trigger functionality */}
        <div className="h-9 w-px bg-border self-end mb-0" />

        {/* Dashboard Group */}
        <div className="w-fit h-full flex flex-col items-start justify-start">
          <span className="text-[10px] uppercase tracking-wider text-muted-foreground font-bold px-1">DASHBOARD</span>
          <div className="flex items-center gap-3">
            <DashboardSelector
              dashboards={dashboards}
              selectedId={selectedDashboardId}
              onSelect={onDashboardChange}
              onCreate={(name) => console.log(name)}
            />
            <Tooltip content={isEditMode ? "Exit Edit Mode" : "Edit Dashboard Layout"}>
              <Button
                variant="outline"
                size="icon"
                onClick={() => setIsEditMode(!isEditMode)}
                className={cn(
                  "bg-card border-border text-foreground hover:text-primary hover:border-primary transition-all duration-200",
                  isEditMode && "bg-primary/10 border-primary text-primary shadow-inner ring-1 ring-primary/30",
                )}>
                <Pencil className={cn("h-4 w-4", isEditMode && "fill-current")} />
              </Button>
            </Tooltip>
            {isEditMode && (
              <span className="text-xs font-medium text-primary animate-pulse whitespace-nowrap">
                Editing Layout...
              </span>
            )}
          </div>
        </div>
      </div>

      {/* Time Period Group */}
      <div className="w-fit h-full flex flex-col items-start justify-start">
        <span className="text-[10px] uppercase tracking-wider text-muted-foreground font-bold px-1">TIME PERIOD</span>
        <TimePeriodSelector date={dateRange} onDateChange={onDateRangeChange} />
      </div>
    </div>
  );
}
