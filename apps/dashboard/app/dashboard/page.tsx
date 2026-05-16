"use client";

import { useState, useCallback } from "react";
import { subDays } from "date-fns";
import { DateRange } from "react-day-picker";

import Toolbar from "@/components/dashboard/toolbar";
import Grid, { LayoutItem} from "@/components/dashboard/grid";

export interface WidgetConfig {
  id: string;
  type: string;
  title: string;
  x: number;
  y: number;
  w: number;
  h: number;
}

export interface DashboardStub {
  id: string;
  label: string;
}

export default function DashboardPage() {
  const [isEditMode, setIsEditMode] = useState(false);
  const [selectedDashboardId, setSelectedDashboardId] = useState("ds-1");
  const [originalLayout, setOriginalLayout] = useState<LayoutItem[]>([]);
  const [dateRange, setDateRange] = useState<DateRange | undefined>({
    from: subDays(new Date(), 7),
    to: new Date(),
  });

  const [widgets, setWidgets] = useState<WidgetConfig[]>([
    { id: "1", type: "anomaly", title: "Cost Anomalies", x: 0, y: 0, w: 4, h: 2 },
    { id: "2", type: "forecast", title: "Spending Forecast", x: 4, y: 0, w: 8, h: 4 },
  ]);

  const [dashboards, setDashboards] = useState<DashboardStub[]>([
    { id: "ds-1", label: "Global Cost Overview" },
    { id: "ds-2", label: "AWS Production Metrics" },
    { id: "ds-3", label: "Azure Spending Forecast" },
  ]);
  
  const handleStartEditing = useCallback(() => {
    setOriginalLayout(widgets.map(w => ({ id: w.id, x: w.x, y: w.y, w: w.w, h: w.h })));
    setIsEditMode(true);
  }, [widgets]);

  const handleSaveEdit = useCallback(() => {
    console.log(`Saving widgets for ${selectedDashboardId} to CloudSherpa DB...`, widgets);
    setIsEditMode(false);
  }, [widgets, selectedDashboardId]);

  const handleCancelEdit = useCallback(() => {
    setWidgets(prev => prev.map(w => {
      const orig = originalLayout.find(o => o.id === w.id);
      return orig ? { ...w, ...orig } : w;
    }));
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
    setWidgets(prev => prev.map(w => { 
      const match = newLayout.find(l => l.id === w.id);
      return match ? { ...w, x: match.x, y: match.y, w: match.w, h: match.h } : w;
    }));
  };
  return (
    <>
      <header className="sticky top-0 z-10 border-b bg-background/95 px-6 py-4">
        <Toolbar
          dashboards={dashboards}
          isEditMode={isEditMode}
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