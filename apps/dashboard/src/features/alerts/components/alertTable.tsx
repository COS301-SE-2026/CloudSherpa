"use client";

import { useMemo, useState } from "react";
import {
    ColumnDef,
    flexRender,
    getCoreRowModel,
    getSortedRowModel,
    SortingState,
    useReactTable,
} from "@tanstack/react-table";
import { Button } from "@/components/atoms/button";
import { Switch } from "@/components/atoms/switch";
import { Table, TableBody, TableCell, TableRow } from "@/components/atoms/table";
import { Info } from "lucide-react";
import { SEVERITY_COLOURS, STATUS_LABELS, TYPE } from "@/features/alerts/types/alertTypes";
import type { Alert } from "@/features/alerts/types/alertTypes";
import { toast } from "sonner";
import { TableHeaderData } from "@/features/alerts/components/atoms/tableHeaderData";

interface PropsForAlertsTable {
    alerts: Alert[];
    onToggle: (alert: Alert, enabled: boolean) => Promise<void>;
    info: (alert: Alert) => void;
}

interface ForColumns {
    onToggle: (alert: Alert, enabled: boolean) => Promise<void>;
    info: (alert: Alert) => void;
}

function cleanAlertTitle(title: string): string {
    const cleaned = title.trim();

    if (!cleaned.endsWith(")")) {
        return cleaned;
    }

    const tagStartIndex = cleaned.toLowerCase().lastIndexOf("(z=");

    if (tagStartIndex !== -1) {
        const insideTag = cleaned.slice(tagStartIndex + 3, -1);

        if (insideTag.length > 0 && !insideTag.includes(")")) {
            return cleaned.slice(0, tagStartIndex).trim();
        }
    }

    return cleaned;
}

function helperForColumns({ onToggle, info }: ForColumns): ColumnDef<Alert>[] {
    return [
        {
            id: "enabled",
            header: () => "STATUS",
            enableSorting: false,
            cell: ({ row }) => {
                const active = row.original.status === "ACTIVE";

                const handlingToggle = async (checked: boolean) => {
                    try {
                        await onToggle(row.original, checked);

                        toast.success(checked ? "Alert enabled" : "Alert disabled");
                    } catch {
                        toast.error(checked ? "Failed to enable alert" : "Failed to disable alert");
                    }
                };

                return (
                    <div className="flex items-center gap-2">
                        <Switch checked={active} onCheckedChange={handlingToggle} />

                        <span className="text-xs text-muted-foreground">
                            {" "}
                            {STATUS_LABELS[row.original.status]}{" "}
                        </span>
                    </div>
                );
            },
        },

        {
            accessorKey: "severity",
            header: () => "SEVERITY",
            cell: ({ row }) => {
                const inactive = row.original.status !== "ACTIVE";

                const colours = inactive
                    ? "text-muted-foreground"
                    : SEVERITY_COLOURS[row.original.severity];

                return (
                    <span className={`text-xs font-semibold uppercase tracking wider ${colours}`}>
                        {" "}
                        {row.original.severity}{" "}
                    </span>
                );
            },
        },

        {
            accessorKey: "alertType",
            header: () => "TYPE",
            cell: ({ row }) => (
                <span className="text-xs text-muted-foreground">
                    {" "}
                    {TYPE[row.original.alertType]}{" "}
                </span>
            ),
        },

        {
            accessorKey: "title",
            header: () => "ALERT",
            cell: ({ row }) => {
                const inactive = row.original.status !== "ACTIVE";
                const displayTitle = cleanAlertTitle(row.original.title);

                return (
                    <span className={inactive ? "text-muted-foreground" : "text-foreground"}>
                        {" "}
                        {displayTitle}{" "}
                    </span>
                );
            },
        },

        {
            accessorKey: "lastSeen",
            header: () => "UPDATED AT",
            cell: ({ row }) => {
                const updatedAt = row.original.lastSeen ?? row.original.createdAt;
                return (
                    <span className="text-xs text-muted-foreground">
                        {" "}
                        {updatedAt ? new Date(updatedAt).toLocaleString() : "-"}{" "}
                    </span>
                );
            },
        },

        {
            id: "info",
            header: () => "",
            enableSorting: false,
            cell: ({ row }) => (
                <Button
                    variant="ghost"
                    size="sm"
                    className="h-auto p-0 text-muted-foreground hover:text-foreground"
                    onClick={() => info(row.original)}
                >
                    {" "}
                    <Info className="h-4 w-4" />{" "}
                </Button>
            ),
        },
    ];
}

export function AlertTable({ alerts, onToggle, info }: Readonly<PropsForAlertsTable>) {
    const [sorting, setSorting] = useState<SortingState>([]);

    const columns = useMemo(() => helperForColumns({ onToggle, info }), [onToggle, info]);

    const tableForAlerts = useReactTable({
        data: alerts,
        columns,
        state: { sorting },
        onSortingChange: setSorting,
        getCoreRowModel: getCoreRowModel(),
        getSortedRowModel: getSortedRowModel(),
    });

    return (
        <div className="overflow-hidden rounded-lg border border-border bg-card text-card-foreground">
            <Table>
                <TableHeaderData headerGroups={tableForAlerts.getHeaderGroups()} />

                <TableBody>
                    {tableForAlerts.getRowModel().rows.length === 0 ? (
                        <TableRow>
                            <TableCell
                                colSpan={columns.length}
                                className="py-8 text-center text-sm text-muted-foreground"
                            >
                                {" "}
                                No alerts found{" "}
                            </TableCell>
                        </TableRow>
                    ) : (
                        tableForAlerts.getRowModel().rows.map((row) => (
                            <TableRow key={row.id}>
                                {row.getVisibleCells().map((cell) => (
                                    <TableCell key={cell.id} className="text-sm">
                                        {" "}
                                        {flexRender(
                                            cell.column.columnDef.cell,
                                            cell.getContext()
                                        )}{" "}
                                    </TableCell>
                                ))}
                            </TableRow>
                        ))
                    )}
                </TableBody>
            </Table>
        </div>
    );
}
