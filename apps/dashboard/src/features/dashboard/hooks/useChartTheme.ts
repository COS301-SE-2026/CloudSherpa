"use client";

import { useTheme } from "next-themes";
import * as echarts from "echarts";

import lightTokens from "@/app/tokens/chart-light.json";
import darkTokens from "@/app/tokens/chart-dark.json";

//note echarts is not a react library so fo the themes to work it has to be defined globally before any chart is rendred
//thus we have if(typoef globalTHis.window !=="undefined")

const createEChartsTheme = (tokens: Record<string, string>) => ({
  color: [tokens["chart-1"], tokens["chart-2"], tokens["chart-3"], tokens["chart-4"], tokens["chart-5"]],
  backgroundColor: "transparent",
  textStyle: {
    fontFamily: "inherit",
    color: tokens["foreground"],
  },
  tooltip: {
    backgroundColor: tokens["card"],
    borderColor: tokens["border"],
    borderWidth: 1,
    textStyle: { color: tokens["card-foreground"], fontSize: 12 },
    padding: [8, 12],
    extraCssText: "border-radius: var(--radius); box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1);",
    axisPointer: {
      type: "line",
      lineStyle: { color: tokens["border"], width: 1, type: "dashed" },
    },
  },
  line: {
    smooth: 0.3,
    lineStyle: { width: 2.5 },
    symbol: "none",
  },
  categoryAxis: {
    axisLine: { show: false },
    axisTick: { show: false },
    splitLine: { show: false },
    axisLabel: { color: tokens["muted-foreground"], margin: 16 },
  },
  timeAxis: {
    axisLine: { show: false },
    axisTick: { show: false },
    splitLine: { show: false },
    axisLabel: { color: tokens["muted-foreground"], margin: 16 },
  },
  valueAxis: {
    axisLine: { show: false },
    axisTick: { show: false },
    splitLine: {
      show: true,
      lineStyle: { color: tokens["border"], type: "dashed", opacity: 0.6 },
    },
    axisLabel: { color: tokens["muted-foreground"], margin: 16 },
  },
});

if ("window" in globalThis) {
  echarts.registerTheme("cloudSherpaLight", createEChartsTheme(lightTokens));
  echarts.registerTheme("cloudSherpaDark", createEChartsTheme(darkTokens));
}

export function useChartTheme() {
  const { theme, systemTheme } = useTheme();

  const currentTheme = theme === "system" ? systemTheme : theme;
  const isDark = currentTheme === "dark";

  return {
    themeName: isDark ? "cloudSherpaDark" : "cloudSherpaLight",
    tokens: isDark ? darkTokens : lightTokens,
  };
}
