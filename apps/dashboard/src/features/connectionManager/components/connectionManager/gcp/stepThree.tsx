"use client";

import React, { useState, useEffect, useMemo, useCallback } from "react";
import { StepThree } from "@/features/connectionManager/components/connectionManager/wizardSetup/stepThree";
import { Button } from "@/components/atoms/button";
import {
    Tooltip,
    TooltipContent,
    TooltipProvider,
    TooltipTrigger,
} from "@/components/atoms/tooltip";
import { Slider } from "@/components/atoms/slider";
import { Label } from "@/components/atoms/label";
import { Search, ArrowUpDown, ChevronLeft, ChevronRight } from "lucide-react";
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
import {
    createGcpConnection,
    GcpCredentialsDto,
    PersistGcpConnectionRequest,
    ResourceSelectionDto,
} from "@/lib/fetch/gcp-connection-api";
import { ResourceDetail } from "@/lib/fetch/cloud-resource-api";
import { useRouter } from "next/navigation";
/*
- should have tanstack table for resources, as elect & deselect all for it
- should also have pagination
*/

interface StepThreePropsForGcp {
    displayName: string;
    resources: ResourceDetail[];
    ingestionPeriod: number;
    credentials: GcpCredentialsDto;
    onComplete: (ingestionPeriod: number) => void;
    onBack?: () => void;
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

export default function StepThreeGcp({
    displayName,
    resources,
    credentials,
    onComplete,
    onBack,
    ingestionPeriod = 60,
}: Readonly<StepThreePropsForGcp>) {
    const [forPagination, setForPagination] = useState({ pageIndex: 0, pageSize: 8 });

    const [forSaving, setForSaving] = useState(false);

    const [errors, setErrors] = useState<string | null>(null);

    const [forIngestionPeriod, setForIngestionPeriod] = useState<number>(ingestionPeriod);

    const [tableResources, setTableResources] = useState<ResourceSelectionDto[]>([]);

    const [filter, setFilter] = useState("");

    const [sort, setSort] = useState<SortingState>([]);

    const [filterColumn, setFilterColumn] = useState<ColumnFiltersState>([]);

    const realResources = resources;

    const count = tableResources.filter((resource) => resource.active).length;
    const recIngestionPeriod = count * 5 * 20;

    const formattingSecond = (totalSeconds: string | number) => {
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
    };

    const toggleResource = (resourceId: string) => {
        setTableResources((current) =>
            current.map((resource) =>
                resource.resourceId === resourceId
                    ? {
                          ...resource,
                          active: !resource.active,
                      }
                    : resource
            )
        );
    };

    const handlingSelectedAll = useCallback(() => {
        setTableResources((previous) => {
            const allActive = previous.length > 0 && previous.every((resource) => resource.active);

            return previous.map((resource) => ({
                ...resource,
                active: !allActive,
            }));
        });
    }, []);

    const actions = useMemo<ActionForResource>(
        () => ({
            toggleResource,
            toggleAll: handlingSelectedAll,
        }),
        [toggleResource, handlingSelectedAll]
    );

    const table = useReactTable({
        data: tableResources,
        columns,
        meta: actions,
        state: {
            globalFilter: filter,
            sorting: sort,
            columnFilters: filterColumn,
            pagination: forPagination,
        },
        getRowId: (row) => row.resourceId,
        onGlobalFilterChange: setFilter,
        onSortingChange: setSort,
        onColumnFiltersChange: setFilterColumn,
        getCoreRowModel: getCoreRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        getSortedRowModel: getSortedRowModel(),
        onPaginationChange: setForPagination,
        getPaginationRowModel: getPaginationRowModel(),
    });

    useEffect(() => {
        const mappedResources: ResourceSelectionDto[] = realResources.map((resource) => ({
            resourceId: resource.resourceId,
            serviceType: resource.serviceCategory,
            resourceType: resource.resourceType,
            resourceName: resource.name,
            region: resource.region,
            tags: resource.tags,
            active: true,
        }));
        setTableResources(mappedResources);
    }, [realResources]);

    const router = useRouter();

    const handleSubmit = async (event: React.SubmitEvent<HTMLFormElement>) => {
        event.preventDefault();

        setForSaving(true);
        setErrors(null);

        try {
            const request: PersistGcpConnectionRequest = {
                userId: "",
                displayName,
                ingestionPeriod: forIngestionPeriod.toString(),
                credentials,
                resources: tableResources.map((resource): ResourceSelectionDto => ({
                    resourceId: resource.resourceId,
                    serviceType: resource.serviceType,
                    resourceType: resource.resourceType,
                    resourceName: resource.resourceName,
                    region: resource.region,
                    tags: resource.tags,
                    active: resource.active,
                })),
            };

            await createGcpConnection(request);

            onComplete(forIngestionPeriod);
            router.push("/manageConnections");
        } catch (err) {
            setErrors(err instanceof Error ? err.message : "Unable to create GCP connection.");
        } finally {
            setForSaving(false);
        }
    };

    return (
        <StepThree
            heading="Select instances"
            description="Select the instance you want CloudSherpa to monitor"
            onSubmit={handleSubmit}
            onBack={onBack || (() => {})}
            forSaving={forSaving}
            forErrors={errors}
        >
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
                        onClick={handlingSelectedAll}
                        className="text-primary hover:text-accent text-sm transition-colors px-0"
                    >
                        {tableResources.length > 0 && tableResources.every((r) => r.active)
                            ? "Deselect All"
                            : "Select All"}
                    </Button>
                </div>

                <div className="relative flex-1">
                    <Search
                        size={14}
                        className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground"
                    />

                    <Input
                        value={filter}
                        onChange={(change) => setFilter(change.target.value)}
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
                                                    cell.column.id === "selected"
                                                        ? "w-10"
                                                        : undefined
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

                    {tableResources.length > 8 && (
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

            <div className="space-y-2 pt-4 border-t border-border">
                <div className="flex items-center gap-2">
                    <Label
                        htmlFor="ingestionPeriod"
                        className="text-foreground text-sm font-medium"
                    >
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
                                    Recommended ingestion interval: {recIngestionPeriod} seconds
                                    based on {count} selected resources. Setting the interval to a
                                    lower value could incur costs due to API free tier limits. The
                                    ingestion interval determines the frequency of dashboard
                                    timeseries updates.
                                </p>
                            </TooltipContent>
                        </Tooltip>
                    </TooltipProvider>
                </div>

                <div className="flex flex-col gap-2 justify-center items-end">
                    <span className="text-sm font-medium">
                        {" "}
                        {formattingSecond(forIngestionPeriod)}{" "}
                    </span>

                    <Slider
                        value={[Number(forIngestionPeriod)]}
                        onValueChange={(changeVal) => setForIngestionPeriod(changeVal[0])}
                        min={60}
                        max={400}
                        step={1}
                    />

                    <p className="text-sm text-muted-foreground/70">
                        {" "}
                        Recommended: {formattingSecond(recIngestionPeriod)}{" "}
                    </p>
                </div>
            </div>
        </StepThree>
    );
}
