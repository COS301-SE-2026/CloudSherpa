"use client";

import {useMemo, useState} from "react";
import {Switch} from "@/components/atoms/switch";
import {ColumnDef, flexRender, getCoreRowModel, getSortedRowModel, SortingState, useReactTable} from "@tanstack/react-table";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/atoms/table";
import {Button} from "@/components/atoms/button";
import type {Threshold} from "@/features/thresholds/types/thresholdTypes";
import {OPERATOR_LABEL, SEVERITY_COLOURS} from "@/features/thresholds/types/thresholdTypes";
import {ArrowUp, ArrowDown} from "lucide-react";

interface PropsForThresholds{
    thresholds : Threshold[];
    edit : (forThreshold : Threshold) => void;
    toggleEnabled : (forThreshold : Threshold, enabled : boolean) => void;
    onDelete : (forThreshold : Threshold) => void;
}

export function ThresholdTable({
    thresholds, edit, toggleEnabled, onDelete,
} : Readonly<PropsForThresholds>){
    const [sorting, setSorting] = useState<SortingState>([]);

    const forColumns = useMemo<ColumnDef<Threshold>[]>(() => [
        {id : "enabled", header : () => "ENABLED", enableSorting : false, cell : ({row}) => {
            const forThreshold = row.original;

            return(<Switch checked = {forThreshold.enabled} onCheckedChange = {(checked) => toggleEnabled(forThreshold, checked)} aria-label = {`Toggle ${forThreshold.metricName} threshold`}/>);
        },},

        {accessorKey : "metricName", header : () => "METRIC", cell : ({row}) => {
            const forThreshold = row.original;

            const mutedThreshold = !forThreshold.enabled;

            return(<span className = {mutedThreshold ? "text-muted-foreground" : "text-foreground"}> {forThreshold.metricName} </span>);
        },},

        {id : "condition", header : () => "CONDITION", enableSorting : false, cell : ({row}) => {
            const forThreshold = row.original;

            const mutedThreshold = !forThreshold.enabled;

            return(<span className = {mutedThreshold ? "text-muted-foreground" : "text-foreground"}> {OPERATOR_LABEL[forThreshold.operator]} {forThreshold.value} </span>);
        },},

        {accessorKey : "severity", header : () => "SEVERITY", cell : ({row}) => {
            const forThreshold = row.original;

            const mutedThreshold = !forThreshold.enabled;

            return(<span className = {mutedThreshold ? "text-muted-foreground" : SEVERITY_COLOURS[forThreshold.severity]}> {forThreshold.severity} </span>);
        },},

        {accessorKey : "value", header : () => "VALUE", cell : ({row}) => {
            const forThreshold = row.original;

            const mutedThreshold = !forThreshold.enabled;

            return(<span className = {mutedThreshold ? "text-muted-foreground" : "text-foreground"}> {forThreshold.value} </span>);
        },},

        {id : "currentValue", header : () => "CURRENT VALUE", enableSorting : false, cell : () => <span className = "text-muted-foreground"> - </span>},

        {id : "actions", header : () => "ACTIONS", enableSorting : false, cell : ({row}) => {
            const forThreshold = row.original;

            return(<div className = "flex items-center gap-2">
                <Button variant = "link" size = "sm" className = "h-auto p-0 text-primary" onClick = {() => edit(forThreshold)}> Edit </Button>

                <Button variant = "link" size = "sm" className = "h-auto p-0 text-destructive" onClick = {() => onDelete(forThreshold)}> Delete </Button>
            </div>);
        },},
    ], [edit, toggleEnabled, onDelete],
    );

    const forThresholdTable = useReactTable({
        data : thresholds, columns : forColumns, state : {sorting}, onSortingChange : setSorting, getCoreRowModel : getCoreRowModel(), getSortedRowModel : getSortedRowModel(),
    });

    return(
        
    );
}