import { create } from "zustand";
import { LayoutItem, DashboardConfig, WidgetConfig } from "@/features/dashboard/types/widgets";

interface DashboardActions {
    createSnapshot: () => void;
    restoreSnapshot: () => void;
    clearSnapshot: () => void;
    setActiveDashboard: (id: string | null) => void;
    addDashboard: (dashboard: DashboardConfig) => void;
    removeDashboard: (id: string) => void;
    addWidget: (layout: LayoutItem, widget: WidgetConfig) => void;
    updateWidgetConfig: (widget: WidgetConfig) => void;
    removeWidget: (layoutId: string, widgetId: string) => void;
    updateLayouts: (newLayouts: LayoutItem[]) => void;
    setInitialState: (
        dashboards: Record<string, DashboardConfig>,
        layouts: LayoutItem[],
        widgets: WidgetConfig[]
    ) => void;
}

export interface DashboardStore {
    activeDashboardId: string | null;
    dashboards: Record<string, DashboardConfig>;
    layouts: Record<string, LayoutItem>;
    widgets: Record<string, WidgetConfig>;
    snapshot: {
        dashboards: Record<string, DashboardConfig>;
        layouts: Record<string, LayoutItem>;
        widgets: Record<string, WidgetConfig>;
    } | null;
    actions: DashboardActions;
}

export const useDashboardStore = create<DashboardStore>((set) => ({
    activeDashboardId: null,
    dashboards: {},
    layouts: {},
    widgets: {},
    snapshot: null,

    actions: {
        createSnapshot: () =>
            set((state) => ({
                snapshot: {
                    dashboards: JSON.parse(JSON.stringify(state.dashboards)),
                    layouts: JSON.parse(JSON.stringify(state.layouts)),
                    widgets: JSON.parse(JSON.stringify(state.widgets)),
                },
            })),

        restoreSnapshot: () =>
            set((state) => {
                if (!state.snapshot) return state;
                return {
                    dashboards: state.snapshot.dashboards,
                    layouts: state.snapshot.layouts,
                    widgets: state.snapshot.widgets,
                    snapshot: null,
                };
            }),

        clearSnapshot: () => set({ snapshot: null }),

        setActiveDashboard: (id) => set({ activeDashboardId: id }),
        addDashboard: (dashboard) =>
            set((state) => ({
                dashboards: {
                    ...state.dashboards,
                    [dashboard.id]: dashboard,
                },
                activeDashboardId: dashboard.id,
            })),
        removeDashboard: (id) =>
            set((state) => {
                const newDashboards = { ...state.dashboards };
                delete newDashboards[id];
                return {
                    dashboards: newDashboards,
                    activeDashboardId:
                        state.activeDashboardId === id ? null : state.activeDashboardId,
                };
            }),
        addWidget: (layout, widget) =>
            set((state) => {
                const activeDashboard = state.activeDashboardId
                    ? state.dashboards[state.activeDashboardId]
                    : undefined;
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
        updateWidgetConfig: (widget) =>
            set((state) => ({
                widgets: {
                    ...state.widgets,
                    [widget.id]: widget,
                },
            })),
        removeWidget: (layoutId, widgetId) =>
            set((state) => {
                const newLayouts = { ...state.layouts };
                delete newLayouts[layoutId];

                const newWidgets = { ...state.widgets };
                delete newWidgets[widgetId];

                const newDashboards = { ...state.dashboards };
                if (state.activeDashboardId && newDashboards[state.activeDashboardId]) {
                    newDashboards[state.activeDashboardId] = {
                        ...newDashboards[state.activeDashboardId],
                        layoutItemIds: newDashboards[state.activeDashboardId].layoutItemIds.filter(
                            (id) => id !== layoutId
                        ),
                    };
                }

                return {
                    layouts: newLayouts,
                    widgets: newWidgets,
                    dashboards: newDashboards,
                };
            }),

        updateLayouts: (newLayouts) =>
            set((state) => {
                const activeId = state.activeDashboardId;
                if (!activeId || !state.dashboards[activeId]) return state;
                const activeDashboard = state.dashboards[activeId];
                const newLayoutIds = newLayouts.map((l) => l.id);

                const updatedLayouts = { ...state.layouts };
                const updatedWidgets = { ...state.widgets };

                const idsToDelete = activeDashboard.layoutItemIds.filter(
                    (id) => !newLayoutIds.includes(id)
                );

                idsToDelete.forEach((id) => {
                    delete updatedLayouts[id];
                    delete updatedWidgets[id];
                });
                newLayouts.forEach((layout) => {
                    updatedLayouts[layout.id] = layout;
                });
                return {
                    layouts: updatedLayouts,
                    widgets: updatedWidgets,
                    dashboards: {
                        ...state.dashboards,
                        [activeId]: {
                            ...activeDashboard,
                            layoutItemIds: newLayoutIds,
                        },
                    },
                };
            }),

        setInitialState: (dashboards, layoutsArray, widgetsArray) => {
            const layoutsMap = layoutsArray.reduce<Record<string, LayoutItem>>((acc, item) => {
                acc[item.id] = item;
                return acc;
            }, {});

            const widgetsMap = widgetsArray.reduce<Record<string, WidgetConfig>>((acc, item) => {
                acc[item.id] = item;
                return acc;
            }, {});

            const activeDashboard = Object.values(dashboards).find((d) => d.current);

            set({
                dashboards: dashboards,
                layouts: layoutsMap,
                widgets: widgetsMap,
                activeDashboardId: activeDashboard ? activeDashboard.id : null,
            });
        },
    },
}));
