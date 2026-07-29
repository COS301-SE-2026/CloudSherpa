"use client";

import { TimePeriodSelector } from "@/features/dashboard/components/toolbar/timePeriodSelector";
import { DashboardSelector } from "@/features/dashboard/components/toolbar/dashboardSelector";
import { DateRange } from "react-day-picker";
import { DashboardStub } from "@/features/dashboard/types/widgets";
import EditButton from "@/features/dashboard/components/toolbar/editButton";
import { HelpMenu } from "@/features/helpMenu/helpMenu";

interface ToolbarProps {
    dashboards: DashboardStub[];
    isEditMode: boolean;
    handleAddWidget: () => void;
    handleAddKpi: () => void;
    handleStartEditing: () => void;
    handleSaveEdit: () => void;
    handleCancelEdit: () => void;
    selectedDashboardId: string;
    onDashboardChange: (id: string) => void;
    onCreateDashboard: (name: string) => void;
    onDeleteDashboard: (id: string) => void;
    dateRange: DateRange | undefined;
    onDateRangeChange: (range: DateRange | undefined) => void;
    hasActiveDashboard: boolean;
}

export default function Toolbar({
    dashboards,
    isEditMode,
    hasActiveDashboard,
    handleAddWidget,
    handleAddKpi,
    handleStartEditing,
    handleSaveEdit,
    handleCancelEdit,
    selectedDashboardId,
    onDashboardChange,
    onCreateDashboard,
    onDeleteDashboard,
    dateRange,
    onDateRangeChange,
}: Readonly<ToolbarProps>) {
    return (
        <header className="h-16 flex flex-row items-center justify-between px-6 group-data-[collapsible=icon]:justify-center group-data-[collapsible=icon]:px-0">
            <div className="flex flex-row gap-2">
                <DashboardSelector
                    dashboards={dashboards}
                    selectedId={selectedDashboardId}
                    onSelect={onDashboardChange}
                    onCreate={onCreateDashboard}
                    onDelete={onDeleteDashboard}
                />

                {hasActiveDashboard && (
                    <EditButton
                        isEditMode={isEditMode}
                        handleStartEditing={handleStartEditing}
                        handleSaveEdit={handleSaveEdit}
                        handleCancelEdit={handleCancelEdit}
                        handleAddWidget={handleAddWidget}
                        handleAddKpi={handleAddKpi}
                    />
                )}
            </div>

            {hasActiveDashboard && (
                <div className="flex flex-row items-center gap-2">
                    <TimePeriodSelector date={dateRange} onDateChange={onDateRangeChange} />
                <HelpMenu />
                </div>
            )}
        </header>
    );
}
