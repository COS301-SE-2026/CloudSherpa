import { createColumnHelper } from "@tanstack/react-table";
import { Font_Weight } from "@/design-system/typography/types/typography";
import { TokenTable } from "@/design-system/shared/components/tokenTable";

interface FontWeightProps {
    Font_Weights: Font_Weight[];
}

const columnHelper = createColumnHelper<Font_Weight>();

const columns = [
    columnHelper.accessor("name", {
        header: "Token",
        cell: (info) => (
            <span className="font-mono">
                text-{info.getValue()}
            </span>
        ),
    }),
    columnHelper.accessor("value", {
        header: "Value",
        cell: (info) => <span className="font-mono text-muted-foreground">{info.getValue()}</span>,
    }),
    columnHelper.display({
        id: "preview",
        header: "Preview",
        cell: (info) => {
            const weight = info.row.original;
            return (
                <div
                    className="truncate text-foreground"
                    style={{
                        fontWeight: `var(--font-weight-${weight.name}, ${weight.value})`,
                    }}
                >
                    Your finops solution
                </div>
            );
        },
    }),
];

export default function FontWeight({ Font_Weights }: Readonly<FontWeightProps>) {
    return (
        <TokenTable
            title="Font Weights"
            columns={columns}
            data={Font_Weights}
            description="Some text about font weights"
        />
    );
}
