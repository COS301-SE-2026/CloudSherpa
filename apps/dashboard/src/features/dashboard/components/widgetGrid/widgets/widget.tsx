import React from "react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/atoms/card";
import { LineChart } from "@/features/dashboard/components/widgetGrid/widgets/charts/LineChart";
import { GaugeChart } from "@/features/dashboard/components/widgetGrid/widgets/charts/GaugeChart";
import { MetricType } from "@/features/dashboard/types/metric";
import { Button } from "@/components/atoms/button";
import { WidgetConfigMenu } from "@/features/dashboard/components/widgetGrid/widgets/widgetConfigMenu";
import { WidgetConfig, ChartType } from "@/features/dashboard/types/widgets";
import { EllipsisVertical } from "lucide-react";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import { useState } from "react";

interface BaseChartProps {
  resourceId: string;
  metricType: MetricType;
}

const CHART_COMPONENTS: Record<ChartType, React.ComponentType<BaseChartProps>> = {
  line: LineChart,
  gauge: GaugeChart,
};

interface WidgetProps {
  config: WidgetConfig;
}

export default function Widget({ config }: Readonly<WidgetProps>) {
  const { chartType, title, resourceId, metricType } = config;
  const ChartComponent = CHART_COMPONENTS[chartType];
  const [isConfigOpen, setIsConfigOpen] = useState(false);

  console.log(metricType, resourceId);

  const updateStore = useDashboardStore((state) => state.actions.updateWidgetConfig); //updates store when configmeny saves

  return (
    <>
      <Card className="flex  h-full w-full overflow-hidden border border-border rounded-md shadow-sm bg-card">
        <CardHeader className="flex flex-row items-center justify-between border-b border-border  px-4 py-2 space-y-0 h-12">
          <CardTitle className="text-sm font-semibold text-foreground">{title}</CardTitle>
          <Button onClick={() => setIsConfigOpen(true)} className="bg-transparent">
            <EllipsisVertical />
          </Button>
        </CardHeader>

        <CardContent className="flex-1 w-full relative p-0 overflow-hidden min-h-10">
          {ChartComponent ? (
            <ChartComponent resourceId={resourceId} metricType={metricType} />
          ) : (
            <div className="flex items-center justify-center h-full text-muted-foreground italic text-xs">
              Unknown Chart Type: {chartType}
            </div>
          )}
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
