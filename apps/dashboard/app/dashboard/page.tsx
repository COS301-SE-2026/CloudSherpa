"use client";

import { useState, useCallback } from "react";
import { subDays } from "date-fns";
import { DateRange } from "react-day-picker";

import Toolbar from "@/components/dashboard/toolbar";
import Grid, { LayoutItem } from "@/components/dashboard/grid";

export default function DashboardPage() {
  const [isEditMode, setIsEditMode] = useState(false);
  const [selectedDashboardId, setSelectedDashboardId] = useState("ds-1");
  const [currentLayout, setCurrentLayout] = useState<LayoutItem[]>([]);
  const [dateRange, setDateRange] = useState<DateRange | undefined>({
    from: subDays(new Date(), 7),
    to: new Date(),
  });
  
  const handleEditModeChange = useCallback((val: boolean) => {
    setIsEditMode(val);
    //future pesistence logic for saving layout once exit edit mode
    if (!val && currentLayout.length > 0) {
      console.log(`Saving layout for ${selectedDashboardId} to CloudSherpa DB...`, currentLayout);
    }
  }, [currentLayout, selectedDashboardId]);

  const handleLayoutChange = (newLayout: LayoutItem[]) => {
    setCurrentLayout(newLayout);
  };
  return (
    <>
      <header className="sticky top-0 z-10 border-b bg-background/95">
        <Toolbar
          isEditMode={isEditMode}
          setIsEditMode={handleEditModeChange}
          selectedDashboardId={selectedDashboardId}
          onDashboardChange={setSelectedDashboardId}
          dateRange={dateRange}
          onDateRangeChange={setDateRange}
        />
      </header>

      <main className="flex-1 overflow-y-auto overflow-x-hidden">
        <Grid
          isEditMode={isEditMode}
          dashboardId={selectedDashboardId}
          dateRange={dateRange}
          onLayoutChange={handleLayoutChange}
        />
      </main>
    </>
  );
}