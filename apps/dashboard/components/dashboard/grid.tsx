"use client";
import React, { useLayoutEffect, useRef, useEffect } from "react";
import "gridstack/dist/gridstack.min.css";
import { GridStack, GridItemHTMLElement, GridStackWidget } from "gridstack";
import { LayoutItem } from "@/types/widgets";
import { WidgetWrapper } from "@/components/molecules/widgetWrapper";

interface GridProps {
  isEditMode: boolean;
  dashboardId: string;
  onLayoutChange: (layout: LayoutItem[]) => void;
  layouts: LayoutItem[];
  onDeleteWidget: (layoutId: string, widgetId: string) => void;
}

export default function Grid({
  isEditMode,
  dashboardId: _dashboardId,
  onLayoutChange,
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
      
      gridStackInstance.current.on("change", (_event, nodes) => {
        if (gridStackInstance.current && isEditModeRef.current && nodes) {
          // Use the save callback to re-inject the widgetId from the DOM into the state update
          const fullLayout = gridStackInstance.current.save(false, false, (node, w: GridStackWidget) => {
            (w as LayoutItem).widgetId = node.el?.getAttribute("data-widget-id") || "";
          }) as LayoutItem[];
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
    if (!gridStackInstance.current) return;

    // Use batchUpdate to prevent multiple re-layouts during synchronization
    gridStackInstance.current.batchUpdate();

    // This needs to run in both edit and view mode so initial layouts position correctly
    const newItems = gridRef.current?.querySelectorAll<GridItemHTMLElement>(".grid-stack-item"); // Specify type here
    newItems?.forEach((el) => {
      // el is already typed as GridItemHTMLElement due to querySelectorAll<GridItemHTMLElement>
      if (!el.gridstackNode) {
        gridStackInstance.current?.makeWidget(el);
      }
    });


    const layoutIds = new Set(layouts.map((l) => l.id));
    const nodesToRemove = gridStackInstance.current.engine.nodes.filter(
      (n) => n.id && !layoutIds.has(n.id)
    );
    nodesToRemove.forEach((node) => {
      gridStackInstance.current?.removeWidget(node.el!, false, false); //tell dom to let gristack handle it
    });

    gridStackInstance.current.compact(); //.compact optimizes grid layout by reclaiming spaces, helps remove on page load layout inconsistencies
    gridStackInstance.current.batchUpdate(false);
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
        {layouts.map((l) => (
          <WidgetWrapper 
            key={l.id} 
            layout={l} 
            isEditMode={isEditMode} 
            onDeleteWidget={onDeleteWidget}
          />
        ))}
      </div>
    </div>
  );
}