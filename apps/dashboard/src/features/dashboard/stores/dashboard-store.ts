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
import { MetricType } from "@/features/dashboard/types/metric";
import { setDashboardPresetTimeWindow } from "../utils/setDashboardTimeWindow";
import { persist } from "zustand/middleware";
import { getPresetRange } from "../components/toolbar/timePeriodSelector";
import { timeMs } from "@/lib/timeUtils";
import { toast } from "sonner";
import type { AiVersionSummary, DashboardPlan } from "@/features/dashboard/types/agentic";
import {
    createAiSession,
    deleteAiSession,
    generateDashboardPlan,
    getAiDashboardVersions,
    activateAiDashboardVersion,
    applyAiDashboardVersion,
} from "@/lib/fetch/api-agentic-dashboard";
import type { DashboardDTO } from "@/lib/fetch/api-dashboard";
import type { AiDashboardApplyMode } from "@/features/dashboard/types/agentic";

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
    applyDashboard: (versionId: string, mode: AiDashboardApplyMode) => Promise<boolean>;
    cancelSession: () => Promise<void>;
}

export type AgenticSlice = {
    sessionId: string | null;
    startedDashboardId: string | null;
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
            const state = get();
            return state.isSessionActive ? state.stagedWidgets[id] : state.widgets[id];
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

function adaptFetchedDashboards(dashboards: DashboardDTO[]) {
    const dashboardsMap: Record<string, DashboardConfig> = {};
    const layoutsArray: LayoutItem[] = [];
    const widgetsArray: WidgetConfig[] = [];

    dashboards.forEach((dashboard) => {
        dashboardsMap[dashboard.id] = {
            id: dashboard.id,
            displayName: dashboard.displayName,
            timeFrom: dashboard.timeFrom,
            timeTo: dashboard.timeTo,
            predefinedTime: dashboard.predefinedTime,
            current: dashboard.current,
            layoutItemIds: dashboard.widgets.map((widget) => widget.id),
        };

        dashboard.widgets.forEach((widget) => {
            layoutsArray.push({
                id: widget.id,
                x: widget.startX,
                y: widget.startY,
                w: widget.width,
                h: widget.height,
                autoPosition: false,
            });

            if (widget.widgetType === "CHART") {
                widgetsArray.push({
                    id: widget.id,
                    chartType: widget.chartType as ChartType,
                    chartColour: widget.chartColour as ChartColour,
                    widgetType: "CHART",
                    displayName: widget.displayName,
                    provider: widget.provider,
                    accountId: widget.accountId,
                    resourceId: widget.resourceId,
                    metricType: widget.metricType as MetricType | null,
                    metricName: widget.metricName,
                });
            } else {
                widgetsArray.push({
                    id: widget.id,
                    widgetType: "KPI",
                    displayName: widget.displayName,
                    chargeIds: widget.chargeIds,
                    aggregationWindowDays: widget.aggregationWindowDays,
                });
            }
        });
    });

    return { dashboardsMap, layoutsArray, widgetsArray };
}

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
    startedDashboardId: null,
    currentVersionId: null,
    isSessionActive: false,
    isGenerating: false,
    assistantMessage: null,
    versions: [],
    stagedLayouts: {},
    stagedWidgets: {},

    agenticActions: {
        startSessionAndGenerate: async (prompt: string) => {
            const startedDashboardId = get().activeDashboardId;
            if (!startedDashboardId) {
                toast.error("Select a dashboard before starting an AI session.");
                return;
            }

            set({ isGenerating: true, startedDashboardId });

            let sessionId: string | null = null;

            try {
                const sessionRes = await createAiSession();
                sessionId = sessionRes.sessionId;

                const planRes = await generateDashboardPlan({
                    sessionId,
                    startingDashboardId: startedDashboardId,
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

                await get().agenticActions.fetchVersions();

                if (planRes.stageSucceeded) {
                    toast.success("AI session started and draft generated!");
                } else if (planRes.stageAttempted) {
                    toast.error(
                        planRes.assistantMessage ||
                            "The AI could not create the requested dashboard."
                    );
                }
            } catch (error) {
                console.error("Failed to start AI session:", error);

                if (sessionId) {
                    try {
                        await deleteAiSession(sessionId);
                    } catch (cleanupError) {
                        console.error("Failed to clean up AI session:", cleanupError);
                    }
                }

                set({
                    sessionId: null,
                    startedDashboardId: null,
                    currentVersionId: null,
                    isSessionActive: false,
                    isGenerating: false,
                    assistantMessage: null,
                    versions: [],
                    stagedLayouts: {},
                    stagedWidgets: {},
                });

                toast.error(
                    error instanceof Error ? error.message : "Failed to generate AI dashboard plan."
                );
            }
        },

        sendPrompt: async (prompt: string) => {
            const { sessionId, startedDashboardId } = get();
            if (!sessionId || !startedDashboardId) return;

            set({ isGenerating: true });

            try {
                const planRes = await generateDashboardPlan({
                    sessionId,
                    startingDashboardId: startedDashboardId,
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

                if (planRes.stageSucceeded) {
                    toast.success("Dashboard draft updated!");
                } else if (planRes.stageAttempted) {
                    toast.error(
                        planRes.assistantMessage ||
                            "The AI could not create the requested dashboard."
                    );
                }
            } catch (error) {
                console.error("Failed to update plan:", error);
                toast.error(
                    error instanceof Error ? error.message : "Failed to update AI dashboard draft."
                );
                set({ isGenerating: false });
            }
        },

        switchVersion: async (versionId: string) => {
            const { sessionId } = get();
            if (!sessionId) return;

            try {
                const versionRes = await activateAiDashboardVersion(sessionId, versionId);
                const { layoutsMap, widgetsMap } = adaptState(versionRes.dashboard);
                console.log(" Staged layout: ", layoutsMap);
                set({
                    currentVersionId: versionRes.versionId,
                    stagedLayouts: layoutsMap,
                    stagedWidgets: widgetsMap,
                });

                await get().agenticActions.fetchVersions();
                toast.success(
                    versionRes.version === 0
                        ? "Switched to the dashboard you started with."
                        : `Switched to version v${versionRes.version}`
                );
            } catch (error) {
                console.error("Failed to activate version:", error);
                toast.error(
                    error instanceof Error ? error.message : "Failed to load selected version."
                );
            }
        },

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

        applyDashboard: async (versionId: string, mode: AiDashboardApplyMode) => {
            const { sessionId, startedDashboardId } = get();
            if (!sessionId) return false;

            if (mode === "REPLACE_STARTED_DASHBOARD" && !startedDashboardId) {
                toast.error("The dashboard that started the AI session is no longer available.");
                return false;
            }

            try {
                const appliedDashboards = await applyAiDashboardVersion(sessionId, versionId, {
                    mode,
                    startedDashboardId:
                        mode === "REPLACE_STARTED_DASHBOARD" ? startedDashboardId! : undefined,
                });

                const { dashboardsMap, layoutsArray, widgetsArray } =
                    adaptFetchedDashboards(appliedDashboards);

                get().actions.setInitialState(dashboardsMap, layoutsArray, widgetsArray);

                set({
                    sessionId: null,
                    startedDashboardId: null,
                    currentVersionId: null,
                    isSessionActive: false,
                    isGenerating: false,
                    assistantMessage: null,
                    versions: [],
                    stagedLayouts: {},
                    stagedWidgets: {},
                });

                toast.success(
                    mode === "REPLACE_STARTED_DASHBOARD"
                        ? "The selected AI dashboard replaced your original dashboard."
                        : "The selected AI dashboard was created as a new dashboard."
                );
                return true;
            } catch (error) {
                console.error("Failed to apply dashboard version:", error);
                toast.error(
                    error instanceof Error ? error.message : "Failed to apply AI dashboard."
                );
                return false;
            }
        },

        cancelSession: async () => {
            const { sessionId } = get();
            if (sessionId) {
                try {
                    await deleteAiSession(sessionId);
                } catch (error) {
                    console.error("Failed to delete session on cancel:", error);
                }
            }

            set({
                sessionId: null,
                startedDashboardId: null,
                currentVersionId: null,
                isSessionActive: false,
                isGenerating: false,
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
