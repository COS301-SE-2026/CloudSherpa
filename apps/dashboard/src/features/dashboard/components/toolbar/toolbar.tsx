"use client";

import { TimePeriodSelector } from "@/features/dashboard/components/toolbar/timePeriodSelector";
import { DashboardSelector } from "@/features/dashboard/components/toolbar/dashboardSelector";
import { DateRange } from "react-day-picker";
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
    <header className="flex h-16 shrink-0 items-center justify-between px-4 transition-[width,height] ease-linear group-has-data-[collapsible=icon]/sidebar-wrapper:h-12 bg-red-500">
      <div className="flex items-center gap-3 md:gap-4 shrink-0">
        <DashboardSelector
          dashboards={dashboards}
          selectedId={selectedDashboardId}
          onSelect={onDashboardChange}
          onCreate={onCreateDashboard}
        />
        <EditButton
          isEditMode={isEditMode}
          handleStartEditing={handleStartEditing}
          handleSaveEdit={handleSaveEdit}
          handleCancelEdit={handleCancelEdit}
          handleAddWidget={handleAddWidget}
        />
      </div>
      <div className="flex items-center">
        <TimePeriodSelector date={dateRange} onDateChange={onDateRangeChange} />
      </div>
    </header>
  );
}
