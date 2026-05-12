"use client"

import { useEffect, useState } from "react"
import { MetricData } from "./metrictype";

export default function Demo() {

    const [metrics, setMetrics] = useState<MetricData[]>([]);

    useEffect(() => {
        const eventSource = new EventSource("http://localhost:8083/stream");

        eventSource.onopen = () => {
            console.log("SSE connected");
        };

        const handleMetric = (event: MessageEvent<string>) => {
            const metric = JSON.parse(event.data) as MetricData;
            setMetrics((currentMetrics) => [metric, ...currentMetrics]);
        };

        eventSource.onerror = (error) => {
            console.log(error);
            eventSource.close();
        }

        eventSource.addEventListener("metric", handleMetric);

        return () => {
            eventSource.removeEventListener("metric", handleMetric);
            eventSource.close();
        }
    }, [])

    return (
        <main className="min-h-screen bg-slate-50 p-6 text-slate-950">
            <div className="mx-auto max-w-7xl space-y-6">
                <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
                    <div>
                        <h1 className="text-2xl font-semibold tracking-normal">Metric Stream</h1>
                        <p className="text-sm text-slate-600">Metrics received: {metrics.length}</p>
                    </div>
                </div>

                <div className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
                    <div className="overflow-x-auto">
                        <table className="min-w-full divide-y divide-slate-200 text-sm">
                            <thead className="bg-slate-100 text-left text-xs font-semibold uppercase text-slate-600">
                                <tr>
                                    <th className="px-4 py-3">Recorded</th>
                                    <th className="px-4 py-3">Environment</th>
                                    <th className="px-4 py-3">Resource</th>
                                    <th className="px-4 py-3">Service</th>
                                    <th className="px-4 py-3 text-right">Usage</th>
                                    <th className="px-4 py-3 text-right">Cost</th>
                                    <th className="px-4 py-3">Metric ID</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-100">
                                {metrics.length === 0 ? (
                                    <tr>
                                        <td className="px-4 py-8 text-center text-slate-500" colSpan={7}>
                                            Waiting for metrics...
                                        </td>
                                    </tr>
                                ) : (
                                    metrics.map((metric) => (
                                        <tr className="hover:bg-slate-50" key={metric.metric_id}>
                                            <td className="whitespace-nowrap px-4 py-3 text-slate-700">
                                                {new Date(metric.recorded_at).toLocaleString()}
                                            </td>
                                            <td className="whitespace-nowrap px-4 py-3 font-medium text-slate-900">
                                                {metric.environment_id}
                                            </td>
                                            <td className="whitespace-nowrap px-4 py-3 text-slate-700">
                                                {metric.resource_id}
                                            </td>
                                            <td className="whitespace-nowrap px-4 py-3 text-slate-700">
                                                {metric.service_category}
                                            </td>
                                            <td className="whitespace-nowrap px-4 py-3 text-right tabular-nums text-slate-700">
                                                {metric.usage_amount} {metric.usage_unit}
                                            </td>
                                            <td className="whitespace-nowrap px-4 py-3 text-right tabular-nums font-medium text-slate-900">
                                                {metric.currency} {metric.cost_amount.toFixed(2)}
                                            </td>
                                            <td className="max-w-48 truncate px-4 py-3 font-mono text-xs text-slate-500">
                                                {metric.metric_id}
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </main>
    )
}
