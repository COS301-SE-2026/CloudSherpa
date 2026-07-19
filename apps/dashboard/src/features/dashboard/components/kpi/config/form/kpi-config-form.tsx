"use client";

import { Button } from "@/components/atoms/button";
import { Card, CardTitle } from "@/components/atoms/card";
import { FieldSeparator } from "@/components/atoms/field";
import { useState } from "react";
import { KpiFormDetails } from "./kpi-form-details";
import { KpiFormTimePeriod } from "./kpi-form-time-period";
import { KPIWidget } from "../../kpi-widget";
import { KpiWidgetConfig } from "@/features/dashboard/types/widgets";
import { KpiConfigSummary } from "../kpi-config-summary";
import {
    KPIConfigTableRow,
    kpiConfigColumns,
} from "@/features/dashboard/components/kpi/config/columns";
import { KPIConfigTable } from "@/features/dashboard/components/kpi/config/config-table";

const mockConnections = ["All connections", "Production AWS", "Data AWS", "Finance GCP"];

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

export type KpiConfigFormProps = {
    readonly kpiId: string;
};

export function KpiConfigForm({ kpiId }: KpiConfigFormProps) {
    const [startDate, setStartDate] = useState<Date>();
    const [endDate, setEndDate] = useState<Date>();
    const [title, setTitle] = useState("Tmp title");

    const [config, setConfig] = useState<KpiWidgetConfig>({
        id: "123",
        aggregationWindowDays: 30,
        resourceIds: ["1", "2", "3"],
        title: title,
        widgetType: "kpi",
    });

    function onTitleChange(newTitle: string): void {
        setTitle(newTitle);
        setConfig((prev) => ({
            ...prev,
            title: newTitle,
        }));
    }

    return (
        <main className="flex flex-1 flex-col gap-6 p-6 lg:p-8 w-full mx-auto">
            <div className="flex flex-row gap-6">
                <h1 className="text-2xl">KPI Configuration</h1>
                <Button variant={"default"}>Save KPI</Button>
                <Button variant={"secondary"}>Cancel</Button>
            </div>
            <div className="grid grid-cols-[2fr_1fr] gap-4 h-full">
                <Card className="p-6">
                    <KpiFormDetails title={title} onTitleChange={onTitleChange} />
                    <FieldSeparator></FieldSeparator>
                    <KPIConfigTable
                        columns={kpiConfigColumns}
                        data={mockKpiConfigRows}
                        connections={mockConnections}
                    />
                    <FieldSeparator />
                    <KpiFormTimePeriod
                        startDate={startDate}
                        setStartDate={setStartDate}
                        endDate={endDate}
                        setEndDate={setEndDate}
                    />
                </Card>

                <Card className="p-6">
                    <CardTitle>Preview</CardTitle>
                    <KPIWidget config={config} />

                    <KpiConfigSummary />
                </Card>
            </div>
        </main>
    );
}
