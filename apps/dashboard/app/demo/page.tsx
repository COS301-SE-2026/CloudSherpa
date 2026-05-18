"use client"

import { useMetricStream } from "@/services/sse/metric-stream";
import { useMetricStore } from "@/stores/metric-store";
import { LineChartWidget } from "@/components/widgets/charts/LineChart";
import { GaugeWidget } from "@/components/widgets/charts/GaugeChart";
import { WidgetContainer } from "@/components/widgets/base/WidgetContainer";
import { ConfigurableWidget } from "@/components/widgets/changeChart";

export default function Demo() {
    const { error } = useMetricStream();
    const metrics = useMetricStore((state) => (state.seriesByKey));

    const forCpuData = metrics['mock-ec2-1:anon'] || [];
    const latestCpuValue = forCpuData.length > 0 ? forCpuData[forCpuData.length-1].value : 0;

    const availableResources = ['mock-ec2-1'];
    const availableMetricTypes = ['cpu', 'memory', 'disk', 'cost', 'anon'];

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
                    <ConfigurableWidget 
                        forInitialConfiguration={{
                            forTitle: "EC2 mock",
                            resourceId: "mock-ec2-1",
                            metricType: "anon",
                            forWidgetType: "line"
                        }}
                        availableResources={availableResources}
                        availableMetricTypes={availableMetricTypes}
                    />
                    
                    <ConfigurableWidget 
                        forInitialConfiguration={{
                            forTitle: "Name",
                            resourceId: "mock-ec2-1",
                            metricType: "anon",
                            forWidgetType: "gauge"
                        }}
                        availableResources={availableResources}
                        availableMetricTypes={availableMetricTypes}
                    />
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