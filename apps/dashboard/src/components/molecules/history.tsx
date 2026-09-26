"use client";

import { useEffect, useState } from "react";
import {
    createColumnHelper,
    flexRender,
    getCoreRowModel,
    useReactTable,
} from "@tanstack/react-table";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/atoms/table";
import { Button } from "@/components/atoms/button";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import type { AiVersionSummary } from "@/features/dashboard/types/agentic";
import ApplyDashboardDialog from "@/features/dashboard/components/agenticdash/applyDashboardDialog";

const colHelper = createColumnHelper<AiVersionSummary>();

const columns = [
    colHelper.display({
        id: "version",
        header: "Dashboard",
        cell: (info) => {
            const version = info.row.original;
            return (
                <div className="min-w-0">
                    <span className="font-medium text-muted-foreground truncate max-w-[180px] block">
                        {version.version === 0
                            ? "Original dashboard"
                            : `Version ${version.version}`}
                    </span>
                    <span className="text-xs text-muted-foreground truncate max-w-[180px] block">
                        {version.title}
                    </span>
                </div>
            );
        },
    }),
    colHelper.accessor("createdAt", {
        header: "Date",
        cell: (info) => {
            const val = info.getValue();
            if (!val) return <span className="text-muted-foreground">-</span>;
            const date = new Date(val);
            return (
                <span className="text-muted-foreground text-xs">
                    {date.toLocaleDateString()} {date.toLocaleTimeString()}
                </span>
            );
        },
    }),
];

export default function History() {
    const sessionId = useDashboardStore((state) => state.sessionId);
    const versions = useDashboardStore((state) => state.versions);
    const currentVersionId = useDashboardStore((state) => state.currentVersionId);
    const { fetchVersions, switchVersion, applyDashboard } = useDashboardStore(
        (state) => state.agenticActions
    );
    const [applyDialogOpen, setApplyDialogOpen] = useState(false);
    const [isApplying, setIsApplying] = useState(false);

    useEffect(() => {
        if (sessionId) {
            fetchVersions();
        }
    }, [sessionId, fetchVersions]);

    const table = useReactTable({
        data: versions,
        columns,
        getCoreRowModel: getCoreRowModel(),
    });

    const selectedVersion = versions.find((version) => version.versionId === currentVersionId);
    const canApply = selectedVersion !== undefined && selectedVersion.version > 0;

    const handleApply = async (mode: "REPLACE_STARTED_DASHBOARD" | "CREATE_NEW_DASHBOARD") => {
        if (!selectedVersion) return;

        setIsApplying(true);
        try {
            const applied = await applyDashboard(selectedVersion.versionId, mode);
            if (applied) {
                setApplyDialogOpen(false);
            }
        } finally {
            setIsApplying(false);
        }
    };

    if (!sessionId) {
        return (
            <div className="h-full w-full flex items-center justify-center border rounded-lg text-center text-muted-foreground text-sm">
                Start an AI session to view version history and drafts.
            </div>
        );
    }

    return (
        <>
            <div className="h-full w-full flex flex-col overflow-hidden border rounded-lg">
                <div className="flex-1 min-h-0 overflow-y-auto">
                    <Table>
                        <TableHeader className="sticky top-0 bg-background z-10 shadow-sm">
                            {table.getHeaderGroups().map((headerGroup) => (
                                <TableRow key={headerGroup.id}>
                                    {headerGroup.headers.map((header) => (
                                        <TableHead key={header.id}>
                                            {header.isPlaceholder
                                                ? null
                                                : flexRender(
                                                      header.column.columnDef.header,
                                                      header.getContext()
                                                  )}
                                        </TableHead>
                                    ))}
                                </TableRow>
                            ))}
                        </TableHeader>
                        <TableBody>
                            {table.getRowModel().rows?.length ? (
                                table.getRowModel().rows.map((row) => {
                                    const isSelected = row.original.versionId === currentVersionId;
                                    return (
                                        <TableRow
                                            key={row.id}
                                            data-state={isSelected && "selected"}
                                            className={`cursor-pointer transition-colors ${
                                                isSelected ? "bg-muted/80 font-semibold" : ""
                                            }`}
                                            onClick={() => switchVersion(row.original.versionId)}
                                        >
                                            {row.getVisibleCells().map((cell) => (
                                                <TableCell key={cell.id}>
                                                    {flexRender(
                                                        cell.column.columnDef.cell,
                                                        cell.getContext()
                                                    )}
                                                </TableCell>
                                            ))}
                                        </TableRow>
                                    );
                                })
                            ) : (
                                <TableRow>
                                    <TableCell
                                        colSpan={columns.length}
                                        className="h-24 text-center"
                                    >
                                        No history found.
                                    </TableCell>
                                </TableRow>
                            )}
                        </TableBody>
                    </Table>
                </div>

                {canApply && selectedVersion && (
                    <div className="border-t bg-background p-3 flex items-center justify-between gap-3">
                        <div className="min-w-0">
                            <span className="text-sm font-medium block truncate">
                                {selectedVersion.title}
                            </span>
                            <span className="text-xs text-muted-foreground">
                                Version {selectedVersion.version} is active
                            </span>
                        </div>
                        <Button
                            size="sm"
                            onClick={() => setApplyDialogOpen(true)}
                            disabled={isApplying}
                        >
                            Apply
                        </Button>
                    </div>
                )}
            </div>

            {canApply && selectedVersion && (
                <ApplyDashboardDialog
                    open={applyDialogOpen}
                    dashboardName={selectedVersion.title}
                    isApplying={isApplying}
                    onOpenChange={setApplyDialogOpen}
                    onApply={handleApply}
                />
            )}
        </>
    );
}
