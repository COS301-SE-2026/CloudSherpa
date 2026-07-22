import { createColumnHelper } from "@tanstack/react-table";
import { Breakpoint } from "@/design-system/layout-and-spacing/types/layoutAndSpacing";
import { TokenTable } from "@/design-system/shared/components/tokenTable";

interface BreakPointsoProps {
    breakpoints: Breakpoint[];
}

const columnHelper = createColumnHelper<Breakpoint>();

const columns = [
    columnHelper.accessor("name", {
        header: "Token",
        cell: (info) => <span>{info.getValue()}</span>,
    }),
    columnHelper.accessor("value", {
        header: "Value",
        cell: (info) => <span className="text-neutral-500">{info.getValue()}</span>,
    }),
];

export default function Breakpoints({ breakpoints }: Readonly<BreakPointsoProps>) {
    return (
        <TokenTable
            title="Breakpoints"
            columns={columns}
            data={breakpoints}
            description="some text about breakpoints"
        />
    );
}
