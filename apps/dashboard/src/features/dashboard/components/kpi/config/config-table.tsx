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

interface KPIConfigTableProps<TData, TValue> {
    readonly columns: ColumnDef<TData, TValue>[];
    readonly data: TData[];
    readonly providers: CloudProviderEnum[];
    readonly onSetSelectedRows: (rows: TData[]) => void;
}

export function KPIConfigTable<TData, TValue>({
    columns,
    data,
    providers,
    onSetSelectedRows,
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
                                value={
                                    (table.getColumn("provider")?.getFilterValue() as string) ??
                                    providers[0]
                                }
                                onValueChange={(value) =>
                                    table
                                        .getColumn("provider")
                                        ?.setFilterValue(
                                            value === "All Providers" ? undefined : value
                                        )
                                }
                            >
                                <SelectTrigger className="w-full">
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectGroup>
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
                                        <TableHead key={header.id}>
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
                    <TableBody>
                        {table.getRowModel().rows?.length ? (
                            table.getRowModel().rows.map((row) => (
                                <TableRow
                                    key={row.id}
                                    data-state={row.getIsSelected() && "selected"}
                                >
                                    {row.getVisibleCells().map((cell) => (
                                        <TableCell key={cell.id}>
                                            {flexRender(
                                                cell.column.columnDef.cell,
                                                cell.getContext()
                                            )}
                                        </TableCell>
                                    ))}
                                </TableRow>
                            ))
                        ) : (
                            <TableRow>
                                <TableCell colSpan={columns.length} className="h-24 text-center">
                                    No results.
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </div>
            <DataTablePagination table={table} />
        </>
    );
}
