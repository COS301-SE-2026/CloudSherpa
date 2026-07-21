"use client";

import { createColumnHelper } from "@tanstack/react-table";
import { Spacing } from "@/design-system/layout-and-spacing/types/layoutAndSpacing";
import { TokenTable } from "@/design-system/shared/components/tokenTable";

interface SpacingsProps {
    spacings: Spacing[];
}

const columnHelper = createColumnHelper<Spacing>();

const columns = [
    columnHelper.accessor("name", {
        header: "Token",
        cell: (info) => <span>p/m-{info.getValue()}</span>,
    }),
    columnHelper.accessor("value", {
        header: "Value",
        cell: (info) => <span className="text-neutral-500">{info.getValue()}</span>,
    }),
    columnHelper.display({
        id: "preview",
        header: "Preview",
        cell: (info) => {
            const space = info.row.original;
            return (
                <div
                    className="h-3 bg-primary rounded-full"
                    style={{
                        width: space.value,
                    }}
                />
            );
        },
    }),
];
export default function SpacingsProps({ spacings }: Readonly<SpacingsProps>) {
    return <TokenTable title="Spacing" columns={columns} data={spacings} />;
}
