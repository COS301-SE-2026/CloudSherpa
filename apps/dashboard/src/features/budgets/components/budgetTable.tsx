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
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/atoms/table";
import { ArrowUp, ArrowDown, Pencil, Trash2 } from "lucide-react";
import type { Budget } from "@/features/budgets/types/budgetTypes";

interface PropsForBudget {
    budgets: Budget[];
    edit: (budget: Budget) => void;
    toggleEnabled: (budget: Budget, enabled: boolean) => void;
    onDelete: (budget: Budget) => void;
}

interface Columns {
    edit: (budget: Budget) => void;
    toggleEnabled: (budget: Budget, enabled: boolean) => void;
    onDelete: (budget: Budget) => void;
}

function helperForColumns({ edit, toggleEnabled, onDelete }: Columns): ColumnDef<Budget>[] {
    return [
        {
            id: "enabled",
            header: () => "ENABLED",
            enableSorting: false,
            cell: ({ row }) => (
                <Switch
                    checked={row.original.enabled}
                    onCheckedChange={(checked) => toggleEnabled(row.original, checked)}
                />
            ),
        },

        {
            accessorKey: "scope",
            header: () => "SCOPE",
            cell: ({ row }) => {
                const mutedBudget = !row.original.enabled;

                return (
                    <span className={mutedBudget ? "text-muted-foreground" : "text-foreground"}>
                        {" "}
                        {row.original.scope}{" "}
                    </span>
                );
            },
        },

        {
            accessorKey: "budget",
            header: () => "BUDGET",
            cell: ({ row }) => {
                const mutedBudget = !row.original.enabled;

                const amount = row.original;

                return (
                    <span className={mutedBudget ? "text-muted-foreground" : "text-foreground"}>
                        {" "}
                        {amount.currency} {amount.amount.toFixed(2)}{" "}
                    </span>
                );
            },
        },

        {
            accessorKey: "window_days",
            header: () => "WINDOW",
            cell: ({ row }) => (
                <span className={!row.original.enabled ? "text-muted-foreground" : ""}>
                    {" "}
                    {row.original.window_days}{" "}
                </span>
            ),
        },

        {
            id: "actions",
            header: () => "ACTIONS",
            enableSorting: false,
            cell: ({ row }) => (
                <div className="flex items-center gap-2">
                    <Button
                        variant="ghost"
                        size="sm"
                        className="h-auto p-0 text-primary"
                        onClick={() => edit(row.original)}
                    >
                        {" "}
                        <Pencil className="h-4 w-4" />{" "}
                    </Button>

                    <Button
                        variant="ghost"
                        size="sm"
                        className="h-auto p-0 text-destructive"
                        onClick={() => onDelete(row.original)}
                    >
                        {" "}
                        <Trash2 className="h-4 w-4" />{" "}
                    </Button>
                </div>
            ),
        },
    ];
}

export function BudgetTable({ budgets, edit, toggleEnabled, onDelete }: Readonly<PropsForBudget>) {
    const [sorting, setSorting] = useState<SortingState>([]);

    const forColumns = useMemo(
        () => helperForColumns({ edit, toggleEnabled, onDelete }),
        [edit, toggleEnabled, onDelete]
    );

    const tableForBudgets = useReactTable({
        data: budgets,
        columns: forColumns,
        state: { sorting },
        onSortingChange: setSorting,
        getCoreRowModel: getCoreRowModel(),
        getSortedRowModel: getSortedRowModel(),
    });

    return (
        <div className="overflow-hidden rounded-lg border border-border bg-card text-card-foreground">
            <Table>
                <TableHeader>
                    {tableForBudgets.getHeaderGroups().map((forHeaderGroups) => (
                        <TableRow key={forHeaderGroups.id} className="hover:bg-transparent">
                            {forHeaderGroups.headers.map((header) => {
                                const ableToSort = header.column.getCanSort();

                                const sorted = header.column.getIsSorted();

                                return (
                                    <TableHead
                                        key={header.id}
                                        className="text-xs font-semibold uppercase tracking-wider text-muted-foreground"
                                    >
                                        {ableToSort ? (
                                            <button
                                                type="button"
                                                onClick={header.column.getToggleSortingHandler()}
                                                className="inline-flex items-center gap-1 rounded-sm uppercase tracking-wider hover:text-foreground"
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
                    {tableForBudgets.getRowModel().rows.length === 0 ? (
                        <TableRow>
                            <TableCell
                                colSpan={forColumns.length}
                                className="py-8 text-center text-sm text-muted-foreground"
                            >
                                {" "}
                                No budgets configures{" "}
                            </TableCell>
                        </TableRow>
                    ) : (
                        tableForBudgets.getRowModel().rows.map((row) => (
                            <TableRow key={row.id}>
                                {row.getVisibleCells().map((forCells) => (
                                    <TableCell key={forCells.id} className="text-sm">
                                        {flexRender(
                                            forCells.column.columnDef.cell,
                                            forCells.getContext()
                                        )}
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
