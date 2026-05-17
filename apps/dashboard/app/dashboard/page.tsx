"use client";

import { useState, useCallback } from "react";
import { subDays } from "date-fns";
import { DateRange } from "react-day-picker";

import { MetricType } from "@/types/metric";
import Toolbar from "@/components/dashboard/toolbar";
import Grid, { LayoutItem } from "@/components/dashboard/grid";
import { useMetricStream } from "@/services/sse/metric-stream";

export interface WidgetConfig {
  id: string;
  type: string; // e.g., 'line', 'gauge'
  title: string;
  resourceId: string; // Identifier for the resource this widget monitors
  metricType: MetricType;
}

export interface DashboardStub {
  id: string;
  label: string;
}

export default function DashboardPage() {
  const { error: streamError } = useMetricStream();

  const [isEditMode, setIsEditMode] = useState(false);
  const [selectedDashboardId, setSelectedDashboardId] = useState("ds-1");
  const [originalLayout, setOriginalLayout] = useState<LayoutItem[]>([]);
  const [dateRange, setDateRange] = useState<DateRange | undefined>({
    from: subDays(new Date(), 7),
    to: new Date(),
  });

  const [widgetConfigs, setWidgetConfigs] = useState<WidgetConfig[]>([
    { id: "1", type: "line", title: "Live CPU Usage (Mock)", resourceId: "mock-ec2-1", metricType: "anon" },
    { id: "2", type: "gauge", title: "Live Memory (Mock)", resourceId: "mock-ec2-1", metricType: "anon" },
  ]);

  const [widgetLayouts, setWidgetLayouts] = useState<LayoutItem[]>([
    { id: "1", x: 0, y: 0, w: 6, h: 4 },
    { id: "2", x: 4, y: 0, w: 6, h: 4 },
  ]);

  const [dashboards, setDashboards] = useState<DashboardStub[]>([
    { id: "ds-1", label: "Global Cost Overview" },
    { id: "ds-2", label: "AWS Production Metrics" },
    { id: "ds-3", label: "Azure Spending Forecast" },
  ]);

  const handleStartEditing = useCallback(() => {
    setOriginalLayout([...widgetLayouts]);
    setIsEditMode(true);
  }, [widgetLayouts]);

  const handleSaveEdit = useCallback(() => {
    console.log(`Saving layout for ${selectedDashboardId}...`, widgetLayouts);
    setIsEditMode(false);
  }, [widgetLayouts, selectedDashboardId]);

  const handleCancelEdit = useCallback(() => {
    setWidgetLayouts(originalLayout);
    setIsEditMode(false);
  }, [originalLayout]);

  const handleCreateDashboard = useCallback((name: string) => {
    const newDashboard: DashboardStub = {
      id: `ds-${Date.now()}`,
      label: name,
    };
    setDashboards((prev) => [...prev, newDashboard]);
    setSelectedDashboardId(newDashboard.id);
  }, []);

  const handleAddWidget = useCallback(() => {
    const id = `widget-${Date.now()}`;
    const newConfig: WidgetConfig = {
      id,
      type: "line", 
      title: "New Widget (Click to Customize)", 
      resourceId: "mock-ec2-1",
      metricType: "anon",
    };
    const newLayout: LayoutItem = { id, x: 0, y: 0, w: 6, h: 4, autoPosition: true };

    setWidgetConfigs((prev) => [...prev, newConfig]);
    setWidgetLayouts((prev) => [...prev, newLayout]);
    setIsEditMode(true);
  }, []);

  const handleDeleteWidget = useCallback((widgetId: string) => {
    setWidgetConfigs((prev) => prev.filter((w) => w.id !== widgetId));
    setWidgetLayouts((prev) => prev.filter((l) => l.id !== widgetId));
  }, []);

  const handleLayoutChange = useCallback((newLayout: LayoutItem[]) => {
    setWidgetLayouts(newLayout);
  }, []);

  return (
    <>
      <header className="sticky top-0 z-10 border-b bg-background/95 px-6 py-4">
        <Toolbar
          dashboards={dashboards}
          isEditMode={isEditMode}
          handleAddWidget={handleAddWidget}
          handleStartEditing={handleStartEditing}
          handleSaveEdit={handleSaveEdit}
          handleCancelEdit={handleCancelEdit}
          selectedDashboardId={selectedDashboardId}
          onDashboardChange={setSelectedDashboardId}
          onCreateDashboard={handleCreateDashboard}
          dateRange={dateRange}
          onDateRangeChange={setDateRange}
        />
      </header>

      {streamError && (
        <div className="mx-6 mt-4 p-3 bg-destructive/10 border border-destructive/80 rounded-md text-destructive text-xs">
          Stream Error: {streamError.message}. Real-time updates may be paused.
        </div>
      )}

      <main className="flex-1 overflow-y-auto overflow-x-hidden m-3">
        <Grid
          isEditMode={isEditMode}
          dashboardId={selectedDashboardId}
          onLayoutChange={handleLayoutChange}
          configs={widgetConfigs}
          layouts={widgetLayouts}
          onDeleteWidget={handleDeleteWidget}
        />
      </main>
    </>
  );
}
