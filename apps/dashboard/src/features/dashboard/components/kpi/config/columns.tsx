"use client";

import { ColumnDef } from "@tanstack/react-table";
import { Checkbox } from "@/components/atoms/checkbox";
import { ChevronDown, ChevronUp } from "lucide-react";

export type KPIConfigTableRow = {
    chargeId: string;
    resourceId: string;
    service: string;
    provider: string;
};

export const kpiConfigColumns: ColumnDef<KPIConfigTableRow>[] = [
    {
        id: "select",
        size: 10,
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
        accessorKey: "service",
        header: "Service",
        size: 100,
        cell: ({ getValue }) => {
            const value = getValue<string>();

            return (
                <div className="truncate cursor-text" title={value}>
                    {value ?? "No Service Description"}
                </div>
            );
        },
    },
    // {
    //     accessorKey: "resourceName",
    //     header: "Resource Name",
    // },
    {
        accessorKey: "resourceId",
        header: "Resource ID",
        size: 250,
        cell: ({ getValue }) => {
            const value = getValue<string>();

            return (
                <div className="truncate cursor-text" title={value}>
                    {value ?? "No Resource ID"}
                </div>
            );
        },
    },

    {
        accessorKey: "provider",
        header: "Provider",
        size: 15,
    },
    {
        id: "rowDropdown",
        header: () => null,
        size: 30,
        cell: ({ row }) => (
            <>
                {row.getIsExpanded() ? (
                    <ChevronUp className="h-4 w-4 text-muted-foreground" />
                ) : (
                    <ChevronDown className="h-4 w-4 text-muted-foreground" />
                )}
            </>
        ),
    },
];
