"use client";

import React, { useEffect, useState } from "react";
import Widget from "@/features/dashboard/components/widgetGrid/widgets/widget";
import { WidgetConfig } from "@/features/dashboard/types/widgets";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { MetricSeries } from "@/features/dashboard/types/metric";
import { useWindowStore } from "@/features/dashboard/stores/window-store";

const MOCK_CHART_WIDGETS: WidgetConfig[] = [
    {
        id: "mock-widget-1",
        widgetType: "chart",
        chartType: "line_chart",
        displayName: "Server CPU Load (Mock)",
        resourceId: "demo-server-01",
        metricType: "cpu",
    },
    {
        id: "mock-widget-2",
        widgetType: "chart",
        chartType: "gauge_chart",
        displayName: "Memory Utilization (Mock)",
        resourceId: "demo-server-01",
        metricType: "memory",
    },
];

const MOCK_KPI_WIDGETS: WidgetConfig[] = [
    {
        id: "mock-kpi-widget-1",
        displayName: "Mock KPI 1",
        widgetType: "kpi",
        chargeIds: ["resource-1"],
        aggregationWindowDays: 30,
    },
];

export default function DemoPage() {
    const [isReady, setIsReady] = useState(false);

    useEffect(() => {
        const now = Date.now();
        const secureRandom = () => {
            const a = new Uint32Array(1);
            crypto.getRandomValues(a);
            return a[0] / 0x100000000;
        };

        const mockCpuSeries: MetricSeries = Array.from({ length: 60 }).reduce(
            (acc: MetricSeries, _, i) => {
                const timestampNum = now - (60 - i) * 5000;
                const timestampStr = new Date(timestampNum).toISOString();

                const value = 40 + Math.sin(i * 0.5) * 20 + secureRandom() * 10;

                acc[timestampStr] = {
                    timestamp: timestampStr,
                    value: value,
                    resource_id: "demo-server-01",
                    metricType: "cpu",
                };

                return acc;
            },
            {}
        );

        const memoryTimestampStr = new Date(now).toISOString();
        const mockMemorySeries: MetricSeries = {
            [memoryTimestampStr]: {
                timestamp: memoryTimestampStr,
                value: 72.5,
                resource_id: "demo-server-01",
                metricType: "memory",
            },
        };

        useMetricStore.setState((state) => ({
            ...state,
            seriesByKey: {
                ...state.seriesByKey,
                "demo-server-01:cpu": mockCpuSeries,
                "demo-server-01:memory": mockMemorySeries,
            },
        }));
        useWindowStore.setState({
            fromMs: now - 300_000,
            toMs: now,
        });
        // eslint-disable-next-line react-hooks/set-state-in-effect
        setIsReady(true);
    }, []);

    return (
        <div className="p-8 min-h-screen bg-background">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 auto-rows-[350px]">
                {isReady &&
                    [...MOCK_CHART_WIDGETS, ...MOCK_KPI_WIDGETS].map((config) => (
                        <div key={config.id} className="w-full h-full">
                            <Widget config={config} />
                        </div>
                    ))}
            </div>
        </div>
    );
}
