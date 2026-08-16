"use client";

import React from "react";
import {useReactTable, getCoreRowModel, getFilteredRowModel, getSortedRowModel, createColumnHelper, flexRender, type SortingState, type ColumnFiltersState, type HeaderContext, type CellContext, type Table as TableType} from "@tanstack/react-table";
import {ArrowUpDown} from "lucide-react";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/atoms/table";
import {Button} from "@/components/atoms/button";
import {Badge} from "@/components/atoms/badge";
import {Switch} from "@/components/atoms/switch";

/* copied the fucns from the resource manager to create a shared resource table*/

export interface Resource {
    id: string;
    name: string;
    type: string;
    region: string;
    tag: string[];
    status: "active" | "inactive";
}

interface ResourceAction {
    changeStatus: (id: string) => Promise<void>;
}

function ListOfTags({ tags }: Readonly<{ tags: string[] }>) {
    const displayedTags = tags.slice(0, 3);

    return (
        <div className="flex items-center gap-1 flex-wrap">
            {displayedTags.map((tag) => (
                <Badge key={tag} variant="secondary" className="text-[10px] font-normal">
                    {" "}
                    {tag}{" "}
                </Badge>
            ))}
        </div>
    );
}

export function ResourceHeaders<T extends Resource>({ column }: Readonly<HeaderContext<T, string>>) {
    return (
        <Button
            variant="ghost"
            size="sm"
            className="h-auto p-0 font-medium text-foreground hover:bg-transparent hover:text-foreground/80"
            onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
        >
            {" "}
            Resource <ArrowUpDown size={12} className="ml-1.5 text-muted-foreground" />
        </Button>
    );
}

export function ResourceCells<T extends Resource>({ getValue }: Readonly<CellContext<T, string>>) {
    return <span className="font-medium"> {getValue()} </span>;
}

export function SecondaryCells<T extends Resource>({ getValue }: Readonly<CellContext<T, string>>) {
    return <span className="text-xs text-muted-foreground"> {getValue()} </span>;
}

export function TagCells<T extends Resource>({ getValue }: Readonly<CellContext<T, string[]>>) {
    return <ListOfTags tags={getValue()} />;
}

export function ToggleHeader() {
    return <span className="block text-center"> Active/Inactive </span>;
}

export function ToggleCells<T extends Resource>({ row, table }: Readonly<CellContext<T, T["status"]>>) {
    const { changeStatus } = table.options.meta as ResourceAction;

    return (
        <div className="flex justify-center">
            <Switch
                checked={row.original.status === "active"}
                onCheckedChange={() => changeStatus(row.original.id)}
            />
        </div>
    );
}

const helperForColumns = createColumnHelper<Resource>();

export const columns = [
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

export function useSharedResourceTable<T extends Resource>(
    data : T[], changeStatus : (id : string) => Promise<void>, filter : string, setFilter : (value : string) => void
){
    const actions = React.useMemo<ResourceAction>(() => ({ changeStatus }), [changeStatus]);

    const [sort, setSort] = React.useState<SortingState>([]);
    
    const [filterColumn, setFilterColumn] = React.useState<ColumnFiltersState>([]);

    const table = useReactTable({
        data: data,
        columns,
        meta: actions,
        state: { globalFilter: filter, sorting: sort, columnFilters: filterColumn },

        getRowId: (row) => row.id,
        onGlobalFilterChange: setFilter,

        onSortingChange: setSort,
        onColumnFiltersChange: setFilterColumn,

        //returns all the rows without any filtering/sorting
        getCoreRowModel: getCoreRowModel(),

        //enables row filtering
        getFilteredRowModel: getFilteredRowModel(),

        //enables row sorting
        getSortedRowModel: getSortedRowModel(),
    });

    return table;
}

interface PropsForResourceTable<T extends Resource>{
    table : TableType<T>;
    columnsLength : number;
}

export function ResourceTable<T extends Resource>({
    table, columnsLength,
} : Readonly<PropsForResourceTable<T>>){
    return(
        <Table className="table-fixed w-full">
            <TableHeader>
                {table.getHeaderGroups().map((headerGroup) => (
                    <TableRow key={headerGroup.id} className="hover:bg-transparent">
                        {headerGroup.headers.map((header) => (
                            <TableHead key={header.id}>
                                {" "}
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
                            colSpan={columnsLength}
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
                                <TableCell key={cell.id}>
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
    );
}

export function filterForStatus(table : TableType<Resource>){
    const filterStatus = (table.getColumn("status")?.getFilterValue() as string | undefined) ?? "all";

    const setFilterStatus = (value: string) => {
        table.getColumn("status")?.setFilterValue(value === "all" ? undefined : value);
    };

    return {filterStatus, setFilterStatus};
}