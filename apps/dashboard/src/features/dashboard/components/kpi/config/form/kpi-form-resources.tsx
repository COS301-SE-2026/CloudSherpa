import {
    FieldDescription,
    FieldGroup,
    FieldLabel,
    FieldLegend,
    FieldSet,
} from "@/components/atoms/field";
import { FormCountCircle } from "@/components/atoms/form-count-circle";
import { InputGroup, InputGroupAddon, InputGroupInput } from "@/components/atoms/input-group";
import {
    Select,
    SelectContent,
    SelectGroup,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/atoms/select";
import {
    KPIConfigTableRow,
    kpiConfigColumns,
} from "@/features/dashboard/components/kpi/config/columns";
import { KPIConfigTable } from "@/features/dashboard/components/kpi/config/config-table";
import { SearchIcon } from "lucide-react";

const mockConnections = [
    { label: "All connections", value: "all" },
    { label: "Connection-1", value: "connection-1" },
    { label: "Connection-2", value: "connection-2" },
    { label: "Connection-3", value: "connection-3" },
];

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

export function KpiFormResources() {
    return (
        <>
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
        </>
    );
}
