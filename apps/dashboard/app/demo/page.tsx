"use client"

import { useMetricStream } from "@/services/sse/metric-stream";
import { useMetricStore } from "@/stores/metric-store";
import { useFetchMetrics } from "@/hooks/useFetchMetrics";
import { ConfigurableWidget } from "@/components/widgets/changeChart";

export default function Demo() {
    const { error } = useMetricStream();
    const metrics = useMetricStore((state) => (state.seriesByKey));
    const getResourceList = useMetricStore((state) => state.getResourceList);
    const getMetricList = useMetricStore((state) => state.getMetricList);

    const forCpuData = metrics['74266597-141c-3ecc-8f68-8667ff7163a7:cpu'] || [];
    const latestCpuValue = forCpuData.length > 0 ? forCpuData[forCpuData.length-1].value : 0;
    useFetchMetrics();

    const availableResources = getResourceList();
    const availableMetricTypes = getMetricList();

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
                            id: crypto.randomUUID(),
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
                            id: crypto.randomUUID(),
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
