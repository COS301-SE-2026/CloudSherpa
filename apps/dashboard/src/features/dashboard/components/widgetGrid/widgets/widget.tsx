import React, { useState } from "react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/atoms/card";
import { LineChart } from "@/features/dashboard/components/widgetGrid/widgets/charts/LineChart";
import { GaugeChart } from "@/features/dashboard/components/widgetGrid/widgets/charts/GaugeChart";
import { MetricType } from "@/features/dashboard/types/metric";
import { Button } from "@/components/atoms/button";
import { WidgetConfigMenu } from "@/features/dashboard/components/widgetGrid/widgets/widgetConfigMenu";
import { WidgetConfig, ChartType } from "@/features/dashboard/types/widgets";
import { EllipsisVertical } from "lucide-react";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";

interface BaseChartProps {
    resourceId: string;
    metricType: MetricType;
}

const CHART_COMPONENTS: Record<ChartType, React.ComponentType<BaseChartProps>> = {
    line_chart: LineChart,
    gauge_chart: GaugeChart,
};

interface WidgetProps {
    config: WidgetConfig;
}

export default function Widget({ config }: Readonly<WidgetProps>) {
    const { type, displayName, resourceId, metricType } = config;
    const ChartComponent = CHART_COMPONENTS[type];
    const [isConfigOpen, setIsConfigOpen] = useState(false);

    console.log(metricType, resourceId);

    const updateStore = useDashboardStore((state) => state.actions.updateWidgetConfig);

    const renderChartContent = () => {
        if (!ChartComponent) {
            return (
                <div className="flex items-center justify-center h-full text-muted-foreground italic text-xs">
                    Unknown Chart Type: {type}
                </div>
            );
        }

        if (!resourceId || !metricType) {
            return (
                <Card className="flex flex-col items-center justify-center h-full">
                    <span>Unconfigured Widget</span>
                    <span className="text-xs mt-1">Click the menu to set up</span>
                </Card>
            );
        }

        return <ChartComponent resourceId={resourceId} metricType={metricType} />;
    };

    return (
        <>
            <Card className="flex flex-col h-full w-full overflow-hidden">
                <CardHeader className="flex flex-row items-center justify-between ">
                    <CardTitle>{displayName}</CardTitle>
                    <Button
                        onClick={() => setIsConfigOpen(true)}
                        className="text-muted-foreground bg-transparent hover:bg-muted/10"
                    >
                        <EllipsisVertical />
                    </Button>
                </CardHeader>

                <CardContent className="flex-1 w-full relative overflow-hidden">
                    {renderChartContent()}
                </CardContent>
            </Card>

            <WidgetConfigMenu
                isOpen={isConfigOpen}
                existingConfig={config}
                onClose={() => setIsConfigOpen(false)}
                onSave={(newConfig: WidgetConfig) => {
                    updateStore(newConfig);
                    setIsConfigOpen(false);
                }}
            />
        </>
    );
}
