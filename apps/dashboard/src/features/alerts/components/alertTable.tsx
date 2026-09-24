"use client";

import {useMemo, useState} from "react";
import {ColumnDef, flexRender, getCoreRowModel, getSortedRowModel, SortingState, useReactTable} from "@tanstack/react-table";
import {Button} from "@/components/atoms/button";
import {Switch} from "@/components/atoms/switch";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/atoms/table";
import {ArrowUp, ArrowDown, Info} from "lucide-react";
import { SEVERITY_COLOURS, STATUS_LABELS, TYPE } from "@/features/alerts/types/alertTypes";
import type {Alert} from "@/features/alerts/types/alertTypes";

interface PropsForAlertsTable{
    alerts : Alert[];
    onToggle : (alert : Alert, enabled : boolean) => void;
    info : (alert : Alert) => void;
}

interface ForColumns{
    onToggle : (alert : Alert, enabled : boolean) => void;
    info : (alert : Alert) => void;
}

function helperForColumns({onToggle, info} : ForColumns) : ColumnDef<Alert>[]{
    return[
        {id : "enabled", header : () => "STATUS", enableSorting : false, cell : ({row}) => (
            <div className = "flex items-center gap-2">
                <Switch checked = {row.original.status === "ACTIVE"} onCheckedChange = {(checked) => onToggle(row.original, checked)}/>

                <span className = "text-xs text-muted-foreground"> {STATUS_LABELS[row.original.status]} </span>
            </div>
        ),},

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
            
        </div>
    );
}