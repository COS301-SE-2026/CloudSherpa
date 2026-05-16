"use client";

import { Button } from "@/components/atoms/button";
import { TimePeriodSelector } from "@/components/molecules/timePeriodSelector";
import { DashboardSelector } from "@/components/molecules/dashboardSelector";
import { Tooltip } from "@/components/molecules/tooltip";
import { DateRange } from "react-day-picker";
import { SidebarTrigger } from "@/components/atoms/sidebar";
import { type DashboardStub } from "@/app/dashboard/page";

import { Pencil } from "lucide-react";
import { cn } from "@/lib/utils";

interface ToolbarProps {
  dashboards: DashboardStub[];
  isEditMode: boolean;
  handleStartEditing: () => void;
  handleSaveEdit: () => void;
  handleCancelEdit: () => void;
  selectedDashboardId: string;
  onDashboardChange: (id: string) => void;
  onCreateDashboard: (name: string) => void;
  dateRange: DateRange | undefined;
  onDateRangeChange: (range: DateRange | undefined) => void;
}

export default function Toolbar({
  dashboards,
  isEditMode,
  handleStartEditing,
  handleSaveEdit,
  handleCancelEdit,
  selectedDashboardId,
  onDashboardChange,
  onCreateDashboard,
  dateRange,
  onDateRangeChange,
}: ToolbarProps) {
  return (
    <div className="w-full flex flex-row items-center justify-between transition-card">
      <div className="flex flex-row items-end gap-2">
        {/* Toggle Group */}
        <div className="flex flex-col items-start">
          <SidebarTrigger className="bg-card border-border hover:bg-hover hover:text-secondary h-9 w-9 border" />
        </div>
        {/* vertical seperator as to not confuse user of sidebar trigger functionality */}
        <div className="h-9 w-px bg-border self-end mb-0" />

        {/* Dashboard Group */}
        <div className="w-fit h-full flex flex-row items-start justify-start gap-4">
          <div className="w-full h-full flex flex-col">
            <span className="text-[10px] uppercase tracking-wider text-muted-foreground font-bold px-1">DASHBOARD</span>
            <div className="flex items-center gap-3">
              <DashboardSelector
                dashboards={dashboards}
                selectedId={selectedDashboardId}
                onSelect={onDashboardChange}
                onCreate={onCreateDashboard}
              />
            </div>
          </div>

          {/* Edit Group */}
          <div className="w-fit h-full flex flex-col">
            {/* <Tooltip content={isEditMode ? "Exit Edit Mode" : "Edit Dashboard Layout"}> */}
            {isEditMode && (
              <span className="text-[10px] uppercase tracking-wider text-muted-foreground font-bold px-1 animate-pulse whitespace-nowrap">
                Editing Layout...
              </span>
            )}
            {!isEditMode && (
              <span className="text-[10px] uppercase tracking-wider text-muted-foreground font-bold px-1">Edit</span>
            )}

            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size={isEditMode ? "sm" : "icon"}
                onClick={isEditMode ? handleCancelEdit : handleStartEditing}
                className={cn(
                  "bg-card border-border text-foreground hover:text-primary hover:border-primary transition-all duration-200",
                  isEditMode &&
                    "bg-destructive/10 border-destructive text-destructive hover:bg-destructive/20 hover:text-destructive hover:border-destructive",
                )}>
                {isEditMode ? "Cancel" : <Pencil className="h-4 w-4" />}
              </Button>

              {isEditMode && (
                <Button
                  size="sm"
                  onClick={handleSaveEdit}
                  className="bg-primary text-primary-foreground hover:bg-primary/90 shadow-sm">
                  Save
                </Button>
              )}
            </div>
            {/* </Tooltip> */}
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
