"use client";

import { createColumnHelper } from "@tanstack/react-table";
import { TokenTable } from "@/design-system/shared/components/tokenTable";

type MockServerData = {
    resourceId: string;
    region: string;
};

const mockDataArray: MockServerData[] = [
    { resourceId: "srv-prod-01", region: "us-east-1" },
    { resourceId: "db-replica-02", region: "eu-west-2" },
    { resourceId: "cache-node-04", region: "ap-south-1" },
];

const columnHelper = createColumnHelper<MockServerData>();

const columns = [
    columnHelper.accessor("resourceId", {
        header: "Resource ID",
        cell: (info) => <span className="font-mono font-medium">{info.getValue()}</span>,
    }),
    columnHelper.accessor("region", {
        header: "Region",
        cell: (info) => <span className="text-muted-foreground">{info.getValue()}</span>,
    }),
];

export default function TablesShowcase() {
    return (
        <div className="space-y-6">
            <TokenTable
                title="Tables"
                description="Considering CloudSherpa is a data heavy project, tables are indispensable part of our design. We use tanstack tables instead of shadcn tables since it has quality of life features baked in like
                predefined support for pagination, filtering etc."
                columns={columns}
                data={mockDataArray}
            />
        </div>
    );
}
