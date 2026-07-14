"use client";

import { ColumnDef } from "@tanstack/react-table";
import { Checkbox } from "@/components/atoms/checkbox";

export type KPIConfigTableRow = {
    resourceName: string;
    resourceId: string;
    service: string;
    provider: string;
    connection: string;
};

export const kpiConfigColumns: ColumnDef<KPIConfigTableRow>[] = [
    {
        id: "select",
        header: ({ table }) => (
            <Checkbox
                checked={
                    table.getIsAllPageRowsSelected() ||
                    (table.getIsSomePageRowsSelected() && "indeterminate")
                }
                onCheckedChange={(value) => table.toggleAllPageRowsSelected(!!value)}
                aria-label="Select all"
            />
        ),
        cell: ({ row }) => (
            <Checkbox
                checked={row.getIsSelected()}
                onCheckedChange={(value) => row.toggleSelected(!!value)}
                aria-label="Select row"
            />
        ),
        enableSorting: false,
        enableHiding: false,
    },
    {
        accessorKey: "resourceName",
        header: "Resource Name",
    },
    {
        accessorKey: "resourceId",
        header: "Resource ID",
    },
    {
        accessorKey: "service",
        header: "Service",
    },
    {
        accessorKey: "provider",
        header: "Provider",
    },
    {
        accessorKey: "connection",
        header: "Connection",
    },
];
