"use client";

import { Button } from "@/components/atoms/button";
import { Card, CardContent, CardTitle } from "@/components/atoms/card";
import { FieldSeparator } from "@/components/atoms/field";
import { useEffect, useState } from "react";
import { KpiFormDetails } from "./kpi-form-details";
import { KpiFormTimePeriod } from "./kpi-form-time-period";
import { KPIWidget } from "../../kpi-widget";
import { KpiWidgetConfig } from "@/features/dashboard/types/widgets";
import { KpiConfigSummary } from "../kpi-config-summary";
import {
    kpiConfigColumns,
    KPIConfigTableRow,
} from "@/features/dashboard/components/kpi/config/columns";
import { KPIConfigTable } from "@/features/dashboard/components/kpi/config/config-table";
// import { mockKpiConfigRows } from "@/features/dashboard/components/kpi/config/mock-kpi-config-rows";
// import { CloudProviderEnum } from "@/features/dashboard/types/provider";
import { useFetchTableResources } from "../hooks/useFetchTableResources";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";

export type KpiConfigFormProps = {
    readonly kpiId: string;
};

export function KpiConfigForm({ kpiId }: KpiConfigFormProps) {
    const [selectedRows, setSelectedRows] = useState<KPIConfigTableRow[]>();
    const { fetchTableResources, tableResourcesFetchError, tableResourcesLoading, tableResources } =
        useFetchTableResources();
    const getWidget = useDashboardStore((state) => state.actions.getWidget);
    const updateWidget = useDashboardStore((state) => state.actions.updateWidgetConfig);
    const [isSaving, setIsSaving] = useState(false);
    const widgetConfig = getWidget(kpiId);
    const getWidgetError = widgetConfig === undefined;
    const [config, setConfig] = useState<KpiWidgetConfig>(() =>
        widgetConfig?.widgetType == "kpi"
            ? widgetConfig
            : {
                  id: "123",
                  title: "Default",
                  aggregationWindowDays: 30,
                  widgetType: "kpi",
                  resourceIds: [],
              }
    );

    useEffect(() => {
        async function fetchResources() {
            await fetchTableResources();
        }

        fetchResources();
    }, [fetchTableResources, kpiId]);

    function onTitleChange(newTitle: string): void {
        setConfig((prev) => ({
            ...prev,
            title: newTitle,
        }));
    }

    function onAggregationWindowChange(newWindow: number): void {
        setConfig((prev) => ({
            ...prev,
            aggregationWindowDays: newWindow,
        }));
    }

    function onSetSelectedRows(rows: KPIConfigTableRow[] | undefined) {
        setSelectedRows(rows);
        setConfig((prev) => ({
            ...prev,
            resourceIds: rows?.map((row) => row.resourceId) ?? [],
        }));
    }

    function saveKpiConfig() {
        setIsSaving(true);
        updateWidget(config);
        setIsSaving(false);
    }

    return (
        <main className="flex flex-1 flex-col gap-6 p-6 lg:p-8 w-full mx-auto">
            <div className="flex flex-row gap-6">
                <h1 className="text-2xl">KPI Configuration</h1>
                <Button
                    variant={"default"}
                    disabled={getWidgetError || isSaving}
                    onClick={saveKpiConfig}
                >
                    Save KPI
                </Button>
                <Button variant={"secondary"}>Cancel</Button>
            </div>
            <div className="grid grid-cols-[2fr_1fr] gap-4 h-full">
                <Card className="p-6">
                    <KpiFormDetails title={config.title} onTitleChange={onTitleChange} />
                    <FieldSeparator></FieldSeparator>
                    <KPIConfigTable
                        columns={kpiConfigColumns}
                        data={tableResources ?? []}
                        onSetSelectedRows={onSetSelectedRows}
                        error={tableResourcesFetchError}
                        loading={tableResourcesLoading}
                    />
                    <FieldSeparator />
                    <KpiFormTimePeriod
                        aggregationWindowDays={config.aggregationWindowDays}
                        onAggregationWindowChange={onAggregationWindowChange}
                    />
                </Card>

                <Card className="p-6">
                    <CardTitle>Preview</CardTitle>
                    {getWidgetError ? (
                        <CardContent>Something went wrong, please refresh the page</CardContent>
                    ) : (
                        <KPIWidget config={config} preview />
                    )}

                    <KpiConfigSummary
                        numResources={selectedRows?.length ?? 0}
                        aggregationWindowDays={config.aggregationWindowDays}
                    />
                </Card>
            </div>
        </main>
    );
}
