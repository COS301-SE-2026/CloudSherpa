"use client";

import{useMemo, useState} from "react";
import{Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/atoms/table";
import {ColumnDef, flexRender, getCoreRowModel, getSortedRowModel, SortingState, useReactTable} from "@tanstack/react-table";
import {Button} from "@/components/atoms/button";
import {ArrowUp, ArrowDown, Check, X} from "lucide-react";
import type {Alert, AnomalyAlertPayload} from "@/features/alerts/types/alertTypes";
import {SEVERITY_COLOURS} from "@/features/alerts/types/alertTypes";

interface PropsForAlertsTable{
    alerts : Alert[];
    acknowledge : (alertId : string) => Promise<void>;
    dismiss : (alertId : string) => Promise<void>;
}

function anomalyPayload(alert : Alert) : AnomalyAlertPayload | null {
    return alert.alertType === "ANOMALY" ? (alert.payload as AnomalyAlertPayload) : null;
}

export function AlertsTable({
    alerts, acknowledge, dismiss,
} : Readonly<PropsForAlertsTable>){
    const [sorting, setSorting] = useState<SortingState>([]);

    const forColumns = useMemo<ColumnDef<Alert>[]>(() => [
        {accessorKey : "severity", header : () => "SEVERITY", cell : ({row}) => {
            const access = row.original;

            const inactive = access.status !== "ACTIVE";

            return(
                <span className = {inactive ? "text-muted-foreground" : SEVERITY_COLOURS[access.severity]}> {access.severity} </span>
            );
        },},

        {accessorKey : "alertType", header : () => "TYPE", cell : ({row}) => (
            <span className = "text-foreground"> {row.original.alertType} </span>
        ),},

        {id : "metric", header : () => "METRIC", enableSorting : false, cell : ({row}) => {
            const access = row.original;

            const forName = (access.payload as {metric_name?: string}).metric_name ?? "-";

            return <span className = "text-foreground"> {forName} </span>;
        },},

        {id : "z-score", header : () => "Z_SCORE", enableSorting : false, cell : ({row}) => {
            const forPayload = anomalyPayload(row.original);

            if(!forPayload){
                return <span className = "text-muted-foreground"> - </span>;
            }

            return <span className = "text-foreground"> {forPayload.z_score.toFixed(2)} </span>;
        },},

        {id : "value", header : () => "VALUE", enableSorting : false, cell : ({row}) => {
            const access = row.original;

            const forValue = (access.payload as {metric_value?: number}).metric_value;

            return(<span className = "text-foreground"> {typeof forValue === "number" ? forValue.toFixed(2) : "-"} </span>);
        },},

        {id : "baseline", header : () => "BASELINE", enableSorting : false, cell : ({row}) => {
            const forPayload = anomalyPayload(row.original);

            if(!forPayload){
                return <span className = "text-muted-foreground"> - </span>
            }

            return(<span className = "text-muted-foreground"> {forPayload.average_value.toFixed(2)} +- {forPayload.standard_deviation.toFixed(2)} </span>);
        },},

        {accessorKey : "status", header : () => "STATUS", cell : ({row}) => (
            <span className = "text-muted-foreground"> {row.original.status} </span>
        ),},

        {id : "createdAt", header : () => "CREATED", enableSorting : false, cell : ({row}) => (
            <span className = "text-muted-foreground"> {row.original.createdAt ? new Date(row.original.createdAt).toLocaleString() : "-"} </span>
        ),},

        {id : "actions", header : () => "ACTIONS", enableSorting : false, cell : ({row}) => {
            const access = row.original;

            const active = access.status === "ACTIVE";

            return(
                <div className = "flex items-center gap-1">
                    <Button variant = "ghost" size = "icon" className = "h-8 w-8 text-primary hover:text-primary" disabled = {!active} onClick = {() => acknowledge(access.alertId)}> <Check className = "h-4 w-4"/> </Button>

                    <Button variant = "ghost" size = "icon" className = "h-8 w-8 text-destructive hover:text-destructive" disabled = {!active} onClick = {() => dismiss(access.alertId)}> <X className = "h-4 w-4"/> </Button>
                </div>
            );
        },},
    ], [acknowledge, dismiss],);

    const forAlertsTable = useReactTable({
        data : alerts, columns : forColumns, state : {sorting}, onSortingChange : setSorting, getCoreRowModel : getCoreRowModel(), getSortedRowModel : getSortedRowModel(),
    });

    return(
        <div className = "overflow-hidden rounded-lg border border-border bg-card text-card-foreground">
            <Table>
                <TableHeader>
                    {forAlertsTable.getHeaderGroups().map((headerGroup) => (
                        <TableRow key = {headerGroup.id} className = "hover:bg-transparent">
                            {headerGroup.headers.map((header) => {
                                const ableToSort = header.column.getCanSort();

                                const sorted = header.column.getIsSorted();

                                return(
                                    <TableHead key = {header.id} className = "text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                                        {ableToSort ? (
                                            <button type = "button" onClick = {header.column.getToggleSortingHandler()} className = "inline-flex items-center gap-1 rounded-sm uppercase tracking-wider hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring focus-visible:ring-offset-2 focus-visible:ring-offset-background">
                                                {flexRender(header.column.columnDef.header, header.getContext(),)}

                                                {sorted === "asc" ? (<ArrowUp className = "h-3.5 w-3.5"/> ) : (<ArrowDown className = "h-3.5 w-3.5"/> )}
                                            </button>
                                        ) : (
                                            flexRender(header.column.columnDef.header, header.getContext(),)
                                        )}
                                    </TableHead>
                                );
                            })}
                        </TableRow>
                    ))}
                </TableHeader>

                <TableBody>
                    {forAlertsTable.getRowModel().rows.length === 0 ? (
                        <TableRow>
                            <TableCell colSpan = {forColumns.length} className = "py-8 text-center text-sm text-muted-foreground"> No alerts found </TableCell>
                        </TableRow>
                    ) : (
                        forAlertsTable.getRowModel().rows.map((row) => (
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