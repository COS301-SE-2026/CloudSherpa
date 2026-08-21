"use client";

import React, { ReactNode, useState, useMemo, useCallback } from "react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/atoms/card";
import { Button } from "@/components/atoms/button";
import { Search, ChevronLeft, ChevronRight, ArrowUpDown } from "lucide-react";
import { Input } from "@/components/atoms/input";
import { Badge } from "@/components/atoms/badge";
import { Switch } from "@/components/atoms/switch";
import {
    useReactTable,
    getCoreRowModel,
    getFilteredRowModel,
    getSortedRowModel,
    createColumnHelper,
    flexRender,
    type SortingState,
    type ColumnFiltersState,
    type HeaderContext,
    type CellContext,
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
import { ResourceSelectionDto } from "@/lib/fetch/aws-connection-api";
import {
    Tooltip,
    TooltipContent,
    TooltipProvider,
    TooltipTrigger,
} from "@/components/atoms/tooltip";
import { Slider } from "@/components/atoms/slider";
import { Label } from "@/components/atoms/label";

export interface PropsForStepThree {
    heading: string;
    description: string;
    onSubmit: (event: React.SubmitEvent<HTMLFormElement>) => void;
    onBack: () => void;
    forSaving: boolean;
    forErrors: string | null;
    children: ReactNode;
}

export interface PropsForIngestionSlider {
    ingestionPeriod: number;
    setIngestionPeriod: (value: number) => void;
    activeCount: number;
    recIngestionPeriod: number;
    formatSeconds: (seconds: string | number) => string;
}

//copied from gcp step 3
export function formattingSecond(totalSeconds: string | number): string {
    const seconds = Number(totalSeconds);

    if (Number.isNaN(seconds) || seconds <= 0) {
        return "0 seconds";
    }

    const minutes = Math.floor(seconds / 60);

    const secondsLeft = seconds % 60;

    let minText = "";
    if (minutes > 0) {
        const labelEnding = minutes === 1 ? "" : "s";
        minText = `${minutes} minute${labelEnding}`;
    }

    let secText = "";
    if (secondsLeft > 0) {
        const labelEnding = secondsLeft === 1 ? "" : "s";
        secText = `${secondsLeft} second${labelEnding}`;
    }

    if (minText && secText) {
        return `${minText} ${secText}`;
    }

    return minText || secText;
}

export function calculatingIngestionPeriod(activeCount: number): number {
    return activeCount * 5 * 20;
}

export function getActiveCount(resources: ResourceSelectionDto[]): number {
    return resources.filter((resource) => resource.active).length;
}

export interface HookForIngestionPeriod {
    activeCount: number;
    recIngestionPeriod: number;
}

export function useIngestionPeriod(resources: ResourceSelectionDto[]): HookForIngestionPeriod {
    const activeCount = useMemo(() => getActiveCount(resources), [resources]);

    const recIngestionPeriod = useMemo(
        () => calculatingIngestionPeriod(activeCount),
        [activeCount]
    );

    return { activeCount, recIngestionPeriod };
}

export function IngestionSlider({
    ingestionPeriod,
    setIngestionPeriod,
    activeCount,
    recIngestionPeriod,
    formatSeconds,
}: Readonly<PropsForIngestionSlider>) {
    return (
        <div className="space-y-2 pt-4 border-t border-border">
            <div className="flex items-center gap-2">
                <Label htmlFor="ingestionPeriod" className="text-foreground text-sm font-medium">
                    {" "}
                    Ingestion interval (seconds){" "}
                </Label>

                <TooltipProvider>
                    <Tooltip>
                        <TooltipTrigger asChild>
                            <button
                                type="button"
                                className="flex items-center justify-center w-5 h-5 rounded-full text-xs text-muted-foreground hover:text-foreground border border-border"
                            >
                                {" "}
                                ?{" "}
                            </button>
                        </TooltipTrigger>

                        <TooltipContent>
                            <p>
                                {" "}
                                Recommended ingestion interval: {recIngestionPeriod} seconds based
                                on {activeCount} selected resource. Setting the interval to a lower
                                value could incur costs due to API free tier limits. The ingestion
                                interval determines the frequency of dashboard timeseries updates.
                            </p>
                        </TooltipContent>
                    </Tooltip>
                </TooltipProvider>
            </div>

            <div className="flex flex-col gap-2 justify-center items-end">
                <span className="text-sm font-medium"> {formatSeconds(ingestionPeriod)} </span>

                <Slider
                    value={[Number(ingestionPeriod)]}
                    onValueChange={(changeValue) => setIngestionPeriod(changeValue[0])}
                    min={60}
                    max={400}
                    step={1}
                />

                <p className="text-sm text-muted-foreground/70">
                    {" "}
                    Recommended: {formatSeconds(recIngestionPeriod)}{" "}
                </p>
            </div>
        </div>
    );
}

interface ActionForResource {
    toggleResource: (id: string) => void;
    toggleAll: () => void;
}

function ListOfTags({ tags }: Readonly<{ tags: Record<string, string> }>) {
    const displayedTags = Object.entries(tags).slice(0, 3);

    return (
        <div className="flex items-center gap-1 flex-wrap">
            {displayedTags.map(([key, value]) => (
                <Badge key={key} variant="secondary" className="text-[10px] font-normal">
                    {key}: {value}
                </Badge>
            ))}
        </div>
    );
}

function ResourceHeaders({ column }: Readonly<HeaderContext<ResourceSelectionDto, string>>) {
    return (
        <Button
            variant="ghost"
            size="sm"
            className="h-auto p-0 font-medium text-foreground hover:bg-transparent hover:text-foreground/80"
            onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
        >
            {" "}
            Resource <ArrowUpDown size={12} className="ml-1.5 text-muted-foreground" />{" "}
        </Button>
    );
}

function ResourceCells({ getValue }: Readonly<CellContext<ResourceSelectionDto, string>>) {
    return <span className="font-medium"> {getValue()} </span>;
}

function SecondaryCells({ getValue }: Readonly<CellContext<ResourceSelectionDto, string>>) {
    return <span className="text-xs text-muted-foreground"> {getValue()} </span>;
}

function TagCells({
    getValue,
}: Readonly<CellContext<ResourceSelectionDto, Record<string, string>>>) {
    return <ListOfTags tags={getValue()} />;
}

function ToggleHeader() {
    return <span className="block text-center"> Active/Inactive </span>;
}

function ToggleCells({
    row,
    table,
}: Readonly<CellContext<ResourceSelectionDto, ResourceSelectionDto["active"]>>) {
    const { toggleResource } = table.options.meta as ActionForResource;

    return (
        <div className="flex justify-center">
            <Switch
                checked={row.original.active}
                onCheckedChange={() => toggleResource(row.original.resourceId)}
            />
        </div>
    );
}

const helperForColumns = createColumnHelper<ResourceSelectionDto>();

const columns = [
    helperForColumns.accessor("resourceName", { header: ResourceHeaders, cell: ResourceCells }),
    helperForColumns.accessor("serviceType", { header: "Type", cell: SecondaryCells }),
    helperForColumns.accessor("region", { header: "Region", cell: SecondaryCells }),
    helperForColumns.accessor("tags", { header: "Tags", cell: TagCells }),
    helperForColumns.accessor("active", {
        header: ToggleHeader,
        filterFn: "equals",
        cell: ToggleCells,
        enableSorting: false,
    }),
];

interface PropsForResourceTable {
    data: ResourceSelectionDto[];
    onDataChange: (
        newData:
            ResourceSelectionDto[] | ((previous: ResourceSelectionDto[]) => ResourceSelectionDto[])
    ) => void;
    onFilterChange: (value: string) => void;
    filterValue: string;
    pageSize?: number;
}

export function ResourceTable({
    data,
    onDataChange,
    onFilterChange,
    filterValue,
    pageSize = 8,
}: Readonly<PropsForResourceTable>) {
    const [forPagination, setForPagination] = useState({ pageIndex: 0, pageSize });

    const [sort, setSort] = useState<SortingState>([]);

    const [filterColumn, setFilterColumn] = useState<ColumnFiltersState>([]);

    const toggleResource = useCallback(
        (resourceId: string) => {
            onDataChange((current: ResourceSelectionDto[]) =>
                current.map((resource: ResourceSelectionDto) =>
                    resource.resourceId === resourceId
                        ? {
                              ...resource,
                              active: !resource.active,
                          }
                        : resource
                )
            );
        },
        [onDataChange]
    );

    const toggleAll = useCallback(() => {
        onDataChange((previous: ResourceSelectionDto[]) => {
            const allActive =
                previous.length > 0 &&
                previous.every((resource: ResourceSelectionDto) => resource.active);

            return previous.map((resource) => ({
                ...resource,
                active: !allActive,
            }));
        });
    }, [onDataChange]);

    const actions = useMemo<ActionForResource>(
        () => ({
            toggleResource,
            toggleAll,
        }),
        [toggleResource, toggleAll]
    );

    const table = useReactTable({
        data: data,
        columns,
        meta: actions,
        state: {
            globalFilter: filterValue,
            sorting: sort,
            columnFilters: filterColumn,
            pagination: forPagination,
        },
        getRowId: (row) => row.resourceId,
        onGlobalFilterChange: onFilterChange,
        onSortingChange: setSort,
        onColumnFiltersChange: setFilterColumn,
        getCoreRowModel: getCoreRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        getSortedRowModel: getSortedRowModel(),
        onPaginationChange: setForPagination,
        getPaginationRowModel: getPaginationRowModel(),
    });

    const allActive = data.length > 0 && data.every((resource) => resource.active);

    return (
        <div className="space-y-4">
            <div className="flex flex-wrap items-center justify-between gap-2">
                <h3 className="text-foreground text-sm font-semibold uppercase tracking-wider opacity-80">
                    {" "}
                    Available resources{" "}
                </h3>

                <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={toggleAll}
                    className="text-primary hover:text-accent text-sm transition-colors px-0"
                >
                    {data.length > 0 && allActive ? "Deselect All" : "Select All"}
                </Button>
            </div>

            <div className="relative flex-1">
                <Search
                    size={14}
                    className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground"
                />

                <Input
                    value={filterValue}
                    onChange={(change) => onFilterChange(change.target.value)}
                    placeholder="Search resources..."
                    className="pl-8 h-9"
                />
            </div>

            <div className="rounded-lg border">
                <Table className="table-fixed w-full">
                    <TableHeader>
                        {table.getHeaderGroups().map((headerGroup) => (
                            <TableRow key={headerGroup.id} className="hover:bg-transparent">
                                {headerGroup.headers.map((header) => (
                                    <TableHead
                                        key={header.id}
                                        className={
                                            header.column.id === "selected" ? "w-10" : undefined
                                        }
                                    >
                                        {flexRender(
                                            header.column.columnDef.header,
                                            header.getContext()
                                        )}
                                    </TableHead>
                                ))}
                            </TableRow>
                        ))}
                    </TableHeader>

                    <TableBody>
                        {table.getRowModel().rows.length === 0 ? (
                            <TableRow>
                                <TableCell
                                    colSpan={columns.length}
                                    className="text-center text-xs text-muted-foreground py-6"
                                >
                                    {" "}
                                    No resources{" "}
                                </TableCell>
                            </TableRow>
                        ) : (
                            table.getRowModel().rows.map((row) => (
                                <TableRow key={row.id}>
                                    {row.getVisibleCells().map((cell) => (
                                        <TableCell
                                            key={cell.id}
                                            className={
                                                cell.column.id === "selected" ? "w-10" : undefined
                                            }
                                        >
                                            {flexRender(
                                                cell.column.columnDef.cell,
                                                cell.getContext()
                                            )}
                                        </TableCell>
                                    ))}
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>

                {data.length > 8 && (
                    <div className="flex items-center justify-center gap-2 py-4 border-t border-border">
                        <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            onClick={() => table.previousPage()}
                            disabled={!table.getCanPreviousPage()}
                            className="h-8 w-8 p-0"
                        >
                            {" "}
                            <ChevronLeft size={16} />{" "}
                        </Button>

                        <span className="text-sm font-medium px-3">
                            {" "}
                            Page {table.getState().pagination.pageIndex + 1} of{" "}
                            {table.getPageCount() || 1}{" "}
                        </span>

                        <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            onClick={() => table.nextPage()}
                            disabled={!table.getCanNextPage()}
                            className="h-8 w-8 p-0"
                        >
                            {" "}
                            <ChevronRight size={16} />{" "}
                        </Button>
                    </div>
                )}
            </div>
        </div>
    );
}

export function StepThree({
    heading,
    description,
    onSubmit,
    onBack,
    forSaving,
    forErrors,
    children,
}: Readonly<PropsForStepThree>) {
    return (
        <div className="min-h-screen bg-background flex items-center justify-center p-8">
            <Card className="w-full max-w-2xl shadow-none">
                <CardHeader className="pb-2">
                    <div className="flex items-center gap-2 mb-4">
                        <div className="w-2 h-2 rounded-full bg-primary" />

                        <span className="text-sm font-medium text-muted-foreground/70">
                            {" "}
                            STEP 3 OF 3{" "}
                        </span>
                    </div>

                    <CardTitle className="text-2xl font-semibold tracking-tight text-foreground">
                        {" "}
                        {heading}{" "}
                    </CardTitle>

                    <CardDescription className="mt-2 text-muted-foreground/70">
                        {" "}
                        {description}{" "}
                    </CardDescription>
                </CardHeader>

                <CardContent>
                    <form onSubmit={onSubmit} className="space-y-8">
                        {" "}
                        {children}
                        {forErrors && (
                            <div className="rounded-md border border-red-500 bg-red-50 p-3 text-sm text-red-700">
                                {" "}
                                {forErrors}{" "}
                            </div>
                        )}
                        <div className="flex justify-between pt-6">
                            <Button
                                type="button"
                                disabled={forSaving}
                                onClick={onBack}
                                className="bg-primary hover:bg-accent hover:text-accent-foreground text-primary-foreground px-6 rounded-md transition-all duration-200 font-medium"
                            >
                                {" "}
                                Back{" "}
                            </Button>

                            <Button
                                type="submit"
                                disabled={forSaving}
                                className="bg-primary hover:bg-accent hover:text-accent-foreground text-primary-foreground px-8 py-2 rounded-md transition-all duration-200 font-medium"
                            >
                                {" "}
                                {forSaving ? "Saving..." : "Finish"}{" "}
                            </Button>
                        </div>
                    </form>
                </CardContent>
            </Card>
        </div>
    );
}
