"use client";

import React, { ReactNode, useState, useEffect, useMemo, useCallback } from "react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/atoms/card";
import { Button } from "@/components/atoms/button";
import {
    Tooltip,
    TooltipContent,
    TooltipProvider,
    TooltipTrigger,
} from "@/components/atoms/tooltip";
import { Label } from "@/components/atoms/label";
import { Slider } from "@/components/atoms/slider";
import { Search, ChevronLeft, ChevronRight } from "lucide-react";
import { Input } from "@/components/atoms/input";
import {
    useReactTable,
    getCoreRowModel,
    getFilteredRowModel,
    getSortedRowModel,
    createColumnHelper,
    type CellContext,
    getPaginationRowModel,
    type Table as TableType,
} from "@tanstack/react-table";
import {
    Resource,
    ResourceHeaders,
    ResourceCells,
    SecondaryCells,
    TagCells,
    ToggleHeader,
    ToggleCells,
    ResourceTable,
} from "@/features/resourceManager/resourceTable";
import { ResourceSelectionDto } from "@/lib/fetch/aws-connection-api";
import { ResourceDetail } from "@/lib/fetch/cloud-resource-api";

export interface PropsForIngestionSlider {
    forIngestionPeriod: string;
    setForIngestionPeriod: (value: string) => void;
    count: number;
    recIngestionPeriod: number;
    formattingSecond: (seconds: string | number) => string;
}
export interface PropsForStepThree {
    heading: string;
    description: string;
    onSubmit: (event: React.SubmitEvent<HTMLFormElement>) => void;
    onBack: () => void;
    forSaving: boolean;
    forErrors: string | null;
    children: ReactNode;
}

//copied from gpc step 3
export function SliderForIngestion({
    forIngestionPeriod,
    setForIngestionPeriod,
    count,
    recIngestionPeriod,
    formattingSecond,
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
                                on {count} selected resources. Setting the interval to a lower value
                                could incur costs due to API free tier limits. The ingestion
                                interval determines the frequency of dashboard timeseries updates.
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
                    onValueChange={(changeVal) => setForIngestionPeriod(String(changeVal[0]))}
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

//step 3 for gcp and azure share almost the same functionaliyt, hence copied
//and pasted the shared functions (decrease duplicate code)
interface DetailsForResource {
    id: string;
    name: string;
    type?: string;
    region?: string;
    tag?: string[];
    status?: "active" | "inactive";
}

export interface StepThreePropsForBase {
    resources?: ResourceDetail[];
    onNext: (data: { selectedResources: ResourceSelectionDto[]; ingestionPeriod: string }) => void;
    onBack?: () => void;
    ingestionPeriod?: string;
    hardCodedResources?: ResourceDetail[];
}

const hardCode: ResourceDetail[] = [
    {
        resourceId: "resource1",
        name: "Resource one",
        resourceType: "Service one",
        serviceCategory: "Category one",
        region: "region 1",
        tags: { tag1: "tag1", tag2: "tag2" },
    },
];

interface ActionForResource {
    changeStatus: (id: string) => Promise<void>;
    toggleResource: (id: string) => void;
    toggleAll: () => void;
}

function SelectionHeader({
    table,
}: Readonly<{ table: TableType<Resource & { selected: boolean }> }>) {
    const { toggleAll } = table.options.meta as ActionForResource;

    const rows = table.getRowModel().rows;

    const allSelected = rows.length > 0 && rows.every((row) => row.original.selected);

    const someSelected = rows.some((row) => row.original.selected);

    return (
        <div className="flex justify-center">
            <input
                type="checkbox"
                checked={allSelected}
                ref={(input) => {
                    if (input) {
                        input.indeterminate = someSelected && !allSelected;
                    }
                }}

                onChange={toggleAll}
                className="w-4 h-4 rounded border-border bg-background text-primary focus:ring-primary"
            />
        </div>
    );
}

function SelectionCells({
    row,
    table,
}: Readonly<CellContext<Resource & { selected: boolean }, boolean>>) {
    const { toggleResource } = table.options.meta as ActionForResource;

    return (
        <div className="flex justify-center">
            <input
                type="checkbox"
                checked={row.original.selected}
                onChange={() => {
                    toggleResource(row.original.resourceId);
                }}
                className="w-4 h-4 rounded border-border bg-background text-primary focus:ring-primary"
            />
        </div>
    );
}

export default function StepThreeBase({
    resources = [],
    onNext,
    onBack,
    ingestionPeriod = "60",
    hardCodedResources = hardCode,
}: Readonly<StepThreePropsForBase>) {
    const [forPagination, setForPagination] = useState({ pageIndex: 0, pageSize: 8 });

    const [forSaving, setForSaving] = useState(false);

    const [errors, setErrors] = useState<string | null>(null);

    const [forIngestionPeriod, setForIngestionPeriod] = useState<string>(ingestionPeriod);

    const [tableResources, setTableResources] = useState<(Resource & { selected: boolean })[]>([]);

    const [filter, setFilter] = useState("");

    const realResources = useMemo(
        () => (resources && resources.length > 0 ? resources : hardCodedResources),
        [resources]
    );

    const count = tableResources.filter((forResources) => forResources.selected).length;

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

    const changeStatus = useCallback(async (id: string) => {
        setTableResources((previous) =>
            previous.map((resources) =>
                resources.resourceId === id
                    ? {
                          ...resources,
                          active: !resources.active,
                      }
                    : resources
            )
        );
    }, []);

    const toggleResource = useCallback((resourceId: string) => {
        setTableResources((previous) =>
            previous.map((forResources) =>
                forResources.resourceId === resourceId
                    ? { ...forResources, selected: !forResources.selected }
                    : forResources
            )
        );
    }, []);

    const handlingSelectedAll = useCallback(() => {
        setTableResources((previous) => {
            const allSelected = previous.every((forResources) => forResources.selected);

            return previous.map((resource) => ({
                ...resource,
                selected: !allSelected,
            }));
        });
    }, []);

    const actions = useMemo<ActionForResource>(
        () => ({
            changeStatus,
            toggleResource,
            toggleAll: handlingSelectedAll,
        }),
        [changeStatus, toggleResource, handlingSelectedAll]
    );

    const helperForColumns = createColumnHelper<Resource & { selected: boolean }>();

    const columns = [
        helperForColumns.accessor("selected", {
            header: SelectionHeader,
            cell: SelectionCells,
        }),

        helperForColumns.accessor("resourceName", { header: ResourceHeaders, cell: ResourceCells }),
        helperForColumns.accessor("serviceType", { header: "Type", cell: SecondaryCells }),

        helperForColumns.accessor("region", { header: "Region", cell: SecondaryCells }),
        helperForColumns.accessor("tags", { header: "Tags", cell: TagCells }),

        helperForColumns.accessor("active", {
            header: ToggleHeader,
            filterFn: "equals",
            cell: ToggleCells,
        }),
    ];

    const table = useReactTable({
        data: tableResources,
        columns,
        meta: actions,
        state: {
            globalFilter: filter,
            pagination: forPagination,
        },
        getRowId: (row) => row.resourceId,
        onGlobalFilterChange: setFilter,
        getCoreRowModel: getCoreRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        getSortedRowModel: getSortedRowModel(),
        onPaginationChange: setForPagination,
        getPaginationRowModel: getPaginationRowModel(),
    });

    useEffect(() => {
        const mappedResources = realResources.map((resources) => ({
            resourceId: resources.resourceId,
            serviceType: resources.serviceCategory || "",
            resourceName: resources.name,
            resourceType: resources.resourceType || "Unknown",
            region: resources.region || "Unknown",
            tags: resources.tags || {},
            active: true,
            selected: false,
        }));
        setTableResources(mappedResources);
    }, [realResources]);

    const handlingSubmit = async (forEvent: React.SubmitEvent<HTMLFormElement>) => {
        forEvent.preventDefault();

        setForSaving(true);
        setErrors(null);

        try {
            const resourcesSelected: ResourceSelectionDto[] = tableResources
                .filter((forResources) => forResources.selected)
                .map((forResources) => ({
                    resourceId: forResources.resourceId,
                    serviceType: forResources.serviceType || "",
                    resourceType: forResources.resourceType || "",
                    resourceName: forResources.resourceName,
                    region: forResources.region || "",
                    tags: forResources.tags || {},
                    active: forResources.active,
                }));

            onNext({
                selectedResources: resourcesSelected,
                ingestionPeriod: forIngestionPeriod,
            });
        } catch {
            setErrors("Unable to complete GCP connection setup");
        } finally {
            setForSaving(false);
        }
    };

    return (
        <StepThree
            heading="Select instances"
            description="Select the instance you want CloudSherpa to monitor"
            onSubmit={handlingSubmit}
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
                        {tableResources.length > 0 && tableResources.every((r) => r.selected)
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
                    <ResourceTable table={table} columnsLength={columns.length} />

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

            <SliderForIngestion
                forIngestionPeriod={forIngestionPeriod}
                setForIngestionPeriod={setForIngestionPeriod}
                count={count}
                recIngestionPeriod={recIngestionPeriod}
                formattingSecond={formattingSecond}
            />
        </StepThree>
    );
}
