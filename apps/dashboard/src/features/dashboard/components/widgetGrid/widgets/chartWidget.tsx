"use client";

import { Card, CardHeader, CardTitle, CardContent } from "@/components/atoms/card";
import { LineChart } from "@/features/dashboard/components/widgetGrid/widgets/charts/LineChart";
import { GaugeChart } from "@/features/dashboard/components/widgetGrid/widgets/charts/GaugeChart";
import { MetricType } from "@/features/dashboard/types/metric";
import { Button } from "@/components/atoms/button";
import { ChartType, ChartWidgetConfig } from "@/features/dashboard/types/widgets";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import { useToolbar } from "@/features/dashboard/components/toolbar/toolbarProvider";
import { WidgetMenu } from "@/features/dashboard/components/widgetMenu";
import { WidgetDropdown } from "@/features/dashboard/components/widgetDropdown";
import { useRouter } from "next/navigation";
import { useState } from "react";

interface BaseChartProps {
    resourceId: string;
    metricType: MetricType;
}

const CHART_COMPONENTS: Record<ChartType, React.ComponentType<BaseChartProps>> = {
    line_chart: LineChart,
    gauge_chart: GaugeChart,
};

interface WidgetProps {
    config: ChartWidgetConfig;
    preview?: boolean;
    isEditMode?: boolean;
}

export function ChartWidget({
    config,
    preview = false,
    isEditMode = false,
}: Readonly<WidgetProps>) {
    const { chartType, displayName, resourceId, metricType, id } = config;
    const ChartComponent = CHART_COMPONENTS[chartType];
    const router = useRouter();

    const removeWidget = useDashboardStore((state) => state.actions.removeWidget);

    const renderChartContent = () => {
        if (!ChartComponent) {
            return (
                <div className="flex items-center justify-center h-full text-muted-foreground italic text-xs">
                    Unknown Chart Type: {chartType}
                </div>
            );
        }

        if (!resourceId || !metricType) {
            return (
                <div className="flex flex-col w-full h-full items-center justify-center gap-2">
                    {isEditMode ? (
                        <p className="text-xs text-muted-foreground italic">
                            Save dashboard changes before configuring this widget.
                        </p>
                    ) : (
                        <Button onClick={handleConfigure}>Configure Widget</Button>
                    )}
                </div>
            );
        }

        return <ChartComponent resourceId={resourceId} metricType={metricType} />;
    };

    const handleConfigure = () => {
        router.push(`/edit/chart/${id}`);
    };

    return (
        <WidgetMenu
            onConfigure={handleConfigure}
            onDelete={() => removeWidget(id, id)}
            isEditMode={isEditMode}
            preview={preview}
        >
            <Card className="flex flex-col h-full w-full overflow-hidden">
                <CardHeader className="flex flex-row items-center justify-between ">
                    <CardTitle>{displayName}</CardTitle>
                    <WidgetDropdown
                        onConfigure={handleConfigure}
                        onDelete={() => removeWidget(id, id)}
                        isEditMode={isEditMode}
                    />
                </CardHeader>

                <CardContent className="flex-1 w-full relative overflow-hidden">
                    {renderChartContent()}
                </CardContent>
            </Card>
        </WidgetMenu>
    );
}
