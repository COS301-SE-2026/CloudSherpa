"use client";
import React, { useLayoutEffect, useRef, useEffect } from "react";
import "gridstack/dist/gridstack.min.css";
import { GridStack } from "gridstack";
import { WidgetWrapper } from "@/components/molecules/widgetWrapper";
import { type WidgetConfig } from "@/app/dashboard/page";

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
  onLayoutChange: (layout: LayoutItem[]) => void;
  widgets: WidgetConfig[];
}

export default function Grid({ isEditMode, onLayoutChange, widgets }: GridProps) {  
  const gridRef = useRef<HTMLDivElement>(null);
  const gridStackInstance = useRef<GridStack | null>(null);
  
  const isEditModeRef = useRef(isEditMode);

  useEffect(() => {
    isEditModeRef.current = isEditMode;
  }, [isEditMode]);

  useLayoutEffect(() => {
    if (gridRef.current && !gridStackInstance.current) {
      gridStackInstance.current = GridStack.init(
        {
          cellHeight: 100, //handles row heights that widgets snap to 
          margin: 12, //layer around every widget. meaning there is 24px margin between every widget
          handle: ".drag-handle",
          staticGrid: !isEditMode, //lock grid not in edit mode
          float: false,
          resizable: { handles: "se" }, // part of library handles widget resizing from "south-east"/bottom-right corner

          columnOpts: {
            breakpointForWindow: true,
            breakpoints: [{ w: 768, c: 1 }], // at 768px (standard mobile/tablet) 
          },
        },
        gridRef.current,
      );

      gridStackInstance.current.on("change", () => {
        if (gridStackInstance.current && isEditModeRef.current) {
          const fullLayout = gridStackInstance.current.save() as LayoutItem[];
          onLayoutChange(fullLayout);
        }
      });
    }

    return () => {
      gridStackInstance.current?.destroy(false);
      gridStackInstance.current = null;
    };
  }, []);

  useEffect(() => {
    if (gridStackInstance.current && !isEditMode) {
      gridStackInstance.current.load(widgets);
      gridStackInstance.current.compact();
    }
  }, [widgets, isEditMode]);

  useEffect(() => {
    if (gridStackInstance.current) {
      gridStackInstance.current.setStatic(!isEditMode);

      if (isEditMode) {
        gridRef.current?.classList.add("is-editing");
      } else {
        gridRef.current?.classList.remove("is-editing");
      }
    }
  }, [isEditMode]);

  return (
    <div className="bg-background min-h-screen">
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
