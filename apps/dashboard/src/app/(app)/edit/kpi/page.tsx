"use client";

import { Button } from "@/components/atoms/button";
import { Card, CardTitle } from "@/components/atoms/card";
import {
    Field,
    FieldDescription,
    FieldGroup,
    FieldLabel,
    FieldLegend,
    FieldSeparator,
    FieldSet,
} from "@/components/atoms/field";
import { FormCountCircle } from "@/components/atoms/form-count-circle";
import { Input } from "@/components/atoms/input";
import { InputGroup, InputGroupAddon, InputGroupInput } from "@/components/atoms/input-group";
import {
    Select,
    SelectContent,
    SelectGroup,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/atoms/select";
import { SearchIcon } from "lucide-react";

import {
    KPIConfigTableRow,
    kpiConfigColumns,
} from "@/features/dashboard/components/kpi/config/columns";
import { KPIConfigTable } from "@/features/dashboard/components/kpi/config/config-table";

const mockKpiConfigRows: KPIConfigTableRow[] = [
    {
        resourceName: "Production API Gateway",
        resourceId: "api-gw-prod-01",
        service: "API Gateway",
        provider: "AWS",
        connection: "Production AWS",
    },
    {
        resourceName: "Customer Database",
        resourceId: "rds-customer-prod",
        service: "RDS",
        provider: "AWS",
        connection: "Production AWS",
    },
    {
        resourceName: "Billing Worker Cluster",
        resourceId: "eks-billing-workers",
        service: "EKS",
        provider: "AWS",
        connection: "Production AWS",
    },
    {
        resourceName: "Analytics Storage Bucket",
        resourceId: "s3-analytics-events",
        service: "S3",
        provider: "AWS",
        connection: "Data AWS",
    },
    {
        resourceName: "Cloud Cost Export",
        resourceId: "bigquery-cost-export",
        service: "BigQuery",
        provider: "GCP",
        connection: "Finance GCP",
    },
];

export default function EditKpiPage() {
    const mockConnections = [
        { label: "All connections", value: "all" },
        { label: "Connection-1", value: "connection-1" },
        { label: "Connection-2", value: "connection-2" },
        { label: "Connection-3", value: "connection-3" },
    ];

    return (
        <main className="flex flex-1 flex-col gap-6 p-6 lg:p-8 w-full mx-auto">
            <div className="flex flex-row gap-6">
                <h1 className="text-2xl">KPI Configuration</h1>
                <Button variant={"default"}>Save KPI</Button>
                <Button variant={"secondary"}>Cancel</Button>
            </div>
            <div className="grid grid-cols-[2fr_1fr] gap-4 h-full">
                <Card className="p-6">
                    <FieldSet>
                        <div className="flex flex-row items-center gap-3">
                            <FormCountCircle count={1} />
                            <FieldLegend className="mb-0">KPI Details</FieldLegend>
                        </div>
                        <FieldDescription>
                            Choose the title that will appear on the dashboard card.
                        </FieldDescription>
                        <FieldGroup>
                            <Field>
                                <FieldLabel>Card Title</FieldLabel>
                                <Input placeholder="Card Title"></Input>
                            </Field>
                        </FieldGroup>
                    </FieldSet>
                    <FieldSeparator></FieldSeparator>
                    <FieldSet>
                        <div className="flex flex-row items-center gap-3">
                            <FormCountCircle count={2} />
                            <FieldLegend className="mb-0">Resources</FieldLegend>
                        </div>
                        <FieldDescription>
                            Select the resources whose costs should be aggregated.
                        </FieldDescription>
                        <div className="grid grid-cols-[1fr_2fr] gap-6">
                            <div>
                                <FieldLabel>Connection</FieldLabel>
                                <Select defaultValue="all">
                                    <SelectTrigger className="w-full">
                                        <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectGroup>
                                            {mockConnections.map((connection) => (
                                                <SelectItem
                                                    key={connection.value}
                                                    value={connection.value}
                                                >
                                                    {connection.label}
                                                </SelectItem>
                                            ))}
                                        </SelectGroup>
                                    </SelectContent>
                                </Select>
                            </div>
                            <div>
                                <FieldLabel>Search Resources</FieldLabel>
                                <InputGroup className="w-80">
                                    <InputGroupInput placeholder="Search by resource name or ID" />
                                    <InputGroupAddon>
                                        <SearchIcon />
                                    </InputGroupAddon>
                                </InputGroup>
                            </div>
                        </div>
                    </FieldSet>
                    <KPIConfigTable columns={kpiConfigColumns} data={mockKpiConfigRows} />
                    <FieldSeparator />
                </Card>

                <Card className="p-6">
                    <CardTitle>Preview</CardTitle>
                </Card>
            </div>
        </main>
    );
}
