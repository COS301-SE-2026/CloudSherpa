"use client";

import { TimePeriodSelector } from "@/features/dashboard/components/toolbar/timePeriodSelector";
import { DashboardSelector } from "@/features/dashboard/components/toolbar/dashboardSelector";
import { DateRange } from "react-day-picker";
import { DashboardStub } from "@/features/dashboard/types/widgets";
import EditButton from "@/features/dashboard/components/toolbar/editButton";
import { HelpMenu } from "@/features/helpMenu/helpMenu";
import AddWidget from "@/features/dashboard/components/toolbar/addWidget";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import AgenticDashCard from "@/features/dashboard/components/agenticdash/agenticDashCard";
import { cn } from "@/lib/utils";
import { Button } from "@/components/atoms/button";
import { X } from "lucide-react";
import ApplyDashboardDialog from "@/features/dashboard/components/agenticdash/applyDashboardDialog";
import type { AiDashboardApplyMode } from "@/features/dashboard/types/agentic";
import { useToolbar } from "@/features/dashboard/components/toolbar/toolbarProvider";
import { useState } from "react";
import { Card, CardContent } from "@/components/atoms/card";

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
    const isSessionActive = useDashboardStore((state) => state.isSessionActive);
    const isGenerating = useDashboardStore((state) => state.isGenerating);
    const currentVersionId = useDashboardStore((state) => state.currentVersionId);
    const versions = useDashboardStore((state) => state.versions);
    const { isApplyDialogOpen, setIsApplyDialogOpen } = useToolbar();
    const [isApplying, setIsApplying] = useState(false);

    const { applyDashboard, cancelSession } = useDashboardStore((state) => state.agenticActions);

    const currentVersion = versions.find((version) => version.versionId === currentVersionId);

    const canApplyCurrentVersion =
        isSessionActive && currentVersion !== undefined && currentVersion.version > 0;

    const hideTools = hasActiveDashboard || isSessionActive;

    const handleApply = async (mode: AiDashboardApplyMode): Promise<void> => {
        if (!currentVersionId) {
            return;
        }

        setIsApplying(true);

        try {
            const applied = await applyDashboard(currentVersionId, mode);

            if (applied) {
                setIsApplyDialogOpen(false);
            }
        } finally {
            setIsApplying(false);
        }
    };
    return (
        <>
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
                        {!(isSessionActive || isGenerating) ? (
                            <DashboardSelector
                                dashboards={dashboards}
                                selectedId={selectedDashboardId}
                                onSelect={onDashboardChange}
                                onCreate={onCreateDashboard}
                                onDelete={onDeleteDashboard}
                            />
                        ) : (
                            <div className="flex flex-row justify-start items-center gap-2">
                                <h1
                                    className={cn(
                                        // isGenerating && "animate-pulse",
                                        "text-sm font-semibold text-foreground bg-card border py-3 px-6 rounded-md"
                                    )}
                                >
                                    AI Dashboard Preview
                                </h1>
                                <Card className="py-0 h-full flex flex-row justify-center items-center rounded-md">
                                    <CardContent className=" flex flex-row justify-center items-center gap-4 pr-2">
                                        <span className="text-muted-foreground text-sm font-medium">
                                            {currentVersion?.version === 0
                                                ? "Viewing the dashboard you started with"
                                                : `Viewing version v${currentVersion?.version ?? "0"}`}
                                        </span>
                                        <div className="flex flex-row justify-center items-center gap-1">
                                            {isSessionActive && (
                                                <Button
                                                    size="sm"
                                                    variant="destructive"
                                                    onClick={() => cancelSession()}
                                                    disabled={isGenerating || isApplying}
                                                >
                                                    <X />
                                                    Discard
                                                </Button>
                                            )}
                                            {canApplyCurrentVersion && (
                                                <Button
                                                    size="sm"
                                                    onClick={() => setIsApplyDialogOpen(true)}
                                                    disabled={isGenerating || isApplying}
                                                >
                                                    Save Dashboard
                                                </Button>
                                            )}
                                        </div>
                                    </CardContent>
                                </Card>
                            </div>
                        )}
                    </div>
                    {hideTools && (
                        <div className="flex flex-row items-center gap-2">
                            <AgenticDashCard isApplying={isApplying} />

                            {!(isSessionActive || isGenerating) && (
                                <>
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
                                        isEditMode={isEditMode}
                                    />
                                </>
                            )}

                            <TimePeriodSelector date={dateRange} onDateChange={onDateRangeChange} />

                            <HelpMenu />
                        </div>
                    )}{" "}
                </div>
                <div className="w-full flex flex-row items-center justify-start  px-6 sm:hidden">
                    {hasActiveDashboard && !isSessionActive && (
                        <EditButton
                            isEditMode={isEditMode}
                            handleStartEditing={handleStartEditing}
                            handleSaveEdit={handleSaveEdit}
                            handleCancelEdit={handleCancelEdit}
                        />
                    )}
                </div>
            </header>
            {canApplyCurrentVersion && currentVersion && (
                <ApplyDashboardDialog
                    open={isApplyDialogOpen}
                    dashboardName={currentVersion.title}
                    isApplying={isApplying}
                    onOpenChange={setIsApplyDialogOpen}
                    onApply={handleApply}
                />
            )}
        </>
    );
}
