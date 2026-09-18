"use client";

import { useMemo } from "react";
import {
    createColumnHelper,
    flexRender,
    getCoreRowModel,
    useReactTable,
} from "@tanstack/react-table";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/atoms/table";

type generatedHistory = {
    id: string;
    date: string;
    time: string;
};

const mockPastDashboards: generatedHistory[] = [
    { id: "dash-aws-cost-opt", date: "2026-09-18", time: "14:32:10" },
    { id: "dash-aws-cost-opt", date: "2026-09-18", time: "14:32:10" },
    { id: "dash-aws-cost-opt", date: "2026-09-18", time: "14:32:10" },
    { id: "dash-aws-cost-opt", date: "2026-09-18", time: "14:32:10" },
    { id: "dash-aws-cost-opt", date: "2026-09-18", time: "14:32:10" },
    { id: "dash-aws-cost-opt", date: "2026-09-18", time: "14:32:10" },
];

const colHelper = createColumnHelper<generatedHistory>();

export default function History() {
    const columns = useMemo(
        () => [
            colHelper.accessor("id", {
                header: "Dashboard",
                cell: (info) => (
                    <span className="font-medium text-muted-foreground truncate max-w-[150px] block">
                        {info.getValue()}
                    </span>
                ),
            }),
            colHelper.accessor("date", {
                header: "Date",
                cell: (info) => info.getValue(),
            }),
            colHelper.accessor("time", {
                header: () => <div className="text-right">Time</div>,
                cell: (info) => <div className="text-right">{info.getValue()}</div>,
            }),
        ],
        []
    );

    //init
    const table = useReactTable({
        data: mockPastDashboards,
        columns,
        getCoreRowModel: getCoreRowModel(),
    });

    return (
        <div className="h-full w-full overflow-y-auto border rounded-lg">
            <Table>
                <TableHeader className="sticky top-0 bg-background z-10 shadow-sm">
                    {table.getHeaderGroups().map((headerGroup) => (
                        <TableRow key={headerGroup.id}>
                            {headerGroup.headers.map((header) => (
                                <TableHead key={header.id}>
                                    {header.isPlaceholder
                                        ? null
                                        : flexRender(
                                              header.column.columnDef.header,
                                              header.getContext()
                                          )}
                                </TableHead>
                            ))}
                        </TableRow>
                    ))}
                </TableHeader>
                <TableBody>
                    {table.getRowModel().rows?.length ? (
                        table.getRowModel().rows.map((row) => (
                            <TableRow
                                key={row.id}
                                data-state={row.getIsSelected() && "selected"}
                                className="cursor-pointer hover:bg-muted/50 transition-colors"
                            >
                                {row.getVisibleCells().map((cell) => (
                                    <TableCell key={cell.id}>
                                        {flexRender(cell.column.columnDef.cell, cell.getContext())}
                                    </TableCell>
                                ))}
                            </TableRow>
                        ))
                    ) : (
                        <TableRow>
                            <TableCell colSpan={columns.length} className="h-24 text-center">
                                No history found.
                            </TableCell>
                        </TableRow>
                    )}
                </TableBody>
            </Table>
        </div>
    );
}
