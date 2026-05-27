"use client";
import React, { useLayoutEffect, useRef, useEffect } from "react";
import "gridstack/dist/gridstack.min.css";
import { GridStack, GridItemHTMLElement, GridStackWidget, GridStackNode } from "gridstack";

import { LayoutItem } from "@/features/dashboard/types/widgets";
import { WidgetWrapper } from "@/features/dashboard/components/widgetGrid/widgetWrapper";

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

    //batchupdate prevent multiple re-layouts during synchronization
    gridStackInstance.current.batchUpdate();

    const currentGridNodes = new Map<string, GridStackNode>();
    gridStackInstance.current.engine.nodes.forEach(node => {
      if (node.id) { // node.id is the layout.id (gs-id)
        currentGridNodes.set(node.id, node);
      }
    });

    // Add/Update widgets based on the layouts prop
    layouts.forEach(layoutItem => {
      const existingNode = currentGridNodes.get(layoutItem.id);

      if (existingNode) {
        // Update existing widget's layout if properties differ
        if (
          existingNode.x !== layoutItem.x ||
          existingNode.y !== layoutItem.y ||
          existingNode.w !== layoutItem.w ||
          existingNode.h !== layoutItem.h ||
          existingNode.autoPosition !== layoutItem.autoPosition // Also check autoPosition
        ) {
          gridStackInstance.current?.update(existingNode.el!, {
            x: layoutItem.x,
            y: layoutItem.y,
            w: layoutItem.w,
            h: layoutItem.h,
            autoPosition: layoutItem.autoPosition
          });
        }
      } else {
        // This is a new widget in the layouts prop, make it a GridStack widget
        const el = gridRef.current?.querySelector(`[gs-id="${layoutItem.id}"]`) as GridItemHTMLElement;
        if (el && !el.gridstackNode) { // Only make widget if it's not already one
          gridStackInstance.current?.makeWidget(el, layoutItem);
        }
      }
    });

    // sync with gristack state with layouts prop
    const layoutIdsInProps = new Set(layouts.map((l) => l.id));
    const nodesToRemove = gridStackInstance.current.engine.nodes.filter(
      (n) => n.id && !layoutIdsInProps.has(n.id)
    );
    nodesToRemove.forEach((node) => {
      gridStackInstance.current?.removeWidget(node.el!, false, false);
    });
    //compact on change or load
    gridStackInstance.current.compact(); //.compact optimizes grid layout by reclaiming spaces, helps remove on page load layout inconsistencies
    gridStackInstance.current.batchUpdate(false);
  }, [layouts, isEditMode]);

  //lock layouts outside edit mode
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