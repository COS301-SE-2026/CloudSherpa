"use client";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/atoms/card";
import ChartFormResource from "./chart-form-resource";
import { useState } from "react";
import ChartFormDetails from "./chart-form-details";
import { Button } from "@/components/atoms/button";
import { ChartWidgetConfig } from "@/features/dashboard/types/widgets";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import { ChartWidget } from "./chartWidget";

interface ChartConfigFormProps {
    readonly ChartId: string;
}

export default function ChartConfigForm({ ChartId }: ChartConfigFormProps) {
    const [isConfigOpen, setIsConfigOpen] = useState(false);
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

    const temp = () => {
        console.log("temp");
    };
    return (
        <main className="flex flex-col gap-6 p-6 lg:p-8 w-full h-full">
            <div className="flex flex-row gap-6">
                <h1 className="text-2xl">Chart Configuration</h1>
                <Button variant={"default"}>Save KPI</Button>
                <Button variant={"secondary"}>Cancel</Button>
            </div>
            <div className="flex flex-row gap-6 h-full">
                <Card className="w-2/3">
                    <CardContent className="flex flex-col gap-6">
                        <ChartFormDetails
                            configuration={{} as ChartWidgetConfig}
                            setConfiguration={() => setIsConfigOpen(false)}
                        />
                        <ChartFormResource
                            isOpen={true}
                            onClose={() => setIsConfigOpen(false)}
                            existingConfig={config}
                        />
                    </CardContent>
                </Card>
                <Card className="w-1/3">
                    <CardHeader>
                        <CardTitle>Widget Preview</CardTitle>
                    </CardHeader>
                    <CardContent></CardContent>
                </Card>
            </div>
        </main>
    );
}
