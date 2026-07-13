import React, { useState } from "react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/atoms/card";
import {LineChart} from "@/features/dashboard/components/widgetGrid/widgets/charts/LineChart";
import {GaugeChart} from "@/features/dashboard/components/widgetGrid/widgets/charts/GaugeChart";
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

  const updateStore = useDashboardStore((state) => state.actions.updateWidgetConfig);

  return (
    <>
      <Card className="flex flex-col h-full w-full overflow-hidden">
        <CardHeader className="flex flex-row items-center justify-between ">
          <CardTitle >{title}</CardTitle>
          <Button onClick={() => setIsConfigOpen(true)} className="text-muted-foreground bg-transparent hover:bg-muted/10">
            <EllipsisVertical />
          </Button>
        </CardHeader>

        <CardContent className="flex-1 w-full relative overflow-hidden">
          {ChartComponent ? (
            <ChartComponent resourceId={resourceId} metricType={metricType}/>
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
