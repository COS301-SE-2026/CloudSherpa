"use client";

import type { HeaderGroup } from "@tanstack/react-table";
import { flexRender } from "@tanstack/react-table";
import { ArrowUp, ArrowDown } from "lucide-react";
import { TableHead, TableHeader, TableRow } from "@/components/atoms/table";

interface PropsForTableHeader<TData> {
    headerGroups: HeaderGroup<TData>[];
}

export function TableHeaderData<TData>({ headerGroups }: Readonly<PropsForTableHeader<TData>>) {
    return (
        //copied from other files
        <TableHeader>
            {headerGroups.map((forHeaderGroups) => (
                <TableRow key={forHeaderGroups.id} className="hover:bg-transparent">
                    {forHeaderGroups.headers.map((header) => {
                        const ableToSort = header.column.getCanSort();

                        const sorted = header.column.getIsSorted();

                        return (
                            <TableHead
                                key={header.id}
                                className="text-xs font-semibold uppercase tracking-wider text-muted-foreground"
                            >
                                {ableToSort ? (
                                    <button
                                        type="button"
                                        onClick={header.column.getToggleSortingHandler()}
                                        className="inline-flex items-center gap-1 rounded-sm uppercase tracking-wider hover:text-foreground"
                                    >
                                        {flexRender(
                                            header.column.columnDef.header,
                                            header.getContext()
                                        )}

                                        {sorted === "asc" && <ArrowUp className="h-3.5 w-3.5" />}

                                        {sorted === "desc" && <ArrowDown className="h-3.5 w-3.5" />}
                                    </button>
                                ) : (
                                    flexRender(header.column.columnDef.header, header.getContext())
                                )}
                            </TableHead>
                        );
                    })}
                </TableRow>
            ))}
        </TableHeader>
    );
}
