"use client";

import { TimePeriodSelector } from "@/features/dashboard/components/toolbar/timePeriodSelector";
import { DashboardSelector } from "@/features/dashboard/components/toolbar/dashboardSelector";
import { DateRange } from "react-day-picker";
import { DashboardStub } from "@/features/dashboard/types/widgets";
import EditButton from "@/features/dashboard/components/toolbar/editButton";
import { HelpMenu } from "@/features/helpMenu/helpMenu";
import AddWidget from "@/features/dashboard/components/toolbar/addWidget";
import AgenticDashCard from "@/features/dashboard/components/agenticdash/agenticDashCard";

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
        <header className="sticky top-0 z-50 w-full flex flex-col items-center group-data-[collapsible=icon]:justify-center group-data-[collapsible=icon]:px-0 pt-3 pb-2 relative">
            <div
                className="pb-30 pointer-events-none absolute inset-0 z-[-1] bg-gradient-to-b from-background/90 via-background/60 to-transparent backdrop-blur-md"
                style={{
                    maskImage: "linear-gradient(to bottom, black 60%, transparent 100%)",
                    WebkitMaskImage: "linear-gradient(to bottom, black 60%, transparent 100%)",
                }}
            />
            <div className="h-16 w-full flex flex-row items-center justify-between  px-6">
                <div className="flex flex-row gap-2">
                    <DashboardSelector
                        dashboards={dashboards}
                        selectedId={selectedDashboardId}
                        onSelect={onDashboardChange}
                        onCreate={onCreateDashboard}
                        onDelete={onDeleteDashboard}
                    />
                </div>

                {hasActiveDashboard && (
                    <div className="flex flex-row items-center gap-2">
                        {hasActiveDashboard && (
                            <>
                                <AgenticDashCard />
                                <div className="hidden sm:block">
                                    <EditButton
                                        isEditMode={isEditMode}
                                        handleStartEditing={handleStartEditing}
                                        handleSaveEdit={handleSaveEdit}
                                        handleCancelEdit={handleCancelEdit}
                                    />
                                </div>
                                <AddWidget
                                    handleAddWidget={handleAddWidget}
                                    handleAddKpi={handleAddKpi}
                                />
                            </>
                        )}
                        <TimePeriodSelector date={dateRange} onDateChange={onDateRangeChange} />
                        <HelpMenu />
                    </div>
                )}
            </div>
            <div className="w-full flex flex-row items-center justify-start  px-6 sm:hidden">
                {hasActiveDashboard && (
                    <EditButton
                        isEditMode={isEditMode}
                        handleStartEditing={handleStartEditing}
                        handleSaveEdit={handleSaveEdit}
                        handleCancelEdit={handleCancelEdit}
                    />
                )}
            </div>
        </header>
    );
}
