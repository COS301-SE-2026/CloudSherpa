"use client";

import { TimePeriodSelector } from "@/features/dashboard/components/toolbar/timePeriodSelector";
import { DashboardSelector } from "@/features/dashboard/components/toolbar/dashboardSelector";
import { DateRange } from "react-day-picker";
import { SidebarTrigger } from "@/components/atoms/sidebar";
import { DashboardStub } from "@/features/dashboard/types/widgets";
import EditButton from "@/features/dashboard/components/toolbar/editButton";

interface ToolbarProps {
  dashboards: DashboardStub[];
  isEditMode: boolean;
  handleAddWidget: () => void;
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
  handleAddWidget,
  handleStartEditing,
  handleSaveEdit,
  handleCancelEdit,
  selectedDashboardId,
  onDashboardChange,
  onCreateDashboard,
  dateRange,
  onDateRangeChange,
}: Readonly<ToolbarProps>) {
  return (
    <div className="w-full flex flex-row items-center justify-between transition-card">
      <div className="flex flex-row items-end gap-2">
        {/* Toggle Group */}
        <div className="flex flex-col items-start">
          <SidebarTrigger className="bg-card border-border hover:bg-hover hover:text-foreground h-9 w-9 border" />
        </div>
        {/* vertical seperator as to not confuse user of sidebar trigger functionality */}
        <div className="hidden md:flex h-9 w-px bg-border self-end mb-0" />

        {/* Dashboard Group */}
        <div className="w-fit h-full flex flex-row items-start justify-start gap-4">
          <div className="w-full h-full flex flex-col">
            <span className="hidden md:flex text-[10px] uppercase tracking-wider text-muted-foreground font-bold px-1">DASHBOARD</span>
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
          <div className="fixed bottom-6 right-6 z-[100] flex w-fit flex-col items-end md:static md:h-full md:items-start md:z-auto">
            {isEditMode && (
              <span className="mb-2 rounded-md bg-background/95 px-2 py-1 text-[10px] font-bold uppercase tracking-wider text-muted-foreground shadow-sm animate-pulse whitespace-nowrap md:mb-0 md:bg-transparent md:px-1 md:py-0 md:shadow-none">
                Editing Layout...
              </span>
            )}
            {!isEditMode && (
              <span className="hidden md:block text-[10px] uppercase tracking-wider text-muted-foreground font-bold px-1">Edit</span>
            )}
            <div className="rounded-md shadow-2xl md:shadow-none">
              <EditButton
                isEditMode={isEditMode}
                handleStartEditing={handleStartEditing}
                handleSaveEdit={handleSaveEdit}
                handleCancelEdit={handleCancelEdit}
                handleAddWidget={handleAddWidget}
              />
            </div>
          </div>
        </div>
      </div>

      {/* Time Period Group */}
      <div className="w-fit h-full flex flex-col items-start justify-start">
        <span className="hidden md:block text-[10px] uppercase tracking-wider text-muted-foreground font-bold px-1">TIME PERIOD</span>
        <TimePeriodSelector date={dateRange} onDateChange={onDateRangeChange} />
      </div>
    </div>
  );
}
