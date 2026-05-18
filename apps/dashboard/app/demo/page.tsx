"use client"

import { useMetricStream } from "@/services/sse/metric-stream";
import { useMetricStore } from "@/stores/metric-store";
import { LineChartWidget } from "@/components/widgets/charts/LineChart";
import { GaugeWidget } from "@/components/widgets/charts/GaugeChart";
import { WidgetContainer } from "@/components/widgets/base/WidgetContainer";
import { useFetchMetrics } from "@/hooks/useFetchMetrics";

export default function Demo() {
    const { error } = useMetricStream();
    const metrics = useMetricStore((state) => (state.seriesByKey));

    const forCpuData = metrics['74266597-141c-3ecc-8f68-8667ff7163a7:cpu'] || [];
    const latestCpuValue = forCpuData.length > 0 ? forCpuData[forCpuData.length-1].value : 0;
    useFetchMetrics();

    return (
        <main className="min-h-screen bg-slate-50 p-6 text-slate-950">
            <div className="mx-auto max-w-7xl space-y-6">
                <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
                    <div>
                        <h1 className="text-2xl font-semibold tracking-normal">Metric Stream</h1>
                        <p className="text-sm text-slate-600">
                            Metrics received: {Object.values(metrics).reduce((count, series) => count + series.length, 0)}
                        </p>
                        <p className="text-sm text-blue-600">
                            Current CPU: {latestCpuValue}%
                        </p>
                    </div>
                </div>

                {/*added widgets that will be adjusted from all sides*/}
                <div className="flex flex-wrap gap-6 justify-center">
                    {/*this is the line chart widget*/}
                    <WidgetContainer 
                        forTitle="EC2 mock" 
                        defaultWidth={700}
                        defaultHeight={400}
                        minWidth={400}
                        minHeight={300}
                        isResizable={true}
                    >
                        <LineChartWidget 
                            resourceId="74266597-141c-3ecc-8f68-8667ff7163a7" 
                            title="" 
                            metricType="cpu"
                        />
                    </WidgetContainer>

                    {/*this is for the gauge chart*/}
                    <WidgetContainer 
                        forTitle="Name" 
                        defaultWidth={400}
                        defaultHeight={350}
                        minWidth={300}
                        minHeight={300}
                        isResizable={true}
                    >
                        <GaugeWidget 
                            resourceId="mock-ec2-1" 
                            title="" 
                            metricType="anon"
                        />
                    </WidgetContainer>
                </div>

                {error ? (
                    <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
                        {error.message}
                    </div>
                ) : null}
            </div>
        </main>
    );
}
