import React from "react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/atoms/card";
import { LineChartWidget } from "@/features/dashboard/components/widgetGrid/widgets/charts/LineChart";
import { GaugeWidget } from "@/features/dashboard/components/widgetGrid/widgets/charts/GaugeChart";
import { MetricType } from "@/features/dashboard/types/metric";

interface BaseChartProps {
  title: string;
  resourceId: string;
  metricType: MetricType;
}

const CHART_COMPONENTS: Record<string, React.ComponentType<BaseChartProps>> = {
  line: LineChartWidget,
  gauge: GaugeWidget,
  // 'bar': BarChart,
};

interface WidgetProps {
  title: string;
  chartType: string;
  resourceId: string;
  metricType: MetricType; //ensure typesafety
}

export default function Widget({ title, chartType, resourceId, metricType }: WidgetProps) {
  const ChartComponent = CHART_COMPONENTS[chartType];

  return (
    <Card className="flex flex-col h-full w-full overflow-hidden border border-border rounded-md shadow-sm bg-card">
      <CardHeader className="flex flex-row items-center justify-start border-b border-border  px-4 py-2 space-y-0 h-12">
        <CardTitle className="text-sm font-semibold text-foreground">
          {title}
        </CardTitle>
      </CardHeader>

      <CardContent className="flex-1 w-full relative p-0 overflow-hidden min-h-10">
        {ChartComponent ? (
          <ChartComponent 
            resourceId={resourceId} 
            metricType={metricType} 
            title={title} 
          />
        ) : (
          <div className="flex items-center justify-center h-full text-muted-foreground italic text-xs">
            Unknown Chart Type: {chartType}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
