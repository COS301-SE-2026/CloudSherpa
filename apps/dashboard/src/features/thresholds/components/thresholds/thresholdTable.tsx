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
import { ArrowUp, ArrowDown, Pencil, Trash2 } from "lucide-react";
import {
    Tooltip,
    TooltipContent,
    TooltipProvider,
    TooltipTrigger,
} from "@/components/atoms/tooltip";
import {useMetricStore} from "@/features/dashboard/stores/metric-store";
import {metricSeriesToArray} from "@/features/dashboard/types/metric";

const CONDITION_VERBS: Record<OperatorsForThreshold, string> = {
    GT: "More than",
    GTE: "More than or equal to",
    LT: "Less than",
    LTE: "Less than or equal to",
    EQ: "Equal to",
};

interface PropsForTruncation {
    text: string;
    className?: string;
    tooltipText?: string;
}

interface PropsForCurrentValue{
    resourceId : string;
    metricName : string;
    enabled : boolean;
}

function CurrentValue({resourceId, metricName, enabled} : Readonly<PropsForCurrentValue>){
    const forSeries = useMetricStore((forState) => forState.seriesByKey[`${resourceId}:${metricName}`]);

    const latestValue = metricSeriesToArray(forSeries).at(-1);

    if(latestValue == null || latestValue.value == null){
        return(<Truncation text = "-" className = "w-[100px] flex-shrink-0 text-muted-foreground cursor-help"/>);
    }

    const forDisplay = String(latestValue.value);

    const forTooltip = `${metricName} at ${latestValue.timestamp}: ${forDisplay}`;

    return(<Truncation text = {forDisplay} tooltipText = {forTooltip} className = {`w-[100px] flex-shrink-0 ${enabled ? "text-foreground" : "text-muted-foreground"}`}/>);
}

function Truncation({ text, className = "", tooltipText }: Readonly<PropsForTruncation>) {
    return (
        <TooltipProvider>
            <Tooltip>
                <TooltipTrigger asChild>
                    <span className={`block truncate ${className}`}> {text} </span>
                </TooltipTrigger>

                <TooltipContent>
                    {" "}
                    <p className="max-w-xs break-all"> {tooltipText ?? text} </p>
                </TooltipContent>
            </Tooltip>
        </TooltipProvider>
    );
}

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
}

//copied from below to correct sonarqube errors
function helperForColumns({ edit, toggleEnabled, onDelete }: Columns): ColumnDef<Threshold>[] {
    return [
        {
            id: "enabled",
            header: () => "ENABLED",
            enableSorting: false,
            cell: ({ row }) => {
                const forThreshold = row.original;

                return (
                    <Switch
                        checked={forThreshold.enabled}
                        onCheckedChange={(checked) => toggleEnabled(forThreshold, checked)}
                        aria-label={`Toggle ${forThreshold.metricName} threshold`}
                    />
                );
            },
        },

        {
            accessorKey: "metricName",
            header: () => "METRIC",
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
            id: "currentValue",
            header: () => "CURRENT VALUE",
            enableSorting: false,
            cell: ({row}) => {
                const forThreshold = row.original;

                return(<CurrentValue resourceId = {forThreshold.resourceId} metricName = {forThreshold.metricName} enabled = {forThreshold.enabled}/>);
            },
        },

        {
            id: "actions",
            header: () => "ACTIONS",
            enableSorting: false,
            cell: ({ row }) => {
                const forThreshold = row.original;

                return (
                    <div className="flex items-center gap-2">
                        <Button
                            variant="ghost"
                            size="sm"
                            className="h-auto p-0 text-primary"
                            onClick={() => edit(forThreshold)}
                        >
                            {" "}
                            <Pencil className="h-4 w-4" />{" "}
                        </Button>

                        <Button
                            variant="ghost"
                            size="sm"
                            className="h-auto p-0 text-destructive"
                            onClick={() => onDelete(forThreshold)}
                        >
                            {" "}
                            <Trash2 className="h-4 w-4" />{" "}
                        </Button>
                    </div>
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

    const forColumns = useMemo<ColumnDef<Threshold>[]>(
        () => helperForColumns({ edit, toggleEnabled, onDelete }),
        [edit, toggleEnabled, onDelete]
    );

    const forThresholdTable = useReactTable({
        data: thresholds,
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
                    {forThresholdTable.getHeaderGroups().map((headerGroup) => (
                        <TableRow key={headerGroup.id} className="hover:bg-transparent">
                            {headerGroup.headers.map((header) => {
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
        </div>
    );
}
