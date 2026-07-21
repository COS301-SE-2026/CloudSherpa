import { useMemo } from "react";
import {
    createColumnHelper,
    flexRender,
    getCoreRowModel,
    useReactTable,
} from "@tanstack/react-table";
import { Breakpoint } from "@/design-system/layout-and-spacing/types/layoutAndSpacing";

interface BreakPointsoProps {
    breakpoints: Breakpoint[];
}

const columnHelper = createColumnHelper<Breakpoint>();

export default function Breakpoints({ breakpoints }: Readonly<BreakPointsoProps>) {
    const columns = useMemo(
        () => [
            columnHelper.accessor("name", {
                header: "Token",
                cell: (info) => <span>{info.getValue()}</span>,
            }),
            columnHelper.accessor("value", {
                header: "Value",
                cell: (info) => <span className="text-neutral-500">{info.getValue()}</span>,
            }),
        ],
        []
    );

    const table = useReactTable({
        data: breakpoints,
        columns,
        getCoreRowModel: getCoreRowModel(),
    });

    return (
        <div className="space-y-4 mb-8">
            <h2 className="text-2xl font-bold">Breakpoints</h2>
            <div className="w-full border border-neutral-200 dark:border-neutral-800 rounded-lg overflow-hidden">
                <table className="min-w-full text-sm text-left">
                    <thead className="bg-neutral-50 dark:bg-neutral-900 border-b border-neutral-200 dark:border-neutral-800">
                        {table.getHeaderGroups().map((headerGroup) => (
                            <tr key={headerGroup.id}>
                                {headerGroup.headers.map((header) => (
                                    <th
                                        key={header.id}
                                        className="px-4 py-3 font-semibold text-neutral-700 dark:text-neutral-300"
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
                    <tbody className="divide-y divide-neutral-100 dark:divide-neutral-800">
                        {table.getRowModel().rows.map((row) => (
                            <tr
                                key={row.id}
                                className="hover:bg-neutral-50 dark:hover:bg-neutral-900 transition-colors"
                            >
                                {row.getVisibleCells().map((cell) => (
                                    <td key={cell.id} className="px-4 py-3">
                                        {flexRender(cell.column.columnDef.cell, cell.getContext())}
                                    </td>
                                ))}
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
