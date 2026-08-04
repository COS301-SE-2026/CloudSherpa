"use client";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/atoms/card";
import ChartFormResource from "./chart-form-resource";
import { useState } from "react";
import ChartFormDetails from "./chart-form-details";
import { Button } from "@/components/atoms/button";
import { ChartWidgetConfig } from "@/features/dashboard/types/widgets";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import { ChartWidget } from "./chartWidget";
import { useRouter } from "next/navigation";
import ChartFormConnection from "./chart-form-connection";

interface ChartConfigFormProps {
    ChartId: string;
}

export default function ChartConfigForm({ ChartId }: Readonly<ChartConfigFormProps>) {
    const [isSaving, setIsSaving] = useState(false);
    const updateWidget = useDashboardStore((state) => state.actions.updateChartWidgetConfig);
    const getWidget = useDashboardStore((state) => state.actions.getWidget);
    const widgetConfig = getWidget(ChartId);
    const resolvedWidgetConfig: ChartWidgetConfig =
        widgetConfig?.widgetType === "CHART"
            ? widgetConfig
            : {
                  id: "123",
                  displayName: "Default",
                  widgetType: "CHART",
                  chartType: "line_chart",
                  resourceId: null,
                  metricType: null,
              };
    const [config, setConfig] = useState<ChartWidgetConfig>(resolvedWidgetConfig);
    const router = useRouter();

    const handleSave = () => {
        setIsSaving(true);
        updateWidget(config);
        setIsSaving(false);
        router.push("/dashboard");
    };

    const cancelChartConfig = () => {
        setIsSaving(false);
        router.push("/dashboard");
    };

    return (
        <main className="flex flex-col gap-6 p-6 lg:p-8 w-full h-full">
            <div className="flex flex-row gap-6">
                <h1 className="text-2xl">Chart Configuration</h1>
                <Button variant={"default"} onClick={() => handleSave()} disabled={isSaving}>
                    Save Chart
                </Button>
                <Button
                    variant={"secondary"}
                    onClick={() => cancelChartConfig()}
                    disabled={isSaving}
                >
                    Cancel
                </Button>
            </div>
            <div className="flex flex-row gap-6 h-full">
                <Card className="w-2/3">
                    <CardContent className="flex flex-col gap-6">
                        <ChartFormDetails configuration={config} setConfiguration={setConfig} />
                        <ChartFormConnection configuration={config} setConfiguration={setConfig} />
                        <ChartFormResource configuration={config} setConfiguration={setConfig} />
                    </CardContent>
                </Card>
                <Card className="w-1/3">
                    <CardHeader>
                        <CardTitle>Widget Preview</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <ChartWidget config={config} preview={true} />
                    </CardContent>
                </Card>
            </div>
        </main>
    );
}
