"use client";

import { useState, useCallback } from "react";
import { subDays } from "date-fns";
import { DateRange } from "react-day-picker";

import Toolbar from "@/components/dashboard/toolbar";
import Grid, { LayoutItem } from "@/components/dashboard/grid";
import { useMetricStream } from "@/services/sse/metric-stream";

export interface WidgetLayout {
  id: string;
  x: number;
  y: number;
  w: number;
  h: number;
}

export interface WidgetConfig extends WidgetLayout {
  type: string; // e.g., 'line', 'gauge'
  title: string;
  resourceId: string; // Identifier for the resource this widget monitors
  metricType: string;
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

  const [widgets, setWidgets] = useState<WidgetConfig[]>([
    {
      id: "1",
      type: "line",
      title: "Live CPU Usage (Mock)",
      resourceId: "mock-ec2-1",
      metricType: "anon",
      x: 0,
      y: 0,
      w: 6,
      h: 4,
    },
    {
      id: "2",
      type: "gauge",
      title: "Live Memory (Mock)",
      resourceId: "mock-ec2-1",
      metricType: "anon",
      x: 6,
      y: 0,
      w: 6,
      h: 4,
    },
  ]);

  const [dashboards, setDashboards] = useState<DashboardStub[]>([
    { id: "ds-1", label: "Global Cost Overview" },
    { id: "ds-2", label: "AWS Production Metrics" },
    { id: "ds-3", label: "Azure Spending Forecast" },
  ]);

  const handleStartEditing = useCallback(() => {
    setOriginalLayout(widgets.map((w) => ({ id: w.id, x: w.x, y: w.y, w: w.w, h: w.h })));
    setIsEditMode(true);
  }, [widgets]);

  const handleSaveEdit = useCallback(() => {
    console.log(`Saving widgets for ${selectedDashboardId} to CloudSherpa DB...`, widgets);
    setIsEditMode(false);
  }, [widgets, selectedDashboardId]);

  const handleCancelEdit = useCallback(() => {
    setWidgets((prev) =>
      prev.map((w) => {
        const orig = originalLayout.find((o) => o.id === w.id);
        return orig ? { ...w, ...orig } : w;
      }),
    );
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

  const handleLayoutChange = (newLayout: LayoutItem[]) => {
    // update widget coords
    setWidgets((prev) =>
      prev.map((w) => {
        const match = newLayout.find((l) => l.id === w.id);
        return match ? { ...w, x: match.x, y: match.y, w: match.w, h: match.h } : w;
      }),
    );
  };

  const handleAddWidget = () => {};
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
        <div className="mx-6 mt-4 p-3 bg-destructive/10 border border-destructive/20 rounded-md text-destructive text-xs">
          Stream Error: {streamError.message}. Real-time updates may be paused.
        </div>
      )}

      <main className="flex-1 overflow-y-auto overflow-x-hidden m-3">
        <Grid
          isEditMode={isEditMode}
          dashboardId={selectedDashboardId}
          onLayoutChange={handleLayoutChange}
          widgets={widgets}
        />
      </main>
    </>
  );
}
