"use client";
import * as echarts from "echarts/core";

import { LineChart, GaugeChart } from "echarts/charts";

import {
    GridComponent,
    TooltipComponent,
    LegendComponent,
    DataZoomComponent,
    DatasetComponent,
} from "echarts/components";

import { CanvasRenderer } from "echarts/renderers";

echarts.use([
    LineChart,
    GaugeChart,
    GridComponent,
    TooltipComponent,
    LegendComponent,
    DataZoomComponent,
    DatasetComponent,
    CanvasRenderer,
]);

export * as echarts from "echarts/core";
