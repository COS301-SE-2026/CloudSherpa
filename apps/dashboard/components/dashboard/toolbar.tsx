"use client";

import { Button } from "@/components/atoms/button";
import { Dropdown } from "@/components/atoms/dropdown";
import { TimePeriodSelector } from "@/components/molecules/timePeriodSelector";
import { DashboardSelector } from "../molecules/dashboardSelector";
import { Tooltip } from "@/components/molecules/tooltip";
import { DateRange } from "react-day-picker";

import { useState } from "react";
import { Pencil } from "lucide-react";
import { cn } from "@/lib/utils";

interface DashboardStub {
  id: string;
  label: string;
}
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
              "bg-card border-border text-foreground-secondary hover:text-primary hover:border-primary transition-all duration-200",
              isEditMode && "bg-primary/10 border-primary text-primary shadow-inner ring-1 ring-primary/30",
            )}>
            <Pencil className={cn("h-4 w-4", isEditMode && "fill-current")} />
          </Button>
        </Tooltip>

        {isEditMode && <span className="text-xs font-medium text-primary animate-pulse">Editing Layout...</span>}
      </div>

      <div className="w-full flex flex-row items-center justify-end gap-3">
        <TimePeriodSelector date={dateRange} onDateChange={onDateRangeChange} />
      </div>
    </div>
  );
}
