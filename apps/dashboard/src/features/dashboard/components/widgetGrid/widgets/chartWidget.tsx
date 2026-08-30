import React, { useState, useEffect, useRef } from "react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/atoms/card";
import { LineChart } from "@/features/dashboard/components/widgetGrid/widgets/charts/LineChart";
import { GaugeChart } from "@/features/dashboard/components/widgetGrid/widgets/charts/GaugeChart";
import { Button } from "@/components/atoms/button";
import { ChartType, ChartWidgetConfig } from "@/features/dashboard/types/widgets";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import { WidgetMenu } from "@/features/dashboard/components/widgetMenu";
import { WidgetDropdown } from "@/features/dashboard/components/widgetDropdown";
import { CircleAlert, Sparkles } from "lucide-react";
import { useRouter } from "next/navigation";
import {
    Tooltip,
    TooltipContent,
    TooltipProvider,
    TooltipTrigger,
} from "@/components/atoms/tooltip";
import { useRecStore } from "@/features/optimization/stores/useRecStore";
import { useFetchMetricHistoricalData } from "@/features/dashboard/hooks/useFetchMetricHistoricalData";

interface BaseChartProps {
    resourceId: string;
    metricType: string;
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
    const { chartType, displayName, resourceId, metricName, id } = config;
    const ChartComponent = CHART_COMPONENTS[chartType];
    const [hasNoData, setHasNoData] = useState(false);
    const router = useRouter();

    // watch widget content while expanding
    const contentRef = useRef<HTMLDivElement>(null);
    const [isLayoutReady, setIsLayoutReady] = useState(false);

    const fromMs = useDashboardStore((state) => state.fromMs);
    const toMs = useDashboardStore((state) => state.toMs);

    useFetchMetricHistoricalData({
        resourceId: resourceId ?? "",
        fromMs: fromMs,
        toMs: toMs,
        metricName: metricName ?? undefined,
    });

    useEffect(() => {
        if (!contentRef.current) return; //check content present

        let resizeTimer: ReturnType<typeof setTimeout>;

        // built in observer for referenced
        const observer = new ResizeObserver(() => {
            clearTimeout(resizeTimer);

            resizeTimer = setTimeout(() => {
                setIsLayoutReady(true);
            }, 100);
        });
        //observers card content for pizel changes
        observer.observe(contentRef.current);

        return () => {
            observer.disconnect();
            clearTimeout(resizeTimer);
        };
    }, []);

    const openConfig = () => {
        if (!isEditMode) {
            router.push(`/edit/metrics/${config.id}`);
        }
    };

    const removeWidget = useDashboardStore((state) => state.actions.removeWidget);

    const hasRecommendation = useRecStore((state) =>
        state.recommendationGroups.some((group) =>
            group.recommendations.some(
                (rec) => rec.resourceId === resourceId && rec.status === "ACTIVE"
            )
        )
    );

    const renderChartContent = () => {
        if (!ChartComponent) {
            return (
                <div className="flex items-center justify-center h-full text-muted-foreground italic text-xs">
                    Unknown Chart Type: {chartType}
                </div>
            );
        }

        if (!resourceId || !metricName) {
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
                metricType={metricName}
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
                    <div className="flex flex-row justify-end items-center gap-2">
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
                        {hasRecommendation && !hasNoData && (
                            <TooltipProvider delayDuration={100}>
                                <Tooltip>
                                    <TooltipTrigger asChild>
                                        <div className="flex items-center">
                                            <Sparkles className="h-5 w-5 text-primary cursor-help" />
                                        </div>
                                    </TooltipTrigger>
                                    <TooltipContent
                                        side="bottom"
                                        align="end"
                                        className="w-48 text-center text-xs"
                                    >
                                        <p>
                                            The resource related to this widget has active
                                            optimization recommendations.
                                        </p>
                                    </TooltipContent>
                                </Tooltip>
                            </TooltipProvider>
                        )}

                        {!preview && !isEditMode && (
                            <WidgetDropdown
                                onConfigure={openConfig}
                                onDelete={() => removeWidget(id, id)}
                                isEditMode={isEditMode}
                            />
                        )}
                    </div>
                </CardHeader>

                <CardContent ref={contentRef} className="flex-1 w-full relative overflow-hidden">
                    {isLayoutReady ? (
                        renderChartContent()
                    ) : (
                        <div className="w-full h-full bg-muted/20 animate-pulse rounded" />
                    )}
                </CardContent>
            </Card>
        </WidgetMenu>
    );
}
