import { create } from "zustand";
import { WidgetConfig, LayoutItem } from "@/types/widgets";

export interface DashboardState {
    //layout and widgets are keyed by id
    layouts: Record<string, LayoutItem>;
    widgets: Record<string, WidgetConfig>; 
}

export interface DashboardActions {
    updateLayoutGeometry: (id: string, x: number, y: number, w: number, h: number) => void;
    updateLayouts: (newLayouts: LayoutItem[]) => void;
    updateWidgetConfig: (id: string, config: Partial<WidgetConfig>) => void;
    setInitialState: (layouts: LayoutItem[], widgets: WidgetConfig[]) => void;
    addWidget: (layout: LayoutItem, config: WidgetConfig) => void;
    removeWidget: (layoutId: string, widgetId: string) => void;
}

export type DashboardStore = DashboardState & { actions: DashboardActions };

export const useDashboardStore = create<DashboardStore>((set) => ({
    layouts: {},
    widgets: {},
    actions: {
        updateLayoutGeometry: (id, x, y, w, h) => set((state) => ({
            layouts: { ...state.layouts, [id]: { ...state.layouts[id], x, y, w, h } }
        })),
        updateLayouts: (newLayouts) => set((state) => ({
            layouts: {
                ...state.layouts,
                ...Object.fromEntries(newLayouts.map(l => [l.id, { ...state.layouts[l.id], ...l }]))
            }
        })),
        updateWidgetConfig: (id, config) => set((state) => ({
            widgets: { ...state.widgets, [id]: { ...state.widgets[id], ...config } }
        })),
        setInitialState: (layouts, widgets) => set({
            layouts: Object.fromEntries(layouts.map(l => [l.id, l])),
            widgets: Object.fromEntries(widgets.map(w => [w.id, w]))
        }),
        addWidget: (layout, config) => set((state) => ({
            layouts: { ...state.layouts, [layout.id]: layout },
            widgets: { ...state.widgets, [config.id]: config }
        })),
        removeWidget: (layoutId, widgetId) => set((state) => {
            const { [layoutId]: _, ...remainingLayouts } = state.layouts;
            const { [widgetId]: __, ...remainingWidgets } = state.widgets;
            return { layouts: remainingLayouts, widgets: remainingWidgets };
        }),
    }
}));