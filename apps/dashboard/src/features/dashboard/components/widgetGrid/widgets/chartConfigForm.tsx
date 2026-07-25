"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import { ChartWidgetConfig } from "@/features/dashboard/types/widgets";

import { Button } from "@/components/atoms/button";
import { Card, CardContent, CardTitle } from "@/components/atoms/card";
import { FieldSeparator } from "@/components/atoms/field";

import { ChartWidget } from "@/features/dashboard/components/widgetGrid/widgets/chartWidget";
import { ChartFormDetails } from "@/features/dashboard/components/widgetGrid/widgets/chartFormDetails";
import { ChartFormResource } from "@/features/dashboard/components/widgetGrid/widgets/chartFormResource";

export type ChartConfigFormProps = {
    readonly chartId: string;
};

export function ChartConfigForm({ chartId }: ChartConfigFormProps) {
    const router = useRouter();
    const getWidget = useDashboardStore((state) => state.actions.getWidget);
    const updateWidget = useDashboardStore((state) => state.actions.updateChartWidgetConfig);

    const [isSaving, setIsSaving] = useState(false);

    const widgetConfig = getWidget(chartId) as ChartWidgetConfig | undefined;
    const getWidgetError = widgetConfig === undefined;

    const [config, setConfig] = useState<ChartWidgetConfig>(
        () =>
            widgetConfig ?? {
                id: chartId,
                displayName: "New Chart",
                widgetType: "CHART",
                chartType: "line_chart",
                resourceId: null,
                metricType: null,
            }
    );

    function handleSave() {
        setIsSaving(true);
        updateWidget(config);
        setIsSaving(false);
        router.push("/dashboard");
    }

    return (
        <main className="flex flex-1 flex-col gap-6 p-6 lg:p-8 w-full mx-auto">
            <div className="flex flex-row gap-6">
                <h1 className="text-2xl">Chart Configuration</h1>
                <Button
                    variant="default"
                    disabled={getWidgetError || isSaving}
                    onClick={handleSave}
                >
                    {isSaving ? "Saving..." : "Save Chart"}
                </Button>
                <Button variant="secondary" onClick={() => router.back()}>
                    Cancel
                </Button>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-[2fr_1fr] gap-4 h-full">
                <Card className="p-6 flex flex-col gap-6">
                    <ChartFormDetails config={config} setConfig={setConfig} />
                    <FieldSeparator />
                    <ChartFormResource config={config} setConfig={setConfig} />
                </Card>
                <Card className="p-6">
                    <CardTitle className="mb-4">Preview</CardTitle>
                    {getWidgetError ? (
                        <CardContent>Something went wrong, please refresh the page</CardContent>
                    ) : (
                        <div className="h-100">
                            <ChartWidget config={config} preview />
                        </div>
                    )}
                </Card>
            </div>
        </main>
    );
}
