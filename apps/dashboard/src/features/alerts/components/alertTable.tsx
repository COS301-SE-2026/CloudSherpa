"use client";

import {useMemo, useState} from "react";
import {ColumnDef, flexRender, getCoreRowModel, getSortedRowModel, SortingState, useReactTable} from "@tanstack/react-table";
import {Button} from "@/components/atoms/button";
import {Switch} from "@/components/atoms/switch";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/atoms/table";
import {ArrowUp, ArrowDown, Info} from "lucide-react";
import { SEVERITY_COLOURS, STATUS_LABELS, TYPE } from "@/features/alerts/types/alertTypes";
import type {Alert} from "@/features/alerts/types/alertTypes";
import {toast} from "sonner";

interface PropsForAlertsTable{
    alerts : Alert[];
    onToggle : (alert : Alert, enabled : boolean) => Promise<void>;
    info : (alert : Alert) => void;
}

interface ForColumns{
    onToggle : (alert : Alert, enabled : boolean) => Promise<void>;
    info : (alert : Alert) => void;
}

function helperForColumns({onToggle, info} : ForColumns) : ColumnDef<Alert>[]{
    return[
        {id : "enabled", header : () => "STATUS", enableSorting : false, cell : ({row}) => {
            const active = row.original.status === "ACTIVE";

            const handlingToggle = async (checked : boolean) => {
                try{
                    await onToggle(row.original, checked);

                    toast.success(checked ? "Alert enabled" : "Alert disabled");
                }catch{
                    toast.error(checked ? "Failed to enable alert" : "Failed to disable alert");
                }
            };

            return(
                <div className = "flex items-center gap-2">
                    <Switch checked = {active} onCheckedChange = {handlingToggle}/>

                    <span className = "text-xs text-muted-foreground"> {STATUS_LABELS[row.original.status]} </span>
                </div>
            );
        },},

        {accessorKey : "severity", header : () => "SEVERITY", cell : ({row}) => {
            const inactive = row.original.status !== "ACTIVE";

            const colours = inactive ? "text-muted-foreground" : SEVERITY_COLOURS[row.original.severity];

            return(
                <span className = {`text-xs font-semibold uppercase tracking wider ${colours}`}> {row.original.severity} </span>
            );
        },},

        {accessorKey : "alertType", header : () => "TYPE", cell : ({row}) => (
            <span className = "text-xs text-muted-foreground"> {TYPE[row.original.alertType]} </span>
        ),},

        {accessorKey : "title", header : () => "ALERT", cell : ({row}) => {
            const inactive = row.original.status !== "ACTIVE";

            return(
                <span className = {inactive ? "text-muted-foreground" : "text-foreground"}> {row.original.title} </span>
            );
        },},

        {accessorKey : "createdAt", header : () => "CREATED", cell : ({row}) => (
            <span className = "text-xs text-muted-foreground"> {row.original.createdAt ? new Date(row.original.createdAt).toLocaleString() : "-"} </span>
        ),},

        {id : "info", header : () => "", enableSorting : false, cell : ({row}) => (
            <Button variant = "ghost" size = "sm" className = "h-auto p-0 text-muted-foreground hover:text-foreground" onClick = {() => info(row.original)}> <Info className = "h-4 w-4"/> </Button>
        ),},
    ];
}

export function AlertTable({alerts, onToggle, info} : Readonly<PropsForAlertsTable>){
    const [sorting, setSorting] = useState<SortingState>([]);

    const columns = useMemo(() => helperForColumns({onToggle, info}), [onToggle, info],);

    const tableForAlerts = useReactTable({
        data : alerts, columns, state : {sorting}, onSortingChange : setSorting, getCoreRowModel : getCoreRowModel(), getSortedRowModel : getSortedRowModel(),
    });

    return(
        <div className = "overflow-hidden rounded-lg border border-border bg-card text-card-foreground">
            <Table>
                <TableHeader>
                    {tableForAlerts.getHeaderGroups().map((forHeaderGroups) => (
                        <TableRow key = {forHeaderGroups.id} className = "hover:bg-transparent">
                            {forHeaderGroups.headers.map((header) => {
                                const ableToSort = header.column.getCanSort();

                                const sorted = header.column.getIsSorted();

                                return(
                                    <TableHead key = {header.id} className = "text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                                        {ableToSort ? (
                                            <button type = "button" onClick = {header.column.getToggleSortingHandler()} className = "inline-flex items-center gap-1 rounded-sm uppercase tracking-wider hover:text-foreground">
                                                {flexRender(header.column.columnDef.header, header.getContext())}

                                                {sorted === "asc" && <ArrowUp className = "h-3.5 w-3.5"/>}

                                                {sorted === "desc" && <ArrowDown className = "h-3.5 w-3.5"/>}
                                            </button>
                                        ) : (
                                            flexRender(header.column.columnDef.header, header.getContext())
                                        )}
                                    </TableHead>
                                );
                            })}
                        </TableRow>
                    ))}
                </TableHeader>

                <TableBody>
                    {tableForAlerts.getRowModel().rows.length === 0 ? (
                        <TableRow>
                            <TableCell colSpan = {columns.length} className = "py-8 text-center text-sm text-muted-foreground"> No alerts found </TableCell>
                        </TableRow>
                    ) : (
                        tableForAlerts.getRowModel().rows.map((row) => (
                            <TableRow key = {row.id}>
                                {row.getVisibleCells().map((cell) => (
                                    <TableCell key = {cell.id} className = "text-sm"> {flexRender(cell.column.columnDef.cell, cell.getContext())} </TableCell>
                                ))}
                            </TableRow>
                        ))
                    )}
                </TableBody>
            </Table>
        </div>
    );
}