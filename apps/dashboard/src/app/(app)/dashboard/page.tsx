"use client";

import { useState, useCallback, useEffect, Suspense, useMemo } from "react";
import { DateRange } from "react-day-picker";
import { useRouter, useSearchParams } from "next/navigation";

import { Spinner } from "@/components/atoms/spinner";
import Grid from "@/features/dashboard/components/widgetGrid/grid";
import {
  LayoutItem,
  DashboardConfig,
  DashboardStub,
  WidgetConfig,
  ChartType,
} from "@/features/dashboard/types/widgets";
import { useDashboardStore, DashboardStore } from "@/features/dashboard/stores/dashboard-store";
import { useMetricStream } from "@/features/dashboard/services/sse/metric-stream";
import { useFetchMetrics } from "@/features/dashboard/hooks/useFetchMetrics";
import { useWindowStore } from "@/features/dashboard/stores/window-store";
import { useResourceNameStore } from "@/features/dashboard/stores/resource-store";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";

function DashboardContent() {
  const { error: streamError } = useMetricStream();
  const router = useRouter();
  const searchParams = useSearchParams();
  const urlId = searchParams.get("id");
  const [isLoading, setIsLoading] = useState(true);
  const [isEditMode, setIsEditMode] = useState(false);
  const [originalLayout, setOriginalLayout] = useState<LayoutItem[]>([]);
  const [originalConfigs, setOriginalConfigs] = useState<WidgetConfig[]>([]);
  const setWindow = useWindowStore((state) => state.setWindow);

  const dashboards = useDashboardStore((state: DashboardStore) => state.dashboards);
  const activeDashboardId = useDashboardStore((state: DashboardStore) => state.activeDashboardId);
  const layoutsMap = useDashboardStore((state: DashboardStore) => state.layouts);
  const widgetsMap = useDashboardStore((state: DashboardStore) => state.widgets);

  // Metrics and resource name stores
  const { metricFetchError, metricFetchLoad } = useFetchMetrics();
  const fetchResourceNames = useResourceNameStore((state) => state.fetchResources);
  const getMetricList = useMetricStore((state) => state.getMetricList);

  const { setInitialState, addWidget, removeWidget, updateLayouts, setActiveDashboard, addDashboard } =
    useDashboardStore((state: DashboardStore) => state.actions);

  const createDefaultWidgetConfig = useCallback(
    (id: string, title: string, chartType: ChartType): WidgetConfig => {
      const metricsByResource = getMetricList();
      const resourceId = Object.keys(metricsByResource)[0];

      return {
        id,
        title,
        chartType,
        resourceId,
        metricType: resourceId ? (metricsByResource[resourceId]?.[0] ?? "anon") : "anon",
      };
    },
    [getMetricList],
  );

  useEffect(() => {
    const loadDashboardData = async () => {
      // only initialize if we don't have any dashboards in the store yet make full use of zustand caching.
      setIsLoading(true);

      // Leverage fact that dashboards need to be loaded into mem to trigger initial metric fetch
      await fetchResourceNames();

      if (metricFetchLoad) {
        return;
      }

      if (metricFetchError) {
        setIsLoading(false);
        return;
      }

      if (Object.keys(dashboards).length === 0) {
        // Some info that helped me whilst debugging, this is so that there are mock widgets
        // once the dashboard loads
        const initialConfigs: WidgetConfig[] = [
          createDefaultWidgetConfig("w-1", "Live CPU Usage (Mock)", "line"),
          createDefaultWidgetConfig("w-2", "Live Memory (Mock)", "gauge"),
        ];
        const initialLayouts: LayoutItem[] = [
          { id: "l-1", widgetId: "w-1", x: 0, y: 0, w: 6, h: 4 },
          { id: "l-2", widgetId: "w-2", x: 4, y: 0, w: 6, h: 4 },
        ];
        const initialDashboards: Record<string, DashboardConfig> = {
          "ds-1": { id: "ds-1", name: "Global Cost Overview", layoutItemIds: ["l-1", "l-2"] },
          "ds-2": { id: "ds-2", name: "AWS Production Metrics", layoutItemIds: [] },
          "ds-3": { id: "ds-3", name: "Azure Spending Forecast", layoutItemIds: [] },
        };
        setInitialState(initialDashboards, initialLayouts, initialConfigs);

        const dashboardIds = Object.keys(initialDashboards);
        if (urlId && initialDashboards[urlId]) {
          setActiveDashboard(urlId);
        } else if (dashboardIds.length > 0) {
          const defaultId = dashboardIds[0];
          setActiveDashboard(defaultId);
          router.replace(`?id=${defaultId}`);
        }
        setIsLoading(false);
      } else {
        // if we already have data, just ensure not stuck in loading state
        setIsLoading(false);
      }
    };

    //note: reactMemo is an option for components that re-render a lot (apparently)

    loadDashboardData();
  }, [
    setInitialState,
    setActiveDashboard,
    router,
    dashboards,
    fetchResourceNames,
    urlId,
    createDefaultWidgetConfig,
    metricFetchLoad,
    metricFetchError,
  ]);

  // sync Zustand store when the URL changes (i.e browser back/forward buttons)
  useEffect(() => {
    if (urlId && dashboards[urlId] && urlId !== activeDashboardId) {
      setActiveDashboard(urlId);
    }
  }, [urlId, dashboards, activeDashboardId, setActiveDashboard]);

  const handleDashboardChange = useCallback(
    (id: string) => {
      router.push(`?id=${id}`);
    },
    [router],
  );

  // computes the layouts array for the active dashboard
  const activeDashboard = activeDashboardId ? dashboards[activeDashboardId] : undefined;
  const widgetLayouts = useMemo(() => {
    return (
      activeDashboard?.layoutItemIds?.map((id: string) => layoutsMap[id]).filter((l): l is LayoutItem => !!l) ?? []
    );
  }, [activeDashboard, layoutsMap]);

  // maps normalized dashboards back to stub format for thetoollbar dropdown
  const dashboardStubs: DashboardStub[] = Object.values(dashboards).map((d: DashboardConfig) => ({
    id: d.id,
    label: d.name,
  }));

  const handleStartEditing = useCallback(() => {
    setOriginalLayout(widgetLayouts.map((l) => ({ ...l })));
    setOriginalConfigs(Object.values(widgetsMap).map((c) => ({ ...c })));
    setIsEditMode(true);
  }, [widgetLayouts, widgetsMap]);

  const handleSaveEdit = useCallback(() => {
    console.log(`Saving layout for ${activeDashboardId}...`, layoutsMap);
    setIsEditMode(false);
  }, [layoutsMap, activeDashboardId]);

  const handleCancelEdit = useCallback(() => {
    // Pass revert to original staate screenshort before edit was activated
    setInitialState(dashboards, originalLayout, originalConfigs);
    setIsEditMode(false);
  }, [originalLayout, originalConfigs, dashboards, setInitialState]);

  const handleCreateDashboard = useCallback(
    (name: string) => {
      const newId = crypto.randomUUID();
      const newDashboard: DashboardConfig = {
        id: newId,
        name: name,
        layoutItemIds: [],
      };
      addDashboard(newDashboard);
      router.push(`?id=${newId}`); 
    },
    [addDashboard, router],
  );

  const handleAddWidget = useCallback(() => {
    const widgetId = crypto.randomUUID();
    const layoutId = crypto.randomUUID();
    const newConfig = createDefaultWidgetConfig(widgetId, "New Widget (Click to Customize)", "line");
    const newLayout: LayoutItem = { id: layoutId, widgetId, x: 0, y: 0, w: 6, h: 4, autoPosition: true };

    addWidget(newLayout, newConfig);
    setIsEditMode(true);
  }, [addWidget, createDefaultWidgetConfig]);

  const handleDeleteWidget = useCallback(
    (layoutId: string, widgetId: string) => {
      removeWidget(layoutId, widgetId);
    },
    [removeWidget],
  );

  const handleLayoutChange = useCallback(
    (newLayout: LayoutItem[]) => {
      updateLayouts(newLayout);
    },
    [updateLayouts],
  );

  const handleDateRangeChange = useCallback(
    (range: DateRange | undefined) => {
      if (range?.from && range?.to) {
        setWindow(range.from, range.to);
      }
    },
    [setWindow],
  );
  const renderMainContent = () => {
    if (isLoading) {
      return (
        <div className="flex-1 flex items-center justify-center">
          <Spinner className="size-8" />
        </div>
      );
    }

    if (metricFetchError) {
      return (
        <div className="flex-1 flex flex-col items-center justify-center text-center p-10">
          <h2 className="text-xl font-semibold mb-2">Unable to Load Metrics</h2>
          <p className="text-muted-foreground mb-6">Widgets are paused until historical metrics are available.</p>
        </div>
      );
    }

    if (activeDashboard) {
      return (
        <Grid
          isEditMode={isEditMode}
          dashboardId={activeDashboardId || ""}
          onLayoutChange={handleLayoutChange}
          layouts={widgetLayouts}
          onDeleteWidget={handleDeleteWidget}
          metricFetchLoad={metricFetchLoad}
        />
      );
    }

    return (
      <div className="flex-1 flex flex-col items-center justify-center text-center p-10">
        <h2 className="text-xl font-semibold mb-2">No Dashboards Found</h2>
        <p className="text-muted-foreground mb-6">
          Create your first dashboard to start monitoring your cloud resources.
        </p>
      </div>
    );
  };

  return (
    <>
      {streamError && (
        <div className="mx-6 mt-4 p-3 bg-destructive/10 border border-destructive/80 rounded-md text-destructive text-xs">
          Stream Error: {streamError.message}. Real-time updates may be paused.
        </div>
      )}

      {metricFetchError && (
        <div className="mx-6 mt-4 p-3 bg-destructive/10 border border-destructive/80 rounded-md text-destructive text-xs">
          Metric Error: {metricFetchError.message}. Widgets will render once historical metrics are available.
        </div>
      )}

      <main className="flex-1 overflow-y-auto overflow-x-hidden m-3 flex flex-col" data-testid="dashboard">
        {renderMainContent()}
      </main>
    </>
  );
}

// this part of the page depends on runtime info (like searchparams) that isn't available during the static build.
// still prerender the static parts of your dashboard
// fixes lighthouse issues hopefully
export default function DashboardPage() {
  return (
    <Suspense
      fallback={
        <div className="flex-1 flex items-center justify-center text-muted-foreground animate-pulse">
          Loading dashboard...
        </div>
      }>
      <DashboardContent />
    </Suspense>
  );
}
