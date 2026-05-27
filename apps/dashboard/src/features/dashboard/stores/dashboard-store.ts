import { create } from 'zustand';
import { LayoutItem, DashboardConfig } from '@/features/dashboard/types/widgets';
import { WidgetConfigData } from '@/features/dashboard/components/widgetGrid/widgets/widgetConfig';

interface DashboardActions {
    setActiveDashboard: (id: string | null) => void;
    addDashboard: (dashboard: DashboardConfig) => void;
    removeDashboard: (id: string) => void;
    addWidget: (layout: LayoutItem, widget: WidgetConfigData) => void;
    updateWidgetConfig: (widget: WidgetConfigData) => void;
    removeWidget: (layoutId: string, widgetId: string) => void;
    updateLayouts: (newLayouts: LayoutItem[]) => void;
    setInitialState: (
        dashboards: Record<string, DashboardConfig>,
        layouts: LayoutItem[],
        widgets: WidgetConfigData[]
    ) => void;
}

export interface DashboardStore {
    activeDashboardId: string | null;
    dashboards: Record<string, DashboardConfig>;
    layouts: Record<string, LayoutItem>;
    widgets: Record<string, WidgetConfigData>;
    actions: DashboardActions;
}

export const useDashboardStore = create<DashboardStore>((set, get) => ({
    activeDashboardId: null,
    dashboards: {},
    layouts: {},
    widgets: {},

    actions: {
        setActiveDashboard: (id) => set({ activeDashboardId: id }),
        addDashboard: (dashboard) => set((state) => ({
            dashboards: {
                ...state.dashboards,
                [dashboard.id]: dashboard,
            },
            activeDashboardId: dashboard.id, // optionally set active
        })),
        removeDashboard: (id) => set((state) => {
            const newDashboards = { ...state.dashboards };
            delete newDashboards[id];
            return { dashboards: newDashboards };
        }),
        addWidget: (layout, widget) => set((state) => {
            const activeDashboard = state.activeDashboardId ? state.dashboards[state.activeDashboardId] : undefined;
            if (!activeDashboard) return state; 

            return {
                layouts: {
                    ...state.layouts,
                    [layout.id]: layout,
                },
                widgets: {
                    ...state.widgets,
                    [widget.id]: widget,
                },
                dashboards: {
                    ...state.dashboards,
                    [activeDashboard.id]: {
                        ...activeDashboard,
                        layoutItemIds: [...activeDashboard.layoutItemIds, layout.id],
                    },
                },
            };
        }),
        updateWidgetConfig: (widget) => set((state) => ({
            widgets: {
                ...state.widgets,
                [widget.id]: widget,
            },
        })),
        removeWidget: (layoutId, widgetId) => set((state) => {
            const newLayouts = { ...state.layouts };
            delete newLayouts[layoutId];

            const newWidgets = { ...state.widgets };
            delete newWidgets[widgetId];

            const newDashboards = { ...state.dashboards };
            if (state.activeDashboardId && newDashboards[state.activeDashboardId]) {
                newDashboards[state.activeDashboardId] = {
                    ...newDashboards[state.activeDashboardId],
                    layoutItemIds: newDashboards[state.activeDashboardId].layoutItemIds.filter(id => id !== layoutId),
                };
            }

            return {
                layouts: newLayouts,
                widgets: newWidgets,
                dashboards: newDashboards,
            };
        }),
        updateLayouts: (newLayouts) => set((state) => {
            const updatedLayouts = { ...state.layouts };
            newLayouts.forEach(layout => {
                updatedLayouts[layout.id] = layout;
            });
            return { layouts: updatedLayouts };
        }),
        setInitialState: (dashboards, layoutsArray, widgetsArray) => {
            const layoutsMap = layoutsArray.reduce((acc, item) => {
                acc[item.id] = item;
                return acc;
            }, {} as Record<string, LayoutItem>);
            const widgetsMap = widgetsArray.reduce((acc, item) => {
                acc[item.id] = item;
                return acc;
            }, {} as Record<string, WidgetConfigData>);
            set({
                dashboards: dashboards,
                layouts: layoutsMap,
                widgets: widgetsMap,
            });
        },
    },
}));