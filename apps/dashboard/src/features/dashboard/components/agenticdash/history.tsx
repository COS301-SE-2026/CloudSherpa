"use client";

import { useMemo, useEffect } from "react";
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
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import { AiVersionSummary } from "@/features/dashboard/types/agentic";

const colHelper = createColumnHelper<AiVersionSummary>();

export default function History() {
    const sessionId = useDashboardStore((state) => state.sessionId);
    const versions = useDashboardStore((state) => state.versions);
    const currentVersionId = useDashboardStore((state) => state.currentVersionId);
    const { fetchVersions, switchVersion } = useDashboardStore((state) => state.agenticActions);

    useEffect(() => {
        if (sessionId) {
            fetchVersions();
        }
    }, [sessionId, fetchVersions]);

    const columns = useMemo(
        () => [
            colHelper.accessor("version", {
                header: "Dashboard",
                cell: (info) => (
                    <span className="font-medium text-muted-foreground truncate max-w-[150px] block">
                        {info.getValue()}
                    </span>
                ),
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
        ],
        []
    );

    //init
    const table = useReactTable({
        data: versions,
        columns,
        getCoreRowModel: getCoreRowModel(),
    });

    if (!sessionId) {
        return (
            <div className="h-full w-full flex items-center justify-center border rounded-lg text-center text-muted-foreground text-sm">
                Start an AI session to view version history and drafts.
            </div>
        );
    }

    return (
        <div className="h-full w-full overflow-y-auto border rounded-lg">
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
                            <TableCell colSpan={columns.length} className="h-24 text-center">
                                No history found.
                            </TableCell>
                        </TableRow>
                    )}
                </TableBody>
            </Table>
        </div>
    );
}
