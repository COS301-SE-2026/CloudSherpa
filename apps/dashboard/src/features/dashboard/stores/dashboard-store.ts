import { create, StateCreator } from "zustand";
import {
    LayoutItem,
    DashboardConfig,
    WidgetConfig,
    ChartWidgetConfig,
    KpiWidgetConfig,
    ChartType,
    ChartColour,
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
import { timeMs } from "@/lib/timeUtils";
import { toast } from "sonner";
import {
    AiVersionSummary,
    DashboardPlan,
    DashboardPlanWidget,
} from "@/features/dashboard/types/agentic";
import {
    createAiSession,
    deleteAiSession,
    generateDashboardPlan,
    getAiDashboardVersions,
    getAiDashboardVersion,
    applyAiDashboardVersion,
} from "@/lib/fetch/api-agentic-dashboard";
import { MetricType } from "../types/metric";
import { CloudProviderEnum } from "@/features/dashboard/types/provider";

const tickIntervalMs = 60_000;

function getDefaultWindow() {
    const toMs = Date.now();

    return {
        fromMs: toMs - 7 * timeMs.dayMs,
        toMs,
        selectedPreset: "T_7_DAYS" as TimeWindowPreset,
    };
}

export interface AgenticActions {
    startSessionAndGenerate: (prompt: string) => Promise<void>;
    sendPrompt: (prompt: string) => Promise<void>;
    switchVersion: (versionId: string) => Promise<void>;
    fetchVersions: () => Promise<void>;
    acceptDashboard: () => Promise<void>;
    cancelSession: () => Promise<void>;
}

export type AgenticSlice = {
    sessionId: string | null;
    currentVersionId: string | null;
    isSessionActive: boolean;
    isGenerating: boolean;
    assistantMessage: string | null;
    versions: AiVersionSummary[];
    stagedLayouts: Record<string, LayoutItem>;
    stagedWidgets: Record<string, WidgetConfig>;
    agenticActions: AgenticActions;
};

interface DashboardActions {
    createSnapshot: () => void;
    restoreSnapshot: () => void;
    clearSnapshot: () => void;
    setActiveDashboard: (id: string | null) => void;
    addDashboard: (dashboard: DashboardConfig) => void;
    removeDashboard: (id: string) => void;
    addWidget: (layout: LayoutItem, widget: WidgetConfig) => void;
    getWidget: (id: string) => WidgetConfig | undefined;
    updateChartWidgetConfig: (widget: ChartWidgetConfig) => Promise<void>;
    updateKpiWidgetConfig: (widget: KpiWidgetConfig) => void;
    removeWidget: (layoutId: string, widgetId: string) => void;
    updateLayouts: (newLayouts: LayoutItem[]) => void;
    setInitialState: (
        dashboards: Record<string, DashboardConfig>,
        layouts: LayoutItem[],
        widgets: WidgetConfig[]
    ) => void;
    getDashboardNameByID: (id: string) => string | undefined;
    getWidgetNameById: (id: string) => string | undefined;
    setIsCompacting: (value: boolean) => void;
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
    isCompacting: boolean;
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

export type DashboardStore = DashboardSlice & WindowSlice & AgenticSlice;

const createDashboardSlice: StateCreator<DashboardStore, [], [], DashboardSlice> = (set, get) => ({
    activeDashboardId: null,
    dashboards: {},
    layouts: {},
    widgets: {},
    snapshot: null,
    isCompacting: false,

    actions: {
        setIsCompacting: (value) => set({ isCompacting: value }),

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
            const dashboardName = get().actions.getDashboardNameByID(id) ?? "No name";
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
                toast.success(`Successfully deleted ${dashboardName} dashboard.`);
            } catch (e) {
                console.error("Failed to delete dashboard:", e);
                toast.error(`Successfully deleted ${dashboardName} dashboard.`);
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
            const widgetName = get().actions.getWidgetNameById(widget.id) ?? "No name";
            try {
                await updateChartWidgetConfig(widget.id, {
                    id: widget.id,
                    widgetType: "CHART",
                    chartType: widget.chartType,
                    chartColour: widget.chartColour,
                    displayName: widget.displayName,
                    provider: widget.provider,
                    accountId: widget.accountId,
                    resourceId: widget.resourceId,
                    metricType: widget.metricType as string,
                    metricName: widget.metricName as string,
                });

                set((state) => ({
                    widgets: {
                        ...state.widgets,
                        [widget.id]: widget,
                    },
                }));
                toast.success(`Successfully updated ${widgetName} widget configuration.`);
            } catch (error) {
                console.error("Failed to persist widget config:", error);
                toast.error(`Failed to update ${widgetName} widget configuration.`);
                throw error;
            }
        },
        updateKpiWidgetConfig: async (widget) => {
            const widgetName = get().actions.getWidgetNameById(widget.id) ?? "No name";
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
                toast.success(`Successfully updated ${widgetName} widget configuration.`);
            } catch (error) {
                toast.error(`Failed to update ${widgetName} widget configuration.`);
                throw error;
            }
        },
        removeWidget: async (layoutId, widgetId) => {
            const widgetName = get().actions.getWidgetNameById(widgetId) ?? "No name";
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
                toast.success(`Successfully deleted ${widgetName} widget.`);
            } catch (error) {
                console.error("Failed to remove widget");
                toast.error(`Failed to delete ${widgetName} widget.`);
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

        getDashboardNameByID: (id) => {
            return get().dashboards[id]?.displayName ?? undefined;
        },

        getWidgetNameById: (id) => {
            return get().widgets[id]?.displayName ?? undefined;
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

function adaptState(dashboardPlan: DashboardPlan) {
    const layoutsMap: Record<string, LayoutItem> = {};
    const widgetsMap: Record<string, WidgetConfig> = {};

    dashboardPlan.widgets.forEach((w) => {
        const layoutId = w.widgetId;

        layoutsMap[layoutId] = {
            id: layoutId,
            x: w.startX,
            y: w.startY,
            w: w.width,
            h: w.height,
            autoPosition: false,
        };

        if (w.widgetType === "CHART") {
            widgetsMap[w.widgetId] = {
                id: w.widgetId,
                widgetType: "CHART",
                displayName: w.displayName || "AI Chart Widget",
                chartType: (w.chartType as ChartType) || "line_chart",
                chartColour: (w.chartColour as ChartColour) || "chart_1",
                provider: w.provider || null,
                accountId: w.accountId || null,
                resourceId: w.resourceId || null,
                metricName: w.metricName || null,
                metricType: null,
            };
        } else if (w.widgetType === "KPI") {
            widgetsMap[w.widgetId] = {
                id: w.widgetId,
                widgetType: "KPI",
                displayName: w.displayName || "AI KPI Widget",
                aggregationWindowDays: w.aggregationWindowDays || 7,
                chargeIds: w.chargeIds || [],
            };
        }
    });

    return { layoutsMap, widgetsMap };
}

const createAgenticSlice: StateCreator<DashboardStore, [], [], AgenticSlice> = (set, get) => ({
    sessionId: null,
    currentVersionId: null,
    isSessionActive: false,
    isGenerating: false,
    assistantMessage: null,
    versions: [],
    stagedLayouts: {},
    stagedWidgets: {},

    agenticActions: {
        //create new temp session, send first prompt, translate response and populate store so dash can be viewed
        startSessionAndGenerate: async (prompt: string) => {
            set({ isGenerating: true });
            try {
                //create session
                const sessionRes = await createAiSession();
                const sessionId = sessionRes.sessionId;

                //send initial prompt
                const planRes = await generateDashboardPlan({
                    sessionId,
                    message: prompt,
                });

                const { layoutsMap, widgetsMap } = adaptState(planRes.dashboard);

                set({
                    sessionId,
                    currentVersionId: planRes.versionId,
                    isSessionActive: true,
                    isGenerating: false,
                    assistantMessage: planRes.assistantMessage,
                    stagedLayouts: layoutsMap,
                    stagedWidgets: widgetsMap,
                });

                //refresh version history list
                await get().agenticActions.fetchVersions();
                toast.success("AI session started and draft generated!");
            } catch (error) {
                console.error("Failed to start session:", error);
                toast.error("Failed to generate AI dashboard plan.");
                set({ isGenerating: false });
            }
        },

        // while in session, send prompt to update current generated dash
        sendPrompt: async (prompt: string) => {
            const { sessionId } = get();
            if (!sessionId) return;

            set({ isGenerating: true });
            try {
                const planRes = await generateDashboardPlan({
                    sessionId,
                    message: prompt,
                });

                const { layoutsMap, widgetsMap } = adaptState(planRes.dashboard);

                set({
                    currentVersionId: planRes.versionId,
                    isGenerating: false,
                    assistantMessage: planRes.assistantMessage,
                    stagedLayouts: layoutsMap,
                    stagedWidgets: widgetsMap,
                });

                await get().agenticActions.fetchVersions();
                toast.success("Dashboard draft updated!");
            } catch (error) {
                console.error("Failed to update plan:", error);
                toast.error("Failed to update AI dashboard draft.");
                set({ isGenerating: false });
            }
        },

        //jmp 2 previous version, swap it into preview
        switchVersion: async (versionId: string) => {
            const { sessionId } = get();
            if (!sessionId) return;

            try {
                const versionRes = await getAiDashboardVersion(sessionId, versionId);
                const { layoutsMap, widgetsMap } = adaptState(versionRes.dashboard);

                set({
                    currentVersionId: versionRes.versionId,
                    stagedLayouts: layoutsMap,
                    stagedWidgets: widgetsMap,
                });
                toast.success(`Switched to version v${versionRes.version}`);
            } catch (error) {
                console.error("Failed to fetch version:", error);
                toast.error("Failed to load selected version.");
            }
        },

        //retrieve list of dash drafts during session for version history
        fetchVersions: async () => {
            const { sessionId } = get();
            if (!sessionId) return;

            try {
                const versions = await getAiDashboardVersions(sessionId);
                set({ versions });
            } catch (error) {
                console.error("Failed to fetch version history:", error);
            }
        },

        //persist current ai dash and cleanup state
        acceptDashboard: async () => {
            const { sessionId, currentVersionId } = get();
            if (!sessionId || !currentVersionId) return;

            try {
                //apply version
                await applyAiDashboardVersion(sessionId, currentVersionId);

                //clean up session
                await deleteAiSession(sessionId);

                //reset local
                set({
                    sessionId: null,
                    currentVersionId: null,
                    isSessionActive: false,
                    assistantMessage: null,
                    versions: [],
                    stagedLayouts: {},
                    stagedWidgets: {},
                });

                toast.success("Successfully applied AI dashboard!");
            } catch (error) {
                console.error("Failed to apply dashboard version:", error);
                toast.error("Failed to apply AI dashboard.");
            }
        },

        //abort session and cleanup state
        cancelSession: async () => {
            const { sessionId } = get();
            if (sessionId) {
                try {
                    await deleteAiSession(sessionId);
                } catch (error) {
                    console.error("Failed to delete session on cancel:", error);
                }
            }

            //reset local
            set({
                sessionId: null,
                currentVersionId: null,
                isSessionActive: false,
                assistantMessage: null,
                versions: [],
                stagedLayouts: {},
                stagedWidgets: {},
            });

            toast.info("AI session discarded.");
        },
    },
});

// Wrapping in persist instructs zustand to persist the fields specified in the partialize object to local storage
export const useDashboardStore = create<DashboardStore>()(
    persist(
        (...args) => ({
            ...createDashboardSlice(...args),
            ...createWindowSlice(...args),
            ...createAgenticSlice(...args),
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
