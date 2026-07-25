import React, { useState } from "react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/atoms/card";
import { LineChart } from "@/features/dashboard/components/widgetGrid/widgets/charts/LineChart";
import { GaugeChart } from "@/features/dashboard/components/widgetGrid/widgets/charts/GaugeChart";
import { MetricType } from "@/features/dashboard/types/metric";
import { Button } from "@/components/atoms/button";
import { ChartType, ChartWidgetConfig } from "@/features/dashboard/types/widgets";
import { EllipsisVertical, Pencil, Trash } from "lucide-react";
import router from "next/router";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import {
    ContextMenu,
    ContextMenuContent,
    ContextMenuItem,
    ContextMenuSeparator,
    ContextMenuTrigger,
} from "@/components/atoms/context-menu";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/atoms/dropdown-menu";

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
    readonly preview?: boolean;
}

export function ChartWidget({ config, preview = false }: Readonly<WidgetProps>) {
    const { chartType, displayName, resourceId, metricType, id } = config;
    const ChartComponent = CHART_COMPONENTS[chartType];

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
                <div className="flex flex-col w-full h-full items-center justify-center">
                    <Button onClick={handleConfigure}>Configure Widget</Button>
                </div>
            );
        }

        return <ChartComponent resourceId={resourceId} metricType={metricType} />;
    };

    const handleConfigure = () => {
        router.push(`/dashboard/chart/${id}`);
    };

    return (
        <ContextMenu>
            <ContextMenuTrigger>
                <Card className="flex flex-col h-full w-full overflow-hidden">
                    <CardHeader className="flex flex-row items-center justify-between ">
                        <CardTitle>{displayName}</CardTitle>
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button variant="ghost" size="icon" className="h-8 w-8">
                                    <EllipsisVertical className="h-4 w-4" />
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end" className="w-fit">
                                <DropdownMenuItem onClick={() => handleConfigure()}>
                                    <Pencil className="mr-2 h-4 w-4" />
                                    Configure Widget
                                </DropdownMenuItem>
                                <DropdownMenuSeparator />
                                <DropdownMenuItem
                                    onClick={() => removeWidget(id, id)}
                                    className="text-destructive focus:text-destructive"
                                >
                                    <Trash className="mr-2 h-4 w-4" />
                                    Delete Widget
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </CardHeader>

                    <CardContent className="flex-1 w-full relative overflow-hidden">
                        {renderChartContent()}
                    </CardContent>
                </Card>
            </ContextMenuTrigger>
            {!preview && (
                <ContextMenuContent className="w-48">
                    <ContextMenuItem onClick={() => handleConfigure()}>
                        <Pencil className="mr-2 h-4 w-4" />
                        Configure Widget
                    </ContextMenuItem>
                    <ContextMenuSeparator />
                    <ContextMenuItem
                        onClick={() => removeWidget(id, id)}
                        className="text-destructive focus:text-destructive"
                    >
                        <Trash className="mr-2 h-4 w-4" />
                        Delete Widget
                    </ContextMenuItem>
                </ContextMenuContent>
            )}
        </ContextMenu>
    );
}
