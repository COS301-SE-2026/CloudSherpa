"use client";

import dynamic from "next/dynamic";
import type { EChartsOption } from "echarts";

const ReactECharts = dynamic(() => import("echarts-for-react"), {
  ssr: false,
  loading: () => <div className="h-full w-full animate-pulse bg-muted rounded-md" />,
});

export function BaseChart({ option, theme  }: Readonly<{ option: EChartsOption, theme?: string }>) {  return (
    <ReactECharts
      option={option}
      theme={theme}
      style={{ height: "100%", width: "100%", minHeight: "100px" }}
      notMerge={true}
      lazyUpdate={true}
      
    />
  );
}
