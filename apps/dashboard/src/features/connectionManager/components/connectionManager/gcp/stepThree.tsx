"use client";

import React, { useState, useEffect, useMemo, useCallback } from "react";
import { SliderForIngestion, StepThree } from "@/features/connectionManager/components/connectionManager/wizardSetup/stepThree";
import { Button } from "@/components/atoms/button";
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
import {Resource, ResourceHeaders, ResourceCells, SecondaryCells, TagCells, ToggleHeader, ToggleCells, ResourceTable} from "@/features/resourceManager/resourceTable";

/*
- should have tanstack table for resources, as elect & deselect all for it
- should also have pagination
*/
interface DetailsForResource {
    id: string;
    name: string;
    type?: string;
    region?: string;
    tag?: string[];
    status?: "active" | "inactive";
}

interface StepThreePropsForGcp {
    resources?: DetailsForResource[];
    onNext: (data: Record<string, unknown>) => void;
    onBack?: () => void;
    ingestionPeriod?: string;
}

interface Resources extends Resource{
    selected: boolean;
}

interface ActionForResource {
    changeStatus: (id: string) => Promise<void>;
    toggleResource: (id: string) => void;
    toggleAll: () => void;
}

const hardCodedResources: DetailsForResource[] = [
    {
        id: "resource1",
        name: "Resource one",
        type: "Service one",
        region: "region 1",
        tag: ["tag1", "tag2"],
    },
];

function SelectionHeader({ table }: Readonly<{ table: TableType<Resources> }>) {
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

function SelectionCells({ row, table }: Readonly<CellContext<Resources, boolean>>) {
    const { toggleResource } = table.options.meta as ActionForResource;

    return (
        <div className="flex justify-center">
            <input
                type="checkbox"
                checked={row.original.selected}
                onChange={() => {
                    toggleResource(row.original.id);
                }}
                className="w-4 h-4 rounded border-border bg-background text-primary focus:ring-primary"
            />
        </div>
    );
}

const helperForColumns = createColumnHelper<Resources>();

const columns = [
    helperForColumns.accessor("selected", {
        header: SelectionHeader,
        cell: SelectionCells,
    }),

    helperForColumns.accessor("name", { header: ResourceHeaders, cell: ResourceCells }),
    helperForColumns.accessor("type", { header: "Type", cell: SecondaryCells }),

    helperForColumns.accessor("region", { header: "Region", cell: SecondaryCells }),
    helperForColumns.accessor("tag", { header: "Tags", cell: TagCells }),

    helperForColumns.accessor("status", {
        header: ToggleHeader,
        filterFn: "equals",
        cell: ToggleCells,
    }),
];

export default function StepThreeGcp({
    resources = [],
    onNext,
    onBack,
    ingestionPeriod = "60",
}: Readonly<StepThreePropsForGcp>) {
    const [forPagination, setForPagination] = useState({ pageIndex: 0, pageSize: 8 });

    const [forSaving, setForSaving] = useState(false);

    const [errors, setErrors] = useState<string | null>(null);

    const [forIngestionPeriod, setForIngestionPeriod] = useState<string>(ingestionPeriod);

    const [tableResources, setTableResources] = useState<Resources[]>([]);

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
                resources.id === id
                    ? {
                          ...resources,
                          status: resources.status === "active" ? "inactive" : "active",
                      }
                    : resources
            )
        );
    }, []);

    const toggleResource = useCallback((resourceId: string) => {
        setTableResources((previous) =>
            previous.map((forResources) =>
                forResources.id === resourceId
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

    const table = useReactTable({
        data: tableResources,
        columns,
        meta: actions,
        state: {
            globalFilter: filter,
            pagination: forPagination,
        },
        getRowId: (row) => row.id,
        onGlobalFilterChange: setFilter,
        getCoreRowModel: getCoreRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        getSortedRowModel: getSortedRowModel(),
        onPaginationChange: setForPagination,
        getPaginationRowModel: getPaginationRowModel(),
    });

    useEffect(() => {
        const mappedResources: Resources[] = realResources.map((resources) => ({
            id: resources.id,
            name: resources.name,
            type: resources.type || "Unknown",
            region: resources.region || "Unknown",
            tag: resources.tag || ["No tags"],
            status: "active",
            selected: false,
        }));
        setTableResources(mappedResources);
    }, [realResources]);

    const handlingSubmit = async (forEvent: React.SubmitEvent<HTMLFormElement>) => {
        forEvent.preventDefault();

        setForSaving(true);
        setErrors(null);

        try {
            const resourcesSelected = tableResources
                .filter((forResources) => forResources.selected)
                .map((forResources) => forResources.id);

            onNext({
                selectedResources: resourcesSelected,
                ingestionPeriod: forIngestionPeriod,
                tableResources: tableResources,
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
                    <ResourceTable table = {table} columnsLength = {columns.length}/>

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

            <SliderForIngestion forIngestionPeriod = {forIngestionPeriod} setForIngestionPeriod = {setForIngestionPeriod}
                                count = {count} recIngestionPeriod = {recIngestionPeriod} formattingSecond = {formattingSecond}
            />
        </StepThree>
    );
}
