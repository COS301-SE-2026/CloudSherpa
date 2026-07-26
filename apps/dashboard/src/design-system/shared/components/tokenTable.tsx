"use client";

import { ColumnDef, flexRender, getCoreRowModel, useReactTable } from "@tanstack/react-table";
import { Card } from "@/components/atoms/card";
import SubSectionHeading from "./subsectionHeading";

interface TokenTableProps<TData> {
    title: string;
    description: string;
    //Im disabling this since we do know what will be passed in to the columns, it is just easier to use type any since multiple types of data are being passed to the component, so it should be safe
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    columns: ColumnDef<TData, any>[];
    data: TData[];
}

export function TokenTable<TData>({
    title,
    description,
    columns,
    data,
}: Readonly<TokenTableProps<TData>>) {
    const table = useReactTable({
        data,
        columns,
        getCoreRowModel: getCoreRowModel(),
    });

    return (
        <div className="space-y-6">
            <SubSectionHeading title={title} description={description} />
            <Card>
                <table className="min-w-full text-left table-fixed">
                    <thead className="border-b">
                        {table.getHeaderGroups().map((headerGroup) => (
                            <tr key={headerGroup.id}>
                                {headerGroup.headers.map((header) => (
                                    <th
                                        key={header.id}
                                        className="px-4 pb-3 font-semibold text-card-foreground"
                                        style={{ width: `${100 / columns.length}%` }}
                                    >
                                        {header.isPlaceholder
                                            ? null
                                            : flexRender(
                                                  header.column.columnDef.header,
                                                  header.getContext()
                                              )}
                                    </th>
                                ))}
                            </tr>
                        ))}
                    </thead>
                    <tbody className="divide-y pb-none">
                        {table.getRowModel().rows.map((row) => (
                            <tr key={row.id}>
                                {row.getVisibleCells().map((cell) => (
                                    <td key={cell.id} className="px-4 py-3">
                                        {flexRender(cell.column.columnDef.cell, cell.getContext())}
                                    </td>
                                ))}
                            </tr>
                        ))}
                    </tbody>
                </table>
            </Card>
        </div>
    );
}
