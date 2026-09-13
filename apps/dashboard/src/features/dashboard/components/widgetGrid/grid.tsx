"use client";
import {
    useLayoutEffect,
    useRef,
    useEffect,
    forwardRef,
    useImperativeHandle,
    createRef,
    RefObject,
} from "react";
import "gridstack/dist/gridstack.min.css";
import { GridStack, GridItemHTMLElement, GridStackWidget, GridStackNode } from "gridstack";

import { LayoutItem } from "@/features/dashboard/types/widgets";
import { WidgetWrapper } from "@/features/dashboard/components/widgetGrid/widgets/widgetWrapper";

const MIN_WIDGET_W = 3;
const MIN_WIDGET_H = 3;

const repairLayout = (fullLayout: LayoutItem[]): LayoutItem[] =>
    fullLayout.map((l) => {
        return {
            ...l,
            w: Number.isFinite(l.w) && l.w > 0 ? l.w : MIN_WIDGET_W,
            h: Number.isFinite(l.h) && l.h > 0 ? l.h : MIN_WIDGET_H,
            x: Number.isFinite(l.x) ? l.x : 0,
            y: Number.isFinite(l.y) ? l.y : 0,
        };
    });

interface GridProps {
    isEditMode: boolean;
    dashboardId: string;
    onLayoutChange: (layout: LayoutItem[]) => void;
    layouts: LayoutItem[];
}

export interface GridHandle {
    compactAndGetLayout: () => LayoutItem[] | null;
}

export const gridApiRef: RefObject<GridHandle | null> = createRef();

export const Grid = forwardRef<GridHandle, Readonly<GridProps>>(function Grid(
    { isEditMode, onLayoutChange, layouts },
    ref
) {
    const gridRef = useRef<HTMLDivElement>(null);
    const gridStackInstance = useRef<GridStack | null>(null);
    const onLayoutChangeRef = useRef(onLayoutChange);
    const isInternalUpdate = useRef(false);
    const isEditModeRef = useRef(isEditMode);

    useImperativeHandle(ref, () => ({
        compactAndGetLayout: () => {
            if (!gridStackInstance.current) return null;
            isInternalUpdate.current = true;

            gridStackInstance.current.batchUpdate();
            gridStackInstance.current.compact();
            gridStackInstance.current.batchUpdate(false);

            const fullLayout = gridStackInstance.current.save(
                false,
                false,
                (node, w: GridStackWidget) => {
                    (w as LayoutItem).id = String(node.id || "");
                }
            ) as LayoutItem[];

            return repairLayout(fullLayout);
        },
    }));

    useEffect(() => {
        isEditModeRef.current = isEditMode;
    }, [isEditMode]);

    useEffect(() => {
        onLayoutChangeRef.current = onLayoutChange;
    }, [onLayoutChange]);

    useLayoutEffect(() => {
        if (gridRef.current && !gridStackInstance.current) {
            gridStackInstance.current = GridStack.init(
                {
                    cellHeight: 100, //handles row heights that widgets snap to
                    margin: 12, //layer around every widget. meaning there is 24px margin between every widget
                    handle: ".drag-handle",
                    staticGrid: !isEditModeRef.current, //lock grid not in edit mode
                    float: false,
                    animate: false, //better performance
                    minRow: 3,
                    resizable: { handles: "se" }, // part of library handles widget resizing from "south-east"/bottom-right corner

                    columnOpts: {
                        breakpointForWindow: true,
                        breakpoints: [{ w: 768, c: 1 }], // at 768px (standard mobile/tablet)
                    },
                },
                gridRef.current
            );

            gridStackInstance.current.on("change", () => {
                if (gridStackInstance.current && isEditModeRef.current) {
                    isInternalUpdate.current = true;
                    const fullLayout = gridStackInstance.current.save(
                        false,
                        false,
                        (node, w: GridStackWidget) => {
                            (w as LayoutItem).id = String(node.id || "");
                        }
                    ) as LayoutItem[];

                    const repaired = repairLayout(fullLayout);

                    onLayoutChangeRef.current(repaired);
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

        if (isInternalUpdate.current) {
            isInternalUpdate.current = false;
            return;
        }

        //batchupdate prevent multiple relayouts during sync
        gridStackInstance.current.batchUpdate();

        const currentGridNodes = new Map<string, GridStackNode>();
        gridStackInstance.current.engine.nodes.forEach((node) => {
            if (node.id) {
                // node.id is the layout.id (gs-id)
                currentGridNodes.set(node.id, node);
            }
        });

        // add/update widgets based on layouts prop
        layouts.forEach((layoutItem) => {
            const existingNode = currentGridNodes.get(layoutItem.id);

            if (existingNode) {
                // update existing widget layout if properties diff
                if (
                    existingNode.x !== layoutItem.x ||
                    existingNode.y !== layoutItem.y ||
                    existingNode.w !== layoutItem.w ||
                    existingNode.h !== layoutItem.h ||
                    existingNode.autoPosition !== layoutItem.autoPosition
                ) {
                    gridStackInstance.current?.update(existingNode.el!, {
                        x: layoutItem.x,
                        y: layoutItem.y,
                        w: layoutItem.w,
                        h: layoutItem.h,
                        minW: MIN_WIDGET_W,
                        minH: MIN_WIDGET_H,
                        autoPosition: layoutItem.autoPosition,
                    });
                }
            } else {
                //new widget to gridstack widget
                const el = gridRef.current?.querySelector(
                    `[gs-id="${layoutItem.id}"]`
                ) as GridItemHTMLElement;
                if (el && !el.gridstackNode) {
                    // only make widget if not already
                    gridStackInstance.current?.makeWidget(el, {
                        ...layoutItem,
                        minW: MIN_WIDGET_W,
                        minH: MIN_WIDGET_H,
                    });
                }
            }
        });

        //sycn gristack with layout props
        const layoutIdsInProps = new Set(layouts.map((l) => l.id));
        const nodesToRemove = gridStackInstance.current.engine.nodes.filter(
            (n) => n.id && !layoutIdsInProps.has(n.id)
        );
        nodesToRemove.forEach((node) => {
            gridStackInstance.current?.removeWidget(node.el!, false, false);
        });
        //compact on change or load
        gridStackInstance.current.compact();
        gridStackInstance.current.batchUpdate(false);
    }, [layouts]);

    //lock layouts outside edit
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
        <div className="bg-background flex-1 min-h-full pb-40">
            <div ref={gridRef} className="grid-stack">
                {layouts.map((l) => (
                    <WidgetWrapper key={l.id} layout={l} isEditMode={isEditMode} />
                ))}
            </div>
        </div>
    );
});
