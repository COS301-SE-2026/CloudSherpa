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
import { Spinner } from "@/components/atoms/spinner";
import { useLoadDashboardData } from "@/features/dashboard/hooks/useLoadDash";
import { is } from "date-fns/locale";

interface ChartConfigFormProps {
    ChartId: string;
}

export function ChartConfigFormInner({ ChartId }: Readonly<ChartConfigFormProps>) {
    const [isSaving, setIsSaving] = useState(false);
    const [isError, setIsError] = useState("");
    const updateWidget = useDashboardStore((state) => state.actions.updateChartWidgetConfig);
    const getWidget = useDashboardStore((state) => state.actions.getWidget);

    const widgetConfig = getWidget(ChartId);

    const resolvedWidgetConfig: ChartWidgetConfig =
        widgetConfig?.widgetType === "CHART"
            ? widgetConfig
            : {
                  id: ChartId,
                  displayName: "Default",
                  widgetType: "CHART",
                  chartType: "line_chart",
                  chartColour: "chart_1",
                  provider: null,
                  accountId: null,
                  resourceId: null,
                  metricName: "",
                  metricType: null,
              };

    const [config, setConfig] = useState<ChartWidgetConfig>(resolvedWidgetConfig);

    const router = useRouter();

    const handleSave = async () => {
        if (!config.displayName || config.displayName.trim() === "") {
            setIsError("Enter a dispaly name to save changes");
            return;
        }
        setIsSaving(true);
        try {
            await updateWidget(config);
        } catch (error) {
            console.error("Failed to save configuration", error);
        } finally {
            setIsSaving(false);
        }
        router.push("/dashboard");
    };

    const cancelChartConfig = () => {
        setIsSaving(false);
        router.push("/dashboard");
    };

    return (
        <main className="flex flex-col gap-6 p-6 lg:p-8 w-full h-full">
            <div className="flex flex-row gap-2">
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
                        {isError.length > 0 && (
                            <div className="w-full p-3 bg-destructive/10 border border-destructive/80 rounded-md text-destructive text-xs">
                                {isError}
                            </div>
                        )}
                        <ChartFormDetails configuration={config} setConfiguration={setConfig} />
                        <ChartFormConnection configuration={config} setConfiguration={setConfig} />
                        <ChartFormResource
                            key={config.accountId || "empty-connection"}
                            configuration={config}
                            setConfiguration={setConfig}
                        />
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

export default function ChartConfigForm({ ChartId }: Readonly<ChartConfigFormProps>) {
    const { isLoading } = useLoadDashboardData();

    if (isLoading) {
        return (
            <div className="flex-1 flex h-[50vh] items-center justify-center">
                <Spinner className="size-8" />
            </div>
        );
    }

    return <ChartConfigFormInner ChartId={ChartId} />;
}
