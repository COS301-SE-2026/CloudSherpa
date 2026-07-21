import {
    createColumnHelper,
    flexRender,
    getCoreRowModel,
    useReactTable,
} from "@tanstack/react-table";
import { useMemo } from "react";
import { Font_Weight } from "@/design-system/typography/types/typography";

interface FontWeightProps {
    Font_Weights: Font_Weight[];
}

const columnHelper = createColumnHelper<Font_Weight>();

export default function FontWeight({ Font_Weights }: Readonly<FontWeightProps>) {
    const columns = useMemo(
        () => [
            columnHelper.accessor("name", {
                header: "Token",
                cell: (info) => (
                    <span className="font-mono text-brand-600 dark:text-brand-400">
                        text-{info.getValue()}
                    </span>
                ),
            }),
            columnHelper.accessor("value", {
                header: "Value",
                cell: (info) => (
                    <span className="font-mono text-neutral-500">{info.getValue()}</span>
                ),
            }),
            columnHelper.display({
                id: "preview",
                header: "Preview",
                cell: (info) => {
                    const size = info.row.original;
                    return (
                        <div
                            className="truncate text-foreground"
                            style={{
                                fontSize: `var(--font-size-${size.name}, ${size.value})`,
                            }}
                        >
                            Cloud analytics and finops solution
                        </div>
                    );
                },
            }),
        ],
        []
    );

    const table = useReactTable({
        data: Font_Weights,
        columns,
        getCoreRowModel: getCoreRowModel(),
    });

    return (
        <div className="space-y-4 mb-8">
            <h3 className="text-2xl font-bold">Font Weights</h3>
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
