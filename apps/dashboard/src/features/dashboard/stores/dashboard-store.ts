import { create, StateCreator } from "zustand";
import {
    LayoutItem,
    DashboardConfig,
    WidgetConfig,
    ChartWidgetConfig,
    KpiWidgetConfig,
} from "@/features/dashboard/types/widgets";
import {
    deleteWidget,
    updateChartWidgetConfig,
    deleteDashboard,
    updateKpiWidgetConfig,
} from "@/lib/fetch/api-dashboard";
import { TimeWindowPreset } from "../types/timewindow";
import { setDashboardPresetTimeWindow } from "../utils/setDashboardTimeWindow";
import { persist } from "zustand/middleware";
import { getPresetRange } from "../components/toolbar/timePeriodSelector";
import { DateRange } from "react-day-picker";

const tickIntervalMs = 60_000;

function getDefaultWindow() {
    const toMs = Date.now();

    return {
        fromMs: toMs - 7 * 24 * 60 * 60 * 1000,
        toMs,
        selectedPreset: "T_7_DAYS" as TimeWindowPreset,
    };
}

interface DashboardActions {
    createSnapshot: () => void;
    restoreSnapshot: () => void;
    clearSnapshot: () => void;
    setActiveDashboard: (id: string | null) => void;
    addDashboard: (dashboard: DashboardConfig) => void;
    removeDashboard: (id: string) => void;
    addWidget: (layout: LayoutItem, widget: WidgetConfig) => void;
    getWidget: (id: string) => WidgetConfig | undefined;
    updateChartWidgetConfig: (widget: ChartWidgetConfig) => void;
    updateKpiWidgetConfig: (widget: KpiWidgetConfig) => void;
    removeWidget: (layoutId: string, widgetId: string) => void;
    updateLayouts: (newLayouts: LayoutItem[]) => void;
    setInitialState: (
        dashboards: Record<string, DashboardConfig>,
        layouts: LayoutItem[],
        widgets: WidgetConfig[]
    ) => void;
    reset: () => void;
}

export type DashboardSnapshot = {
    dashboards: Record<string, DashboardConfig>;
    layouts: Record<string, LayoutItem>;
    widgets: Record<string, WidgetConfig>;
};

type DashboardSlice = {
    activeDashboardId: string | null;
    dashboards: Record<string, DashboardConfig>;
    layouts: Record<string, LayoutItem>;
    widgets: Record<string, WidgetConfig>;
    snapshot: DashboardSnapshot | null;
    actions: DashboardActions;
};

type WindowSlice = {
    fromMs: number;
    toMs: number;
    selectedPreset: TimeWindowPreset;
    minutes?: number;
    hours?: number;
    days?: number;
    setWindow: (from: Date, to: Date) => void;
    setPreset: (preset: TimeWindowPreset) => void;
    hydrateWindowOnDashboardLoad: (preset: TimeWindowPreset) => void;
    timeoutId?: ReturnType<typeof setTimeout>;
    intervalId?: ReturnType<typeof setInterval>;
    clear: () => void;
};

export type DashboardStore = DashboardSlice & WindowSlice;

