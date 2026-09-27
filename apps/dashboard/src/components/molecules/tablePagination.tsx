"use client";

import { ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "@/components/atoms/button";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/atoms/select";
import type { Table } from "@tanstack/react-table";

interface PropsForPagination<TData> {
    forTable: Table<TData>;
}

const NUMBER_OF_PAGES = [5, 10, 15, 20];

export function TablePagination<TData>({ forTable }: Readonly<PropsForPagination<TData>>) {
    const currentPage = forTable.getState().pagination.pageIndex + 1;

    const totalPages = forTable.getPageCount();

    const pageSize = forTable.getState().pagination.pageSize;

    const totalRows = forTable.getFilteredRowModel().rows.length;

    const firstRow = totalRows === 0 ? 0 : (currentPage - 1) * pageSize + 1;

    const lastRow = Math.min(currentPage * pageSize, totalRows);

    return (
        <div className="flex items-center justify-between border-t border-border px-4 py-3">
            <span className="text-xs text-muted-foreground">
                {" "}
                Showing {firstRow}-{lastRow} of {totalRows}{" "}
            </span>

            <div className="flex items-center gap-2">
                <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => forTable.previousPage()}
                    disabled={!forTable.getCanPreviousPage()}
                    className="h-7 w-7 text-muted-foreground hover:text-foreground"
                >
                    {" "}
                    <ChevronLeft className="h-4 w-4" />{" "}
                </Button>

                <span className="text-xs text-muted-foreground">
                    {" "}
                    Page {currentPage} of {totalPages}{" "}
                </span>

                <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => forTable.nextPage()}
                    disabled={!forTable.getCanNextPage()}
                    className="h-7 w-7 text-muted-foreground hover:text-foreground"
                >
                    {" "}
                    <ChevronRight className="h-4 w-4" />{" "}
                </Button>
            </div>

            <div className="flex items-center gap-2">
                <span className="text-xs text-muted-foreground"> Rows per page: </span>

                <Select
                    value={String(pageSize)}
                    onValueChange={(forValue) => forTable.setPageSize(Number(forValue))}
                >
                    <SelectTrigger className="h-7 w-[60px] text-xs">
                        {" "}
                        <SelectValue />{" "}
                    </SelectTrigger>

                    <SelectContent>
                        {NUMBER_OF_PAGES.map((forSize) => (
                            <SelectItem key={forSize} value={String(forSize)}>
                                {" "}
                                {forSize}{" "}
                            </SelectItem>
                        ))}
                    </SelectContent>
                </Select>
            </div>
        </div>
    );
}
