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
import { SearchIcon, ChevronDownIcon } from "lucide-react";

import {
    KPIConfigTableRow,
    kpiConfigColumns,
} from "@/features/dashboard/components/kpi/config/columns";
import { KPIConfigTable } from "@/features/dashboard/components/kpi/config/config-table";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/atoms/popover";

import { Calendar } from "@/components/atoms/calendar";
import { useState } from "react";
import { format } from "date-fns";

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
    const [startDate, setStartDate] = useState<Date>();
    const [endDate, setEndDate] = useState<Date>();

    const mockConnections = [
        { label: "All connections", value: "all" },
        { label: "Connection-1", value: "connection-1" },
        { label: "Connection-2", value: "connection-2" },
        { label: "Connection-3", value: "connection-3" },
    ];

    const costAggregateQuickSelectOptions = [
        { label: "7 days", value: "7" },
        { label: "14 days", value: "14" },
        { label: "30 days", value: "30" },
        { label: "60 days", value: "60" },
        { label: "90 days", value: "90" },
        { label: "6 months", value: "180" },
        { label: "12 months", value: "365" },
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
                        <FieldGroup>
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
                        </FieldGroup>
                    </FieldSet>
                    <KPIConfigTable columns={kpiConfigColumns} data={mockKpiConfigRows} />
                    <FieldSeparator />

                    <FieldSet>
                        <div className="flex flex-row items-center gap-3">
                            <FormCountCircle count={3} />
                            <FieldLegend className="mb-0">Time Period</FieldLegend>
                        </div>

                        <FieldDescription>
                            Choose the time period over which the costs will be aggregated
                        </FieldDescription>
                        <FieldGroup>
                            <div className="grid grid-cols-[1fr_2fr] gap-6">
                                <div>
                                    <FieldLabel>Quick Select</FieldLabel>
                                    <Select defaultValue="30">
                                        <SelectTrigger>
                                            <SelectValue />
                                        </SelectTrigger>
                                        <SelectContent>
                                            <SelectGroup>
                                                {costAggregateQuickSelectOptions.map((option) => (
                                                    <SelectItem
                                                        key={option.value}
                                                        value={option.value}
                                                    >
                                                        {option.label}
                                                    </SelectItem>
                                                ))}
                                            </SelectGroup>
                                        </SelectContent>
                                    </Select>
                                </div>
                                <div>
                                    <FieldLabel>Custom Range</FieldLabel>
                                    <div className="flex flex-row items-center gap-4">
                                        <Popover>
                                            <PopoverTrigger asChild>
                                                <Button
                                                    variant="outline"
                                                    data-empty={!startDate}
                                                    className="w-[212px] justify-between text-left font-normal data-[empty=true]:text-muted-foreground"
                                                >
                                                    {startDate ? (
                                                        format(startDate, "PPP")
                                                    ) : (
                                                        <span>Pick a start date</span>
                                                    )}
                                                    <ChevronDownIcon data-icon="inline-end" />
                                                </Button>
                                            </PopoverTrigger>

                                            <PopoverContent className="w-auto p-0" align="start">
                                                <Calendar
                                                    mode="single"
                                                    selected={startDate}
                                                    onSelect={setStartDate}
                                                    defaultMonth={startDate}
                                                />
                                            </PopoverContent>
                                        </Popover>
                                        -
                                        <Popover>
                                            <PopoverTrigger asChild>
                                                <Button
                                                    variant="outline"
                                                    data-empty={!startDate}
                                                    className="w-[212px] justify-between text-left font-normal data-[empty=true]:text-muted-foreground"
                                                >
                                                    {startDate ? (
                                                        format(startDate, "PPP")
                                                    ) : (
                                                        <span>Pick an end date</span>
                                                    )}
                                                    <ChevronDownIcon data-icon="inline-end" />
                                                </Button>
                                            </PopoverTrigger>

                                            <PopoverContent className="w-auto p-0" align="start">
                                                <Calendar
                                                    mode="single"
                                                    selected={startDate}
                                                    onSelect={setStartDate}
                                                    defaultMonth={startDate}
                                                />
                                            </PopoverContent>
                                        </Popover>
                                    </div>
                                </div>
                            </div>
                        </FieldGroup>
                    </FieldSet>
                </Card>

                <Card className="p-6">
                    <CardTitle>Preview</CardTitle>
                    <Card className="flex flex-col gap-4 p-6 bg-muted/40">
                        <h1 className="text-lg font-bold">Card title</h1>
                        <h1 className="text-xl">$100.00</h1>
                        <p>Accros 3 selected resources</p>
                        <div className="flex flex-row justify-between">
                            <p>Last 30 days</p>
                            <p>Updated now</p>
                        </div>
                    </Card>

                    <Card className="flex flex-col p-6 bg-muted/40">
                        <h1 className="text-lg font-bold">Configuration Summary</h1>
                        <div className="grid grid-cols-2">
                            <div className="flex flex-col gap-6">
                                <p className="text-muted-foreground">Connections</p>
                                <p className="text-muted-foreground">Resources</p>
                                <p className="text-muted-foreground">Time Period</p>
                                <p className="text-muted-foreground">Aggregation</p>
                            </div>
                            <div className="flex flex-col gap-6">
                                <p className="font-semibold">All connections</p>
                                <p className="font-semibold">3 selected</p>
                                <p className="font-semibold">30 days</p>
                                <p className="font-semibold">Total cost (sum)</p>
                            </div>
                        </div>
                    </Card>
                </Card>
            </div>
        </main>
    );
}
