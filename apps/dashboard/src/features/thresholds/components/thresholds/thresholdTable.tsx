"use client";

import { useMemo, useState } from "react";
import { Switch } from "@/components/atoms/switch";
import {
    ColumnDef,
    flexRender,
    getCoreRowModel,
    getSortedRowModel,
    SortingState,
    useReactTable,
    getPaginationRowModel,
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
import type { OperatorsForThreshold, Threshold } from "@/features/thresholds/types/thresholdTypes";
import { OPERATOR_LABEL, SEVERITY_COLOURS } from "@/features/thresholds/types/thresholdTypes";
import { ArrowUp, ArrowDown, Pencil, Trash2, MoreVertical } from "lucide-react";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/atoms/dropdown-menu";
import { useResourceNames } from "@/features/alerts/hooks/useResourceNames";
import { TablePagination } from "@/components/molecules/tablePagination";
import { Truncation } from "@/components/molecules/truncation";

const CONDITION_VERBS: Record<OperatorsForThreshold, string> = {
    GT: "More than",
    GTE: "More than or equal to",
    LT: "Less than",
    LTE: "Less than or equal to",
    EQ: "Equal to",
};
interface PropsForThresholds {
    thresholds: Threshold[];
    edit: (forThreshold: Threshold) => void;
    toggleEnabled: (forThreshold: Threshold, enabled: boolean) => void;
    onDelete: (forThreshold: Threshold) => void;
}

interface Columns {
    edit: (forThreshold: Threshold) => void;
    toggleEnabled: (forThreshold: Threshold, enabled: boolean) => void;
    onDelete: (forThreshold: Threshold) => void;
    resourceNames: Record<string, string>;
}

//copied from below to correct sonarqube errors
function helperForColumns({
    edit,
    toggleEnabled,
    onDelete,
    resourceNames,
}: Columns): ColumnDef<Threshold>[] {
    return [
        {
            id: "enabled",
            header: () => <span className = "block text-center"> ENABLED </span>,
            enableSorting: false,
            size: 200,
            cell: ({ row }) => {
                const forThreshold = row.original;

                return (
                    <div className = "flex items-center justify-center">
                        <Switch
                            checked={forThreshold.enabled}
                            onCheckedChange={(checked) => toggleEnabled(forThreshold, checked)}
                            aria-label={`Toggle ${forThreshold.metricName} threshold`}
                        />
                    </div>
                );
            },
        },

        {
            id: "resource",
            header: () => "RESOURCE",
            size: 180,
            enableSorting: false,
            cell: ({ row }) => {
                const forThreshold = row.original;

                const mutedThreshold = !forThreshold.enabled;

                const name = resourceNames[forThreshold.resourceId] ?? forThreshold.resourceId;

                return (
                    <Truncation
                        text={name}
                        className={`w-[120px] flex-shrink-0 ${mutedThreshold ? "text-muted-foreground" : "text-foreground"}`}
                    />
                );
            },
        },

        {
            accessorKey: "metricName",
            header: () => "METRIC",
            size: 180,
            cell: ({ row }) => {
                const forThreshold = row.original;

                const mutedThreshold = !forThreshold.enabled;

                return (
                    <Truncation
                        text={forThreshold.metricName}
                        className={`w-[100px] flex-shrink-0 ${mutedThreshold ? "text-muted-foreground" : "text-foreground"}`}
                    />
                );
            },
        },

        {
            id: "condition",
            header: () => "CONDITION",
            enableSorting: false,
            size: 140,
            cell: ({ row }) => {
                const forThreshold = row.original;

                const mutedThreshold = !forThreshold.enabled;

                const visibleCondition = `${OPERATOR_LABEL[forThreshold.operator]} ${forThreshold.value}`;

                const tooltip = `${CONDITION_VERBS[forThreshold.operator]} ${forThreshold.value}`;

                return (
                    <Truncation
                        text={visibleCondition}
                        tooltipText={tooltip}
                        className={mutedThreshold ? "text-muted-foreground" : "text-foreground"}
                    />
                );
            },
        },

        {
            accessorKey: "severity",
            header: () => "SEVERITY",
            size: 120,
            cell: ({ row }) => {
                const forThreshold = row.original;

                const mutedThreshold = !forThreshold.enabled;

                return (
                    <span
                        className={
                            mutedThreshold
                                ? "text-muted-foreground"
                                : SEVERITY_COLOURS[forThreshold.severity]
                        }
                    >
                        {" "}
                        {forThreshold.severity}{" "}
                    </span>
                );
            },
        },

        {
            accessorKey: "value",
            header: () => "VALUE",
            size: 100,
            cell: ({ row }) => {
                const forThreshold = row.original;

                const mutedThreshold = !forThreshold.enabled;

                return (
                    <span className={mutedThreshold ? "text-muted-foreground" : "text-foreground"}>
                        {" "}
                        {forThreshold.value}{" "}
                    </span>
                );
            },
        },

        {
            id: "actions",
            header: () => "",
            enableSorting: false,
            size: 100,
            cell: ({ row }) => {
                const forThreshold = row.original;

                return (
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="sm" className="h-8 w-8 p-0">
                                {" "}
                                <MoreVertical className="h-4 w-4" />{" "}
                            </Button>
                        </DropdownMenuTrigger>

                        <DropdownMenuContent align="end" className="w-36">
                            <DropdownMenuItem
                                onClick={() => edit(forThreshold)}
                                className="cursor-pointer"
                            >
                                {" "}
                                <Pencil className="mr-2 h-4 w-4" /> Edit{" "}
                            </DropdownMenuItem>

                            <DropdownMenuItem
                                onClick={() => onDelete(forThreshold)}
                                className="cursor-pointer text-destructive focus:text-destructive"
                            >
                                {" "}
                                <Trash2 className="mr-2 h-4 w-4" /> Delete{" "}
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                );
            },
        },
    ];
}

export function ThresholdTable({
    thresholds,
    edit,
    toggleEnabled,
    onDelete,
}: Readonly<PropsForThresholds>) {
    const [sorting, setSorting] = useState<SortingState>([]);

    const resourceNames = useResourceNames();

    const forColumns = useMemo<ColumnDef<Threshold>[]>(
        () => helperForColumns({ edit, toggleEnabled, onDelete, resourceNames }),
        [edit, toggleEnabled, onDelete, resourceNames]
    );

    const forThresholdTable = useReactTable({
        data: thresholds,
        columns: forColumns,
        state: { sorting },
        onSortingChange: setSorting,
        getCoreRowModel: getCoreRowModel(),
        getSortedRowModel: getSortedRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        initialState: { pagination: { pageSize: 10 } },
    });

    return (
        <div className="overflow-hidden rounded-lg border border-border bg-card text-card-foreground">
            <Table className="table-fixed">
                <TableHeader>
                    {forThresholdTable.getHeaderGroups().map((headerGroup) => (
                        <TableRow key={headerGroup.id} className="hover:bg-transparent">
                            {headerGroup.headers.map((header) => {
                                const ableToSort = header.column.getCanSort();

                                const sorted = header.column.getIsSorted();

                                return (
                                    <TableHead
                                        key={header.id}
                                        className="text-xs font-semibold uppercase tracking-wider text-muted-foreground"
                                        style={{ width: header.getSize() }}
                                    >
                                        {ableToSort ? (
                                            <button
                                                type="button"
                                                onClick={header.column.getToggleSortingHandler()}
                                                className="inline-flex items-center gap-1 rounded-sm uppercase tracking-wider hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring focus-visible:ring-offset-2 focus-visible:ring-offset-background"
                                            >
                                                {flexRender(
                                                    header.column.columnDef.header,
                                                    header.getContext()
                                                )}

                                                {sorted === "asc" && (
                                                    <ArrowUp className="h-3.5 w-3.5" />
                                                )}

                                                {sorted === "desc" && (
                                                    <ArrowDown className="h-3.5 w-3.5" />
                                                )}
                                            </button>
                                        ) : (
                                            flexRender(
                                                header.column.columnDef.header,
                                                header.getContext()
                                            )
                                        )}
                                    </TableHead>
                                );
                            })}
                        </TableRow>
                    ))}
                </TableHeader>

                <TableBody>
                    {forThresholdTable.getRowModel().rows.length === 0 ? (
                        <TableRow>
                            <TableCell
                                colSpan={forColumns.length}
                                className="py-8 text-center text-sm text-muted-foreground"
                            >
                                {" "}
                                No thresholds found.{" "}
                            </TableCell>
                        </TableRow>
                    ) : (
                        forThresholdTable.getRowModel().rows.map((row) => (
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

            <TablePagination forTable={forThresholdTable} />
        </div>
    );
}
