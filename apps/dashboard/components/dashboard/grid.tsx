"use client";
import React, { useLayoutEffect, useRef, useEffect } from "react";
import "gridstack/dist/gridstack.min.css";
import { GridStack } from "gridstack";
import { WidgetWrapper } from "@/components/molecules/widgetWrapper";
import { type WidgetConfig } from "@/app/dashboard/page";
import Widget from "@/components/widgets/base/Widget";

export interface LayoutItem {
  id: string;
  x: number;
  y: number;
  w: number;
  h: number;
  autoPosition?: boolean;
}

interface GridProps {
  isEditMode: boolean;
  dashboardId: string;
  onLayoutChange: (layout: LayoutItem[]) => void;
  configs: WidgetConfig[];
  layouts: LayoutItem[];
  onDeleteWidget: (widgetId: string) => void;
}

export default function Grid({
  isEditMode,
  dashboardId,
  onLayoutChange,
  configs,
  layouts,
  onDeleteWidget,
}: GridProps) {
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

  // handle dashboard switching - load the whole layout only once per dashboard
  useEffect(() => {
    if (gridStackInstance.current && layouts.length > 0) {
      gridStackInstance.current.load(layouts);
    }
  }, [dashboardId]);

  useEffect(() => {
    if (!gridStackInstance.current) return;

    if (isEditMode) {
      gridStackInstance.current.batchUpdate();

      const newItems = gridRef.current?.querySelectorAll(".grid-stack-item:not(.ui-draggable)");
      newItems?.forEach((el) => {
        gridStackInstance.current?.makeWidget(el as HTMLElement);
      });

      const widgetIds = new Set(layouts.map((l) => l.id));
      const nodesToRemove = gridStackInstance.current.engine.nodes.filter(
        (n) => n.id && !widgetIds.has(n.id)
      );
      nodesToRemove.forEach((node) => {
        gridStackInstance.current?.removeWidget(node.el!, false, false);
      });

      gridStackInstance.current.compact();
      gridStackInstance.current.batchUpdate(false);
    }
  }, [layouts, isEditMode]);

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
        {layouts.map((l) => {
          const config = configs.find((c) => c.id === l.id);
          if (!config) return null;
          
          return (
            <WidgetWrapper 
              key={l.id} 
              {...l} 
              title={config.title}
              isEditMode={isEditMode} 
              onDeleteWidget={onDeleteWidget}
            >
              <Widget
                title={config.title || "Untitled Widget"}
                chartType={config.type}
                resourceId={config.resourceId}
                metricType={config.metricType}
              />
            </WidgetWrapper>
          );
        })}
      </div>
    </div>
  );
}