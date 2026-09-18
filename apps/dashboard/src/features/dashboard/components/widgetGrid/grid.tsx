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
import { useDashboardStore } from "../../stores/dashboard-store";

import { LayoutItem, WidgetConfig } from "@/features/dashboard/types/widgets";
import { WidgetWrapper } from "@/features/dashboard/components/widgetGrid/widgets/widgetWrapper";

const MIN_SIZES: Record<WidgetConfig["widgetType"], { w: number; h: number }> = {
    CHART: { w: 3, h: 3 },
    KPI: { w: 3, h: 2 },
};
const DEFAULT_MIN = { w: 3, h: 3 };

function getMinSize(widgetType?: WidgetConfig["widgetType"]) {
    return widgetType ? MIN_SIZES[widgetType] : DEFAULT_MIN;
}

const repairLayout = (
    fullLayout: LayoutItem[],
    widgetsMap: Record<string, WidgetConfig>
): LayoutItem[] =>
    fullLayout.map((l) => {
        const { w: minW, h: minH } = getMinSize(widgetsMap[l.id]?.widgetType);
        return {
            ...l,
            w: Number.isFinite(l.w) && l.w > 0 ? l.w : minW,
            h: Number.isFinite(l.h) && l.h > 0 ? l.h : minH,
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
    const hasSyncedOnce = useRef(false);
    const scrollRef = useRef<HTMLDivElement>(null);
    const setIsCompacting = useDashboardStore((state) => state.actions.setIsCompacting);
    const compactTimerRef = useRef<NodeJS.Timeout | null>(null);
    const layoutChangeTimerRef = useRef<NodeJS.Timeout | null>(null);
    const isInteractingRef = useRef(false);

    const cancelPendingCompact = () => {
        if (compactTimerRef.current) {
            clearTimeout(compactTimerRef.current);
            compactTimerRef.current = null;
        }
    };

    const scheduleCompact = (delayMs: number) => {
        cancelPendingCompact(); //reset if already running
        compactTimerRef.current = setTimeout(() => {
            if (isEditModeRef.current && !isInteractingRef.current && gridStackInstance.current) {
                gridStackInstance.current.batchUpdate();
                gridStackInstance.current.compact();
                gridStackInstance.current.batchUpdate(false);
            }
        }, delayMs);
    };

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

            const widgetsMap = useDashboardStore.getState().widgets;
            return repairLayout(fullLayout, widgetsMap);
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
                    animate: true, //better performance
                    minRow: 3,
                    resizable: { handles: "se" }, // part of library handles widget resizing from "south-east"/bottom-right corner

                    columnOpts: {
                        breakpointForWindow: true,
                        breakpoints: [{ w: 768, c: 1 }], // at 768px (standard mobile/tablet)
                    },
                },
                gridRef.current
            );

            gridStackInstance.current.on("dragstart resizestart", () => {
                isInteractingRef.current = true;
                cancelPendingCompact();
            });

            gridStackInstance.current.on("dragstop resizestop", () => {
                isInteractingRef.current = false;
                scheduleCompact(500);
            });

            gridStackInstance.current.on("change", () => {
                if (layoutChangeTimerRef.current) {
                    clearTimeout(layoutChangeTimerRef.current);
                }
                layoutChangeTimerRef.current = setTimeout(() => {
                    if (
                        gridStackInstance.current &&
                        isEditModeRef.current &&
                        !isInternalUpdate.current
                    ) {
                        isInternalUpdate.current = true;
                        const fullLayout = gridStackInstance.current.save(
                            false,
                            false,
                            (node, w: GridStackWidget) => {
                                (w as LayoutItem).id = String(node.id || "");
                            }
                        ) as LayoutItem[];

                        const widgetsMap = useDashboardStore.getState().widgets;
                        const repaired = repairLayout(fullLayout, widgetsMap);

                        onLayoutChangeRef.current(repaired);
                    }
                }, 500);
            });
        }

        return () => {
            cancelPendingCompact();
            if (layoutChangeTimerRef.current) clearTimeout(layoutChangeTimerRef.current);
            gridStackInstance.current?.destroy(false);
            gridStackInstance.current = null;
        };
    }, []);

    useEffect(() => {
        if (!gridRef.current) return;

        const resizeObserver = new ResizeObserver(() => {
            if (!isInteractingRef.current) {
                scheduleCompact(250);
            }
        });

        resizeObserver.observe(gridRef.current);

        return () => {
            resizeObserver.disconnect();
            cancelPendingCompact();
        };
    }, []);

    useEffect(() => {
        if (!gridStackInstance.current) return;

        if (isInternalUpdate.current) {
            isInternalUpdate.current = false;
            return;
        }

        const widgetsMap = useDashboardStore.getState().widgets;

        //batchupdate prevent multiple relayouts during sync
        gridStackInstance.current.batchUpdate();

        const currentGridNodes = new Map<string, GridStackNode>();
        gridStackInstance.current.engine.nodes.forEach((node) => {
            if (node.id) {
                // node.id is the layout.id (gs-id)
                currentGridNodes.set(node.id, node);
            }
        });

        let addedNewWidget = false;

        // add/update widgets based on layouts prop
        layouts.forEach((layoutItem) => {
            const { w: minW, h: minH } = getMinSize(widgetsMap[layoutItem.id]?.widgetType);
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
                        minW,
                        minH,
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
                        minW,
                        minH,
                    });
                    addedNewWidget = true;
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
        if (!isInteractingRef.current) {
            gridStackInstance.current.compact();
        }
        gridStackInstance.current.batchUpdate(false);

        if (hasSyncedOnce.current && addedNewWidget && !isEditModeRef.current) {
            setIsCompacting(true);

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

            isInternalUpdate.current = true;
            onLayoutChangeRef.current(repairLayout(fullLayout, widgetsMap));

            scrollRef.current?.scrollIntoView({ behavior: "smooth", block: "end" });

            setIsCompacting(false);
        }
        hasSyncedOnce.current = true;
    }, [layouts, setIsCompacting]);

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
            <div ref={scrollRef} aria-hidden className="h-px w-full" />
        </div>
    );
});
