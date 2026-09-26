import {
    ColumnDef,
    flexRender,
    getCoreRowModel,
    useReactTable,
    ColumnFiltersState,
    getFilteredRowModel,
    getPaginationRowModel,
    RowSelectionState,
    getExpandedRowModel,
} from "@tanstack/react-table";

import {
    FieldDescription,
    FieldGroup,
    FieldLabel,
    FieldLegend,
    FieldSet,
} from "@/components/atoms/field";
import { InputGroup, InputGroupAddon, InputGroupInput } from "@/components/atoms/input-group";
import {
    Select,
    SelectContent,
    SelectGroup,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/atoms/select";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/atoms/table";
import { SearchIcon } from "lucide-react";
import { FormCountCircle } from "@/components/atoms/form-count-circle";
import React, { useMemo, Fragment } from "react";
import { DataTablePagination } from "./config-table-pagination";
import { CloudProviderEnum } from "@/features/dashboard/types/provider";
import { Spinner } from "@/components/atoms/spinner";
import { KPIConfigTableRow } from "./columns";
import { Card, CardHeader, CardContent, CardTitle, CardDescription } from "@/components/atoms/card";

interface KPIConfigTableProps<TValue> {
    readonly columns: ColumnDef<KPIConfigTableRow, TValue>[];
    readonly data: KPIConfigTableRow[];
    readonly onSetChargeIdsChange: (chargeIds: string[]) => void;
    readonly selectedChargeIds: string[];
    readonly error: boolean;
    readonly loading: boolean;
}

const ALL_PROVIDERS = "All Providers";
const providers: CloudProviderEnum[] = ["AWS", "Azure", "GCP"];

function RoundUp(value: number, decimals: number): string {
    return String(Number(value.toFixed(decimals))); //number strips unnecessary leading 0's, toFixed rounds up and extends to 5 decimals
}

export function KPIConfigTable<TValue>({
    columns,
    data,
    onSetChargeIdsChange,
    selectedChargeIds,
    error,
    loading,
}: KPIConfigTableProps<TValue>) {
    const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([]);
    const [globalFilter, setGlobalFilter] = React.useState("");
    // Memoized to prevent infinite rerenders (when using useEffect with rowSelection as dependency)
    // when the initial RowSelectionState is set, so it will only change when selectedChargeIds is different
    // from the currently memoized selectedChargeIds
    const rowSelection = useMemo<RowSelectionState>(
        () => Object.fromEntries(selectedChargeIds.map((chargeId) => [chargeId, true])),
        [selectedChargeIds]
    );

    const table = useReactTable({
        data,
        columns,
        getCoreRowModel: getCoreRowModel(),
        onColumnFiltersChange: setColumnFilters,
        getFilteredRowModel: getFilteredRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        getExpandedRowModel: getExpandedRowModel(),
        getRowCanExpand: () => true,
        onRowSelectionChange: (updater) => {
            // tanstack behaviour: updater takes old RowSelectionState as argument and returns the new RowSelectionState based on
            // what was now selected
            const next = typeof updater === "function" ? updater(rowSelection) : updater;
            // RowSelectionState comprises of an object that has a key value pair for each row id where
            // the key is the row id (here set by below code getRowId) and the value is the boolean, hence
            // we filter on nex[chargeId] which resolves to a boolean
            onSetChargeIdsChange(Object.keys(next).filter((chargeId) => next[chargeId]));
        },
        getRowId: (row) => row.chargeId,
        state: {
            columnFilters,
            globalFilter,
            rowSelection,
        },
    });

    const selectedProvider =
        (table.getColumn("provider")?.getFilterValue() as string | undefined) ?? ALL_PROVIDERS;
    let tableBodyContent: React.ReactNode;

    if (loading) {
        tableBodyContent = (
            <TableRow>
                <TableCell colSpan={columns.length} className="h-24">
                    <div className="flex h-full items-center justify-center text-muted-foreground">
                        <Spinner />
                    </div>
                </TableCell>
            </TableRow>
        );
    } else if (error) {
        tableBodyContent = (
            <TableRow>
                <TableCell colSpan={columns.length} className="h-24 text-center">
                    <div className="flex h-full flex-col items-center justify-center gap-1 text-sm">
                        <p className="font-medium text-destructive">Failed to load resources</p>
                    </div>
                </TableCell>
            </TableRow>
        );
    } else if (table.getRowModel().rows?.length) {
        tableBodyContent = table.getRowModel().rows.map((row) => (
            <Fragment key={row.id}>
                <TableRow
                    data-state={row.getIsSelected() && "selected"}
                    onClick={row.getToggleExpandedHandler()}
                    className="cursor-pointer select-none [&_*]:cursor-pointer" // [&_*] tailwind class modifier adds specified class to component and all it's children
                >
                    {row.getVisibleCells().map((cell) => (
                        <TableCell
                            key={cell.id}
                            style={{
                                width: cell.column.getSize(),
                                maxWidth: cell.column.getSize(),
                            }}
                            onClick={(e) => {
                                if (cell.column.id === "select") {
                                    e.stopPropagation();
                                }
                            }}
                            className="cursor-pointer"
                        >
                            {flexRender(cell.column.columnDef.cell, cell.getContext())}
                        </TableCell>
                    ))}
                </TableRow>
                {row.getIsExpanded() && (
                    <TableRow className="bg-muted hover:bg-muted">
                        <TableCell colSpan={row.getVisibleCells().length}>
                            <Card>
                                <CardHeader>
                                    <div className="flex items-center justify-between">
                                        <div className="space-y-1">
                                            <CardTitle className="text-sm font-semibold">
                                                Resource Details
                                            </CardTitle>
                                            <CardDescription className="text-xs">
                                                Extended metadata for this resource
                                            </CardDescription>
                                        </div>
                                    </div>
                                </CardHeader>
                                <CardContent className="flex flex-col gap-4 w-full text-xs  font-medium text-foreground">
                                    <div className="grid grid-cols-2 gap-4 md:grid-cols-3 text-sm">
                                        <div className="flex flex-col gap-1">
                                            <span className="text-muted-foreground">
                                                Cloud Provider
                                            </span>
                                            <span>{row.original.provider}</span>
                                        </div>
                                        <div className="flex flex-col gap-1">
                                            <span className="text-muted-foreground">
                                                Service Type
                                            </span>
                                            <span>{row.original.service}</span>
                                        </div>
                                        {row.original.resourceName && (
                                            <div className="flex flex-col gap-1">
                                                <span className="text-muted-foreground">
                                                    Resource Name
                                                </span>
                                                <span>
                                                    {row.original.resourceName || "No Name"}
                                                </span>
                                            </div>
                                        )}
                                        <div className="flex flex-col gap-1">
                                            <span className="text-muted-foreground">
                                                Charge Cost
                                            </span>
                                            <span>${RoundUp(row.original.chargeCost, 5)}</span>
                                        </div>
                                    </div>
                                    <div className="flex flex-col gap-4">
                                        <div className="flex flex-col gap-1">
                                            <span className="text-muted-foreground">
                                                Resource ID
                                            </span>
                                            <span
                                                className="text-wrap font-mono"
                                                title={row.original.resourceId}
                                            >
                                                {row.original.resourceId}
                                            </span>
                                        </div>
                                    </div>
                                </CardContent>
                            </Card>
                        </TableCell>
                    </TableRow>
                )}
            </Fragment>
        ));
    } else {
        tableBodyContent = (
            <TableRow>
                <TableCell colSpan={columns.length} className="h-24 text-center">
                    No results.
                </TableCell>
            </TableRow>
        );
    }

    return (
        <>
            <FieldSet>
                <div className="flex flex-row items-center gap-3">
                    <FormCountCircle count={2} />
                    <FieldLegend className="mb-0">Resources</FieldLegend>
                </div>
                <FieldDescription>
                    Select the resources whose costs should be aggregated.
                </FieldDescription>
                <FieldGroup>
                    <div className="grid grid-cols-[1fr_2fr] gap-6">
                        <div>
                            <FieldLabel>Cloud Provider</FieldLabel>
                            <Select
                                value={selectedProvider}
                                onValueChange={(value) =>
                                    table
                                        .getColumn("provider")
                                        ?.setFilterValue(
                                            value === ALL_PROVIDERS ? undefined : value
                                        )
                                }
                            >
                                <SelectTrigger className="w-full">
                                    <SelectValue placeholder={providers[0]} />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectGroup>
                                        <SelectItem key={ALL_PROVIDERS} value={ALL_PROVIDERS}>
                                            {ALL_PROVIDERS}
                                        </SelectItem>
                                        {providers.map((provider) => (
                                            <SelectItem key={provider} value={provider}>
                                                {provider}
                                            </SelectItem>
                                        ))}
                                    </SelectGroup>
                                </SelectContent>
                            </Select>
                        </div>
                        <div>
                            <FieldLabel>Search Resources</FieldLabel>
                            <InputGroup className="w-80">
                                <InputGroupInput
                                    placeholder="Search Resources"
                                    value={globalFilter ?? ""}
                                    onChange={(e) => setGlobalFilter(e.target.value)}
                                />
                                <InputGroupAddon>
                                    <SearchIcon />
                                </InputGroupAddon>
                            </InputGroup>
                        </div>
                    </div>
                </FieldGroup>
            </FieldSet>

            <div className="overflow-hidden rounded-md border">
                <Table>
                    <TableHeader>
                        {table.getHeaderGroups().map((headerGroup) => (
                            <TableRow key={headerGroup.id}>
                                {headerGroup.headers.map((header) => {
                                    return (
                                        <TableHead
                                            key={header.id}
                                            style={{ width: header.getSize() }}
                                        >
                                            {header.isPlaceholder
                                                ? null
                                                : flexRender(
                                                      header.column.columnDef.header,
                                                      header.getContext()
                                                  )}
                                        </TableHead>
                                    );
                                })}
                            </TableRow>
                        ))}
                    </TableHeader>
                    <TableBody>{tableBodyContent}</TableBody>
                </Table>
            </div>
            <DataTablePagination table={table} />
        </>
    );
}
