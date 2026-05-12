"use client"

import { useMetricStream } from "@/services/sse/metric-stream";
import { useMetricStore } from "@/stores/metric-store";
import { LineChartWidget } from "@/components/ui/widgets/charts/LineChartWidget";

export default function Demo() {
    const { error } = useMetricStream();

    const metrics = useMetricStore((state) => (state.seriesByKey));

    return (
        <main className="min-h-screen bg-slate-50 p-6 text-slate-950">
            <div className="mx-auto max-w-7xl space-y-6">
                <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
                    <div>
                        <h1 className="text-2xl font-semibold tracking-normal">Metric Stream</h1>
                        <p className="text-sm text-slate-600">
                            Metrics received: {Object.values(metrics).reduce((count, series) => count + series.length, 0)}
                        </p>
                    </div>
                </div>
                <div className="mx-auto w-full h-80 max-w-4xl">
                    <LineChartWidget resourceId="mock-ec2-1" title="EC2 Mock" metricType="anon"></LineChartWidget>
                </div>
                {error ? (
                    <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
                        {error.message}
                    </div>
                ) : null}

                <div className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
                    <div className="overflow-x-auto">
                        <table className="min-w-full divide-y divide-slate-200 text-sm">
                            <thead className="bg-slate-100 text-left text-xs font-semibold uppercase text-slate-600">
                                <tr>
                                    <th className="px-4 py-3">Recorded</th>
                                    <th className="px-4 py-3">Metric Type</th>
                                    <th className="px-4 py-3 text-right">Usage</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-100">
                                {Object.values(metrics).every((series) => series.length === 0) ? (
                                    <tr>
                                        <td className="px-4 py-8 text-center text-slate-500" colSpan={3}>
                                            {error ? "Metric stream is disconnected." : "Waiting for metrics..."}
                                        </td>
                                    </tr>
                                ) : (
                                    Object.entries(
                                        Object.values(metrics)
                                            .flat()
                                            .reduce<Record<string, typeof metrics[string]>>((groups, metric) => {
                                                groups[metric.resource_id] = [
                                                    ...(groups[metric.resource_id] ?? []),
                                                    metric,
                                                ];

                                                return groups;
                                            }, {})
                                    ).flatMap(([resourceId, series]) => [
                                            <tr className="bg-slate-50" key={`${resourceId}:header`}>
                                                <td className="px-4 py-3 font-semibold text-slate-900" colSpan={3}>
                                                    {resourceId}
                                                </td>
                                            </tr>,
                                            ...series.map((metric) => (
                                                <tr className="hover:bg-slate-50" key={`${metric.resource_id}:${metric.metricType}:${metric.timestamp}`}>
                                                    <td className="whitespace-nowrap px-4 py-3 text-slate-700">
                                                        {new Date(metric.timestamp).toLocaleString()}
                                                    </td>
                                                    <td className="whitespace-nowrap px-4 py-3 text-slate-700">
                                                        {metric.metricType}
                                                    </td>
                                                    <td className="whitespace-nowrap px-4 py-3 text-right tabular-nums text-slate-700">
                                                        {metric.value}
                                                    </td>
                                                </tr>
                                            )),
                                        ])
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </main>
    )
}
