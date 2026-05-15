"use client";
import React, { useLayoutEffect, useRef, useEffect } from "react";
import "gridstack/dist/gridstack.min.css";
import { GridStack } from "gridstack";
import { WidgetWrapper } from "@/components/molecules/widgetWrapper";
import { DateRange } from "react-day-picker";

interface WidgetConfig {
  id: string;
  type: string;
  title: string;
  x: number;
  y: number;
  w: number;
  h: number;
}
export interface LayoutItem {
  id: string;
  x: number;
  y: number;
  w: number;
  h: number;
}

interface GridProps {
  isEditMode: boolean;
  dashboardId: string;
  dateRange: DateRange | undefined;
  onLayoutChange: (layout: LayoutItem[]) => void;
}

export default function Grid({ isEditMode, dashboardId, dateRange, onLayoutChange }: GridProps) {  
  const gridRef = useRef<HTMLDivElement>(null);
  const gridStackInstance = useRef<GridStack | null>(null);

  const [widgets] = React.useState<WidgetConfig[]>([
    { id: "1", type: "anomaly", title: "Cost Anomalies", x: 0, y: 0, w: 4, h: 2 },
    { id: "2", type: "forecast", title: "Spending Forecast", x: 4, y: 0, w: 8, h: 4 },
  ]);

  useLayoutEffect(() => {
    if (gridRef.current && !gridStackInstance.current) {
      gridStackInstance.current = GridStack.init({ /* ... same options ... */ }, gridRef.current);

      gridStackInstance.current.on("change", (event, items) => {
        if (!gridStackInstance.current) return;
        
        const fullLayout = gridStackInstance.current.save() as LayoutItem[];
        onLayoutChange(fullLayout);
      });
    }
  }, []);

useEffect(() => {
  if (gridStackInstance.current) {
    gridStackInstance.current.setStatic(!isEditMode);
    
    if (isEditMode) {
      gridRef.current?.classList.add('is-editing');
    } else {
      gridRef.current?.classList.remove('is-editing');
    }
  }
}, [isEditMode]);

  useLayoutEffect(() => {
    if (gridRef.current && !gridStackInstance.current) {
      gridStackInstance.current = GridStack.init(
        {
          cellHeight: 100,
          margin: 12,
          handle: ".drag-handle",
          staticGrid: !isEditMode, 
          float: true,
          resizable: { handles: "se" },
        },
        gridRef.current,
      );

      gridStackInstance.current.on("change", (event, items) => {
        console.log("Layout updated for CloudSherpa:", items);
      });
    }

    return () => {
      gridStackInstance.current?.destroy(false);
      gridStackInstance.current = null;
    };
  }, []);

  return (
    <div className="p-4 bg-background min-h-screen">
      <div ref={gridRef} className="grid-stack">
        {widgets.map((w) => (
          <WidgetWrapper key={w.id} {...w} isEditMode={isEditMode}>
            <div className="flex items-center justify-center h-full text-muted-foreground italic text-sm">
              Chart Placeholder: {w.type}
            </div>
          </WidgetWrapper>
        ))}
      </div>
    </div>
  );
}
