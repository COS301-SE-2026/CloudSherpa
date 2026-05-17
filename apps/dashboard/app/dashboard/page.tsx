"use client";

import { useState, useCallback, useEffect } from "react";
import { subDays } from "date-fns";
import { DateRange } from "react-day-picker";

import Toolbar from "@/components/dashboard/toolbar";
import Grid from "@/components/dashboard/grid";
import { WidgetConfig, LayoutItem } from "@/types/widgets";
import { useDashboardStore, DashboardStore } from "@/stores/dashboard-store";
import { useMetricStream } from "@/services/sse/metric-stream";

export interface DashboardStub {
  id: string;
  label: string;
}

export default function DashboardPage() {
  const { error: streamError } = useMetricStream();

  const [isEditMode, setIsEditMode] = useState(false);
  const [selectedDashboardId, setSelectedDashboardId] = useState("ds-1");
  const [originalLayout, setOriginalLayout] = useState<LayoutItem[]>([]);
  const [originalConfigs, setOriginalConfigs] = useState<WidgetConfig[]>([]);
  const [dateRange, setDateRange] = useState<DateRange | undefined>({
    from: subDays(new Date(), 7),
    to: new Date(),
  });

  const layoutsMap = useDashboardStore((state: DashboardStore) => state.layouts);
  const widgetsMap = useDashboardStore((state: DashboardStore) => state.widgets);
  const { setInitialState, addWidget, removeWidget, updateLayouts } = useDashboardStore((state: DashboardStore) => state.actions);

  // Initialize mock data into store
  useEffect(() => {
    const initialConfigs: WidgetConfig[] = [
        { id: "w-1", title: "Live CPU Usage (Mock)", resourceId: "mock-ec2-1", metricType: "anon", chartType: "line" },
        { id: "w-2", title: "Live Memory (Mock)", resourceId: "mock-ec2-1", metricType: "anon", chartType: "gauge" },
    ];
    const initialLayouts: LayoutItem[] = [
        { id: "l-1", widgetId: "w-1", x: 0, y: 0, w: 6, h: 4 },
        { id: "l-2", widgetId: "w-2", x: 4, y: 0, w: 6, h: 4 },
    ];
    setInitialState(initialLayouts, initialConfigs);
  }, [setInitialState]);

  const widgetLayouts = Object.values(layoutsMap);

  const [dashboards, setDashboards] = useState<DashboardStub[]>([
    { id: "ds-1", label: "Global Cost Overview" },
    { id: "ds-2", label: "AWS Production Metrics" },
    { id: "ds-3", label: "Azure Spending Forecast" },
  ]);

  const handleStartEditing = useCallback(() => {
    setOriginalLayout(Object.values(layoutsMap).map(l => ({ ...l })));
    setOriginalConfigs(Object.values(widgetsMap).map(c => ({ ...c })));
    setIsEditMode(true);
  }, [layoutsMap, widgetsMap]);

  const handleSaveEdit = useCallback(() => {
    console.log(`Saving layout for ${selectedDashboardId}...`, layoutsMap);
    setIsEditMode(false);
  }, [layoutsMap, selectedDashboardId]);

  const handleCancelEdit = useCallback(() => {
    setInitialState(originalLayout, originalConfigs);
    setIsEditMode(false);
  }, [originalLayout, originalConfigs, setInitialState]);

  const handleCreateDashboard = useCallback((name: string) => {
    const newDashboard: DashboardStub = {
      id: `ds-${Date.now()}`,
      label: name,
    };
    setDashboards((prev) => [...prev, newDashboard]);
    setSelectedDashboardId(newDashboard.id);
  }, []);

  // use UUID's for widget and layout id's so in theory won't be a clash
  const handleAddWidget = useCallback(() => {
    const widgetId = crypto.randomUUID();
    const layoutId = crypto.randomUUID();
    const newConfig: WidgetConfig = {
      id: widgetId,
      chartType: "line", 
      title: "New Widget (Click to Customize)", 
      resourceId: "mock-ec2-1",
      metricType: "anon",
    };
    const newLayout: LayoutItem = { id: layoutId, widgetId, x: 0, y: 0, w: 6, h: 4, autoPosition: true };

    addWidget(newLayout, newConfig);
    setIsEditMode(true);
  }, [addWidget]);

  const handleDeleteWidget = useCallback((layoutId: string, widgetId: string) => {
    removeWidget(layoutId, widgetId);
  }, [removeWidget]);

  const handleLayoutChange = useCallback((newLayout: LayoutItem[]) => {
    updateLayouts(newLayout);
  }, [updateLayouts]);

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
          layouts={widgetLayouts}
          onDeleteWidget={handleDeleteWidget}
        />
      </main>
    </>
  );
}