const createDashboardSlice: StateCreator<DashboardStore, [], [], DashboardSlice> = (set, get) => ({
    activeDashboardId: null,
    dashboards: {},
    layouts: {},
    widgets: {},
    snapshot: null,

    actions: {
        createSnapshot: () =>
            set((state) => {
                const cleanLayouts: Record<string, LayoutItem> = {};
                Object.values(state.layouts).forEach((l) => {
                    cleanLayouts[l.id] = {
                        id: l.id,
                        x: l.x,
                        y: l.y,
                        w: l.w,
                        h: l.h,
                        autoPosition: l.autoPosition,
                    };
                });

                return {
                    snapshot: {
                        dashboards: structuredClone(state.dashboards),
                        layouts: cleanLayouts,
                        widgets: structuredClone(state.widgets),
                    },
                };
            }),

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
        removeDashboard: async (id) => {
            try {
                await deleteDashboard(id);
                set((state) => {
                    const newDashboards = { ...state.dashboards };
                    delete newDashboards[id];
                    return {
                        dashboards: newDashboards,
                        activeDashboardId:
                            state.activeDashboardId === id ? null : state.activeDashboardId,
                    };
                });
            } catch (e) {
                console.error("Failed to delete dashboard:", e);
            }
        },
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
        updateChartWidgetConfig: async (widget) => {
            try {
                await updateChartWidgetConfig(widget.id, {
                    id: widget.id,
                    widgetType: "CHART",
                    chartType: widget.chartType,
                    displayName: widget.displayName,
                    resourceId: widget.resourceId,
                    metricType: widget.metricType as string,
                });

                set((state) => ({
                    widgets: {
                        ...state.widgets,
                        [widget.id]: widget,
                    },
                }));
            } catch (error) {
                console.error("Failed to persist widget config:", error);
                throw error;
            }
        },
        updateKpiWidgetConfig: async (widget) => {
            try {
                await updateKpiWidgetConfig(widget.id, {
                    id: widget.id,
                    displayName: widget.displayName,
                    widgetType: "KPI",
                    aggregationWindowDays: widget.aggregationWindowDays,
                    chargeIds: widget.chargeIds,
                });

                set((state) => ({
                    widgets: {
                        ...state.widgets,
                        [widget.id]: widget,
                    },
                }));
            } catch (error) {
                console.log("Failed to persist kpi widget config: ", error);
                throw error;
            }
        },
        removeWidget: async (layoutId, widgetId) => {
            try {
                await deleteWidget(widgetId);
                set((state) => {
                    const newLayouts = { ...state.layouts };
                    delete newLayouts[layoutId];

                    const newWidgets = { ...state.widgets };
                    delete newWidgets[widgetId];

                    const newDashboards = { ...state.dashboards };
                    if (state.activeDashboardId && newDashboards[state.activeDashboardId]) {
                        newDashboards[state.activeDashboardId] = {
                            ...newDashboards[state.activeDashboardId],
                            layoutItemIds: newDashboards[
                                state.activeDashboardId
                            ].layoutItemIds.filter((id) => id !== layoutId),
                        };
                    }

                    return {
                        layouts: newLayouts,
                        widgets: newWidgets,
                        dashboards: newDashboards,
                    };
                });
            } catch (error) {
                console.error("Failed to remove widget");
                throw error;
            }
        },

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

        getWidget: (id) => {
            const currentWidgets = get().widgets;

            return currentWidgets[id];
        },

        reset: () => {
            set({
                activeDashboardId: null,
                dashboards: {},
                layouts: {},
                widgets: {},
                snapshot: null,
            });
        },
    },
});

const createWindowSlice: StateCreator<DashboardStore, [], [], WindowSlice> = (set, get) => ({
    ...getDefaultWindow(),
    setWindow: (from, to) => {
        set({ fromMs: from.getTime(), toMs: to.getTime() });

        clearTimeout(get().timeoutId ?? undefined);
        clearInterval(get().intervalId ?? undefined);

        if (get().selectedPreset == "custom") {
            return;
        }

        const timeoutId = setTimeout(() => {
            const intervalId = setInterval(() => {
                set({
                    fromMs: get().fromMs + tickIntervalMs,
                    toMs: get().toMs + tickIntervalMs,
                });
            }, tickIntervalMs);

            set({ intervalId: intervalId });
        }, tickIntervalMs);

        set({ timeoutId: timeoutId });
    },
    setPreset: async (preset) => {
        set({ selectedPreset: preset });
        await setDashboardPresetTimeWindow(preset, get().activeDashboardId);
    },
    hydrateWindowOnDashboardLoad: (preset: TimeWindowPreset) => {
        // Uses the default for now
        if (preset == "custom") {
            return;
        }

        const presetRange = getPresetRange(preset) ?? getPresetRange("T_1_HOUR");

        if (!presetRange?.from || !presetRange?.to) {
            return;
        }
        set({ selectedPreset: preset });
        get().setWindow(presetRange.from, presetRange.to);
    },
    clear: () => {
        clearTimeout(get().timeoutId ?? undefined);
        clearInterval(get().intervalId ?? undefined);

        set({
            ...getDefaultWindow(),
            timeoutId: undefined,
            intervalId: undefined,
        });
    },
});

// Wrapping in persist instructs zustand to persist the fields specified in the partialize object to local storage
export const useDashboardStore = create<DashboardStore>()(
    persist(
        (...args) => ({
            ...createDashboardSlice(...args),
            ...createWindowSlice(...args),
        }),
        {
            name: "dashboard-store",
            partialize: (state) => ({
                fromMs: state.fromMs,
                toMs: state.toMs,
                selectedPreset: state.selectedPreset,
            }),
        }
    )
);
