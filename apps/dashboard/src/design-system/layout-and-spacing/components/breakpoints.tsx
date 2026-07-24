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
        cell: (info) => <span className="text-muted-foreground">{info.getValue()}</span>,
    }),
];

export default function Breakpoints({ breakpoints }: Readonly<BreakPointsoProps>) {
    return (
        <TokenTable
            title="Breakpoints"
            columns={columns}
            data={breakpoints}
            description="Breakpoints are crucial for a responsive design. Each breakpoint represents a screen size. We design our ui with a mobile first approach in mind to align with the breakpoints.
            If the current screen the user is viewing CloudSherpa on exceeds a specific breakpoint size like 640 pixels in width then it signals our ui to update either the layout or size of the components being
            rendered on screen to change or enlarge to fit the new screen size. An example use case would be <button className='w-30 md:w-40'/> where w-30 represents the mobile button width and w-40 the width defined for the button
            for screens larger than 640 pixels, meaning small screens."
        />
    );
}
