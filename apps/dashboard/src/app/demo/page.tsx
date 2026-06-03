"use client"

import { useMetricStream } from "@/features/dashboard/services/sse/metric-stream";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { useFetchMetrics } from "@/features/dashboard/hooks/useFetchMetrics";
import { metricSeriesToArray } from "@/features/dashboard/types/metric";
import { useMemo } from "react";

export default function Demo() {
    const { error } = useMetricStream();
    const metrics = useMetricStore((state) => (state.seriesByKey));

    const cpuSeries = metrics['74266597-141c-3ecc-8f68-8667ff7163a7:cpu'];
    const forCpuData = useMemo(() => metricSeriesToArray(cpuSeries), [cpuSeries]);
    const latestCpuValue = forCpuData.length > 0 ? forCpuData.at(-1)?.value : 0;
    useFetchMetrics();

    return (
        <main className="min-h-screen bg-slate-50 p-6 text-slate-950">
            <div className="mx-auto max-w-7xl space-y-6">
                <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
                    <div>
                        <h1 className="text-2xl font-semibold tracking-normal">Metric Stream</h1>
                        <p className="text-sm text-slate-600">
                            {/* Metrics received: {Object.values(metrics).reduce((count, series) => count + Object.keys(series).length, 0)} */}
                        </p>
                        <p className="text-sm text-blue-600">
                            Current CPU: {latestCpuValue}%
                        </p>
                    </div>
                </div>

                {/*added widgets that will be adjusted from all sides*/}
                <div className="flex flex-wrap gap-6 justify-center">
                    {/* <ConfigurableWidget
                        initialConfig={{
                            id: crypto.randomUUID(),
                            title: "EC2 mock",
                            resourceId: "mock-ec2-1",
                            metricType: "anon",
                            widgetType: "line"
                        }}
                    /> */}
{/* 
                    <ConfigurableWidget
                        initialConfig={{
                            id: crypto.randomUUID(),
                            title: "Name",
                            resourceId: "mock-ec2-1",
                            metricType: "anon",
                            widgetType: "gauge"
                        }}
                    /> */}
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
