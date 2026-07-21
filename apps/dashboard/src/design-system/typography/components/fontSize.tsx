"use client";

import { createColumnHelper } from "@tanstack/react-table";
import { Font_Size } from "@/design-system/typography/types/typography";
import { TokenTable } from "@/design-system/shared/components/tokenTable";

interface FontSizeProps {
    FontSizes: Font_Size[];
}
const columnHelper = createColumnHelper<Font_Size>();

const columns = [
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
        cell: (info) => <span className="font-mono text-neutral-500">{info.getValue()}</span>,
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
                    Your finops solution
                </div>
            );
        },
    }),
];
export default function FontSize({ FontSizes }: Readonly<FontSizeProps>) {
    return <TokenTable title="Font Sizes" columns={columns} data={FontSizes} />;
}
