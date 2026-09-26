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
import { Pencil, Trash2, MoreVertical } from "lucide-react";
import type { Budget } from "@/features/budgets/types/budgetTypes";
import { LABELS_FOR_SCOPE } from "@/features/budgets/types/budgetTypes";
import { TableHeaderData } from "@/features/alerts/components/atoms/tableHeaderData";
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger} from "@/components/atoms/dropdown-menu";
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
                        {LABELS_FOR_SCOPE[row.original.scope]}
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
                    {row.original.window_days} {row.original.window_days === 1 ? "day" : "days"}
                </span>
            ),
        },

        {
            id: "actions",
            header: () => "ACTIONS",
            enableSorting: false,
            cell: ({ row }) => {
                const forBudget = row.original;

                return(
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button variant = "ghost" size = "sm" className = "h-8 w-8 p-0"> <MoreVertical className = "h-4 w-4"/> </Button>
                        </DropdownMenuTrigger>

                        <DropdownMenuContent align = "end" className = "w-36">
                            <DropdownMenuItem onClick = {() => edit(forBudget)} className = "cursor-pointer"> <Pencil className = "mr-2 h-4 w-4"/> Edit </DropdownMenuItem>

                            <DropdownMenuItem onClick = {() => onDelete(forBudget)} className = "cursor-pointer text-destructive focus:text-destructive"> <Trash2 className = "mr-2 h-4 w-4"/> Delete </DropdownMenuItem>
                        </DropdownMenuContent>

                    </DropdownMenu>
                );
            },
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
                <TableHeaderData headerGroups={tableForBudgets.getHeaderGroups()} />

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
