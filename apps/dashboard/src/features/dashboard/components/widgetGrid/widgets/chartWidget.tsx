import React, { useState } from "react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/atoms/card";
import { LineChart } from "@/features/dashboard/components/widgetGrid/widgets/charts/LineChart";
import { GaugeChart } from "@/features/dashboard/components/widgetGrid/widgets/charts/GaugeChart";
import { MetricType } from "@/features/dashboard/types/metric";
import { Button } from "@/components/atoms/button";
import { ChartType, ChartWidgetConfig } from "@/features/dashboard/types/widgets";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import { WidgetMenu } from "@/features/dashboard/components/widgetMenu";
import { WidgetDropdown } from "@/features/dashboard/components/widgetDropdown";
import { CircleAlert } from "lucide-react";
import { useRouter } from "next/navigation";
import {
    Tooltip,
    TooltipContent,
    TooltipProvider,
    TooltipTrigger,
} from "@/components/atoms/tooltip";

interface BaseChartProps {
    resourceId: string;
    metricType: MetricType;
    onDataStatusChange?: (hasData: boolean) => void;
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
    const [hasNoData, setHasNoData] = useState(false);
    const router = useRouter();

    const openConfig = () => {
        if (!isEditMode) {
            router.push(`/edit/metrics/${config.id}`);
        }
    };

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
                <div className="flex flex-col  h-full items-center justify-center gap-2">
                    {isEditMode ? (
                        <p className="text-xs text-muted-foreground italic">
                            Save dashboard changes before configuring this widget.
                        </p>
                    ) : (
                        <div className="flex flex-col items-center justify-center gap-2">
                            <span className="text-base">This widget is not configured.</span>
                            <Button onClick={openConfig} aria-label="configure new widget button">
                                Configure Widget
                            </Button>
                        </div>
                    )}
                </div>
            );
        }

        return (
            <ChartComponent
                resourceId={resourceId}
                metricType={metricType}
                onDataStatusChange={(hasData) => setHasNoData(!hasData)}
            />
        );
    };

    return (
        <WidgetMenu
            onConfigure={openConfig}
            onDelete={() => removeWidget(id, id)}
            isEditMode={isEditMode}
            preview={false}
        >
            <Card
                className={`flex flex-col w-full overflow-hidden ${preview ? "h-90" : "h-full"}`}
                aria-label="chart widget"
            >
                <CardHeader className="flex flex-row items-center justify-between ">
                    <CardTitle>{displayName}</CardTitle>
                    <div className="flex items-center gap-2">
                        {hasNoData && (
                            <TooltipProvider delayDuration={100}>
                                <Tooltip>
                                    <TooltipTrigger asChild>
                                        <div className="flex items-center">
                                            <CircleAlert className="h-5 w-5 text-warning animate-pulse cursor-help" />
                                        </div>
                                    </TooltipTrigger>
                                    <TooltipContent
                                        side="bottom"
                                        align="end"
                                        className="w-48 text-center text-xs"
                                    >
                                        <p>There is no data to display for this time window.</p>
                                    </TooltipContent>
                                </Tooltip>
                            </TooltipProvider>
                        )}
                        {preview && (
                            <WidgetDropdown
                                onConfigure={openConfig}
                                onDelete={() => removeWidget(id, id)}
                                isEditMode={isEditMode}
                            />
                        )}
                    </div>
                </CardHeader>

                <CardContent className="flex-1 w-full relative overflow-hidden">
                    {renderChartContent()}
                </CardContent>
            </Card>
        </WidgetMenu>
    );
}
