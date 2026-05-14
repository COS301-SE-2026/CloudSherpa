"use client"
import { useState } from "react";
import Toolbar from "@/components/dashboard/toolbar";
import { subDays } from "date-fns";
import { DateRange } from "react-day-picker";
import Grid, { LayoutItem } from "@/components/dashboard/grid";

export default function Dashboard() {
  const [isEditMode, setIsEditMode] = useState(false);
  const [selectedDashboardId, setSelectedDashboardId] = useState("ds-1");
  const [currentLayout, setCurrentLayout] = useState<LayoutItem[]>([]);

  const [dateRange, setDateRange] = useState<DateRange | undefined>({
    from: subDays(new Date(), 7),
    to: new Date(),
  });

  const handleLayoutChange = (newLayout: LayoutItem[]) => {
    setCurrentLayout(newLayout);
    console.log("Dashboard state updated with new layout:", newLayout);
  };

return (
    <div className="w-full h-screen flex flex-col bg-background">
      <Toolbar 
        isEditMode={isEditMode} 
        //save layout when edit mode turned off
        setIsEditMode={(val) => {
          setIsEditMode(val);
          if (!val && currentLayout.length > 0) {
            console.log("Saving layout to CloudSherpa DB...", currentLayout);
          }
        }}
        selectedDashboardId={selectedDashboardId}
        onDashboardChange={setSelectedDashboardId}
        dateRange={dateRange}
        onDateRangeChange={setDateRange}
      />
      
<div className="flex-1 overflow-y-auto">
        <Grid 
          isEditMode={isEditMode} 
          dashboardId={selectedDashboardId}
          dateRange={dateRange}
          onLayoutChange={handleLayoutChange} // Pass the handler
        />
      </div>
    </div>
  );
}