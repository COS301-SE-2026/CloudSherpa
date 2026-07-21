import { Font_Family } from "@/design-system/typography/types/typography";
import { TokenTable } from "@/design-system/shared/components/tokenTable";
import { createColumnHelper } from "@tanstack/react-table";

interface FontFamilyProps {
    FontFamilies: Font_Family[];
}

const columnHelper = createColumnHelper<Font_Family>();

const columns = [
    columnHelper.accessor("name", {
        header: "Token",
        cell: (info) => <span>font-{info.getValue()}</span>,
    }),
    columnHelper.accessor("value", {
        header: "Value",
        cell: (info) => <span className=" text-neutral-500">{info.getValue()}</span>,
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
                        fontFamily: `var(--font-family-${size.name}, ${size.value})`,
                    }}
                >
                    Cloud analytics and finops solution
                </div>
            );
        },
    }),
];

export default function FontFamily({ FontFamilies }: Readonly<FontFamilyProps>) {
    return <TokenTable title="Font Families" columns={columns} data={FontFamilies} />;
}
