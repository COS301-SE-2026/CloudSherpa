"use client";

import { Button } from "@/components/atoms/button";
import { Card, CardContent, CardTitle, CardHeader } from "@/components/atoms/card";
import { FieldSeparator } from "@/components/atoms/field";
import { useEffect, useState, useRef } from "react";
import { KpiFormDetails } from "./kpi-form-details";
import { KpiFormTimePeriod } from "./kpi-form-time-period";
import { KPIWidget } from "../../kpi-widget";
import { KpiWidgetConfig } from "@/features/dashboard/types/widgets";
import { KpiConfigSummary } from "../kpi-config-summary";
import { kpiConfigColumns } from "@/features/dashboard/components/kpi/config/columns";
import { KPIConfigTable } from "@/features/dashboard/components/kpi/config/config-table";
import { useFetchTableResources } from "../hooks/useFetchTableResources";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import { useRouter } from "next/navigation";
import { useLoadDashboardData } from "@/features/dashboard/hooks/useLoadDash";
import { Spinner } from "@/components/atoms/spinner";

export type KpiConfigFormProps = {
    readonly kpiId: string;
};

export function KpiConfigFormInner({ kpiId }: KpiConfigFormProps) {
    const { fetchTableResources, tableResourcesFetchError, tableResourcesLoading, tableResources } =
        useFetchTableResources();
    const getWidget = useDashboardStore((state) => state.actions.getWidget);
    const updateWidget = useDashboardStore((state) => state.actions.updateKpiWidgetConfig);
    const [isSaving, setIsSaving] = useState(false);
    const widgetConfig = getWidget(kpiId);

    useEffect(() => {
        async function fetchResources() {
            await fetchTableResources();
        }

        fetchResources();
    }, []);

    const getWidgetError = widgetConfig === undefined;

    const [config, setConfig] = useState<KpiWidgetConfig>(() =>
        widgetConfig?.widgetType == "KPI"
            ? widgetConfig
            : {
                  id: kpiId,
                  displayName: "Default",
                  aggregationWindowDays: 30,
                  widgetType: "KPI",
                  chargeIds: [],
              }
    );
    const router = useRouter();

    function onTitleChange(newTitle: string): void {
        setConfig((prev) => ({
            ...prev,
            displayName: newTitle,
        }));
    }

    function onAggregationWindowChange(newWindow: number): void {
        setConfig((prev) => ({
            ...prev,
            aggregationWindowDays: newWindow,
        }));
    }

    function onSelectedChargeIdsChange(chargeIds: string[]) {
        setConfig((prev) => ({
            ...prev,
            chargeIds: chargeIds,
        }));
    }

    function saveKpiConfig() {
        setIsSaving(true);
        updateWidget(config);
        setIsSaving(false);
        router.push("/dashboard");
    }

    function cancelKpiConfig() {
        setIsSaving(false);
        router.push("/dashboard");
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
                <Button variant={"secondary"} disabled={isSaving} onClick={cancelKpiConfig}>
                    Cancel
                </Button>
            </div>
            <div className="grid grid-cols-[2fr_1fr] gap-4 h-full">
                <Card className="p-6">
                    <KpiFormDetails
                        title={config.displayName ?? "No Title"}
                        onTitleChange={onTitleChange}
                    />
                    <FieldSeparator></FieldSeparator>
                    {config.id != "123" && (
                        <KPIConfigTable
                            columns={kpiConfigColumns}
                            data={tableResources ?? []}
                            onSetChargeIdsChange={onSelectedChargeIdsChange}
                            selectedChargeIds={config.chargeIds}
                            error={tableResourcesFetchError}
                            loading={tableResourcesLoading}
                        />
                    )}
                    <FieldSeparator />
                    <KpiFormTimePeriod
                        aggregationWindowDays={config.aggregationWindowDays}
                        onAggregationWindowChange={onAggregationWindowChange}
                    />
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Preview</CardTitle>
                    </CardHeader>
                    <CardContent className="flex flex-col">
                        {getWidgetError ? (
                            <span className="text-base flex wrap">
                                Something went wrong, please refresh the page
                            </span>
                        ) : (
                            <KPIWidget config={config} preview />
                        )}

                        <KpiConfigSummary
                            numResources={config.chargeIds.length ?? 0}
                            aggregationWindowDays={config.aggregationWindowDays}
                        />
                    </CardContent>
                </Card>
            </div>
        </main>
    );
}

export function KpiConfigForm({ kpiId }: KpiConfigFormProps) {
    const { isLoading } = useLoadDashboardData();

    if (isLoading) {
        return (
            <div className="flex-1 flex h-[50vh] items-center justify-center">
                <Spinner className="size-8" />
            </div>
        );
    }

    return <KpiConfigFormInner kpiId={kpiId} />;
}
