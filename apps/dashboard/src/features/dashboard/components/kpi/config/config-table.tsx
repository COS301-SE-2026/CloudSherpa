import {
    ColumnDef,
    flexRender,
    getCoreRowModel,
    useReactTable,
    ColumnFiltersState,
    getFilteredRowModel,
    getPaginationRowModel,
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
import React, { useEffect } from "react";
import { DataTablePagination } from "./config-table-pagination";
import { CloudProviderEnum } from "@/features/dashboard/types/provider";
import { Spinner } from "@/components/atoms/spinner";

interface KPIConfigTableProps<TData, TValue> {
    readonly columns: ColumnDef<TData, TValue>[];
    readonly data: TData[];
    readonly onSetSelectedRows: (rows: TData[]) => void;
    readonly error: boolean;
    readonly loading: boolean;
}

const ALL_PROVIDERS = "All Providers";
const providers: CloudProviderEnum[] = ["AWS", "Azure", "GCP"];

export function KPIConfigTable<TData, TValue>({
    columns,
    data,
    onSetSelectedRows,
    error,
    loading,
}: KPIConfigTableProps<TData, TValue>) {
    const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([]);
    const [globalFilter, setGlobalFilter] = React.useState("");
    const [rowSelection, setRowSelection] = React.useState({});

    const table = useReactTable({
        data,
        columns,
        getCoreRowModel: getCoreRowModel(),
        onColumnFiltersChange: setColumnFilters,
        getFilteredRowModel: getFilteredRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        onRowSelectionChange: setRowSelection,
        state: {
            columnFilters,
            globalFilter,
            rowSelection,
        },
    });

    useEffect(() => {
        const selectedData = table.getSelectedRowModel().rows.map((row) => row.original);
        onSetSelectedRows(selectedData);
    }, [rowSelection, table]);

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
            <TableRow key={row.id} data-state={row.getIsSelected() && "selected"}>
                {row.getVisibleCells().map((cell) => (
                    <TableCell
                        key={cell.id}
                        style={{
                            width: cell.column.getSize(),
                            maxWidth: cell.column.getSize(),
                        }}
                    >
                        {flexRender(cell.column.columnDef.cell, cell.getContext())}
                    </TableCell>
                ))}
            </TableRow>
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
