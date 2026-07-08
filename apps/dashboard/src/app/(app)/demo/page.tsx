"use client";

import { useEffect, useState } from "react";
import Widget from "@/features/dashboard/components/widgetGrid/widgets/widget";
import { WidgetConfig } from "@/features/dashboard/types/widgets";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";

const MOCK_WIDGETS: WidgetConfig[] = [
  {
    id: "mock-widget-1",
    chartType: "line",
    title: "Server CPU Load (Mock)",
    resourceId: "demo-server-01",
    metricType: "cpu",
  },
  {
    id: "mock-widget-2",
    chartType: "gauge",
    title: "Memory Utilization (Mock)",
    resourceId: "demo-server-01",
    metricType: "memory",
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

    const mockCpuSeries: [number, number][] = Array.from({ length: 60 }).map((_, i) => {
      const timestampMs = now - (60 - i) * 5000;
      const value = 40 + Math.sin(i * 0.5) * 20 + secureRandom() * 10;
      return [timestampMs, value];
    });

    const mockMemorySeries: [number, number][] = [[now, 72.5]];

    useMetricStore.setState((state) => ({
      ...state,
      seriesByKey: {
        ...state.seriesByKey,
        "demo-server-01:cpu": mockCpuSeries,
        "demo-server-01:memory": mockMemorySeries,
      },
    }));

    queueMicrotask(() => setIsReady(true));
  }, []);

  return (
    <div className="p-8 min-h-screen bg-background">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 auto-rows-[350px]">
        {isReady &&
          MOCK_WIDGETS.map((config) => (
            <div key={config.id} className="w-full h-full">
              <Widget config={config} />
            </div>
          ))}
      </div>
    </div>
  );
}