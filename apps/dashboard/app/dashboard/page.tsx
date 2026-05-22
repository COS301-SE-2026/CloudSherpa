"use client";

import { useState, useCallback, useEffect, Suspense, useMemo } from "react";
import { DateRange } from "react-day-picker";
import { useRouter, useSearchParams } from "next/navigation";

import Toolbar from "@/components/dashboard/toolbar";
import Grid from "@/components/dashboard/grid";
import { LayoutItem, DashboardConfig, DashboardStub } from "@/types/widgets";
import { useDashboardStore, DashboardStore } from "@/stores/dashboard-store";
import { useMetricStream } from "@/services/sse/metric-stream";
import { useFetchMetrics } from "@/hooks/useFetchMetrics";
import { useWindowStore } from "@/stores/window-store";
import { WidgetConfig, WidgetConfigData } from "@/components/widgets/widgetConfig";
import { useResourceNameStore } from "@/stores/resource-store";
import { useMetricStore } from "@/stores/metric-store";
import { MetricType } from "@/types/metric";
import { Button } from "@/components/atoms/button";

function DashboardContent() {
  const { error: streamError } = useMetricStream();
  const router = useRouter();
  const searchParams = useSearchParams();
  const urlId = searchParams.get("id");
  const [isLoading, setIsLoading] = useState(true);
  const [isEditMode, setIsEditMode] = useState(false);
  const [originalLayout, setOriginalLayout] = useState<LayoutItem[]>([]);
  const [originalConfigs, setOriginalConfigs] = useState<WidgetConfigData[]>([]);
  const [isConfigModalOpen, setIsConfigModalOpen] = useState(false);

  const fromMs = useWindowStore((state) => state.fromMs);
  const toMs = useWindowStore((state) => state.toMs);
  const from = new Date(fromMs);
  const to = new Date(toMs);
  const setWindow = useWindowStore((state) => state.setWindow);
  const dateRange: DateRange = { from, to };

  const dashboards = useDashboardStore((state: DashboardStore) => state.dashboards);
  const activeDashboardId = useDashboardStore((state: DashboardStore) => state.activeDashboardId);
  const layoutsMap = useDashboardStore((state: DashboardStore) => state.layouts);
  const widgetsMap = useDashboardStore((state: DashboardStore) => state.widgets);

  useFetchMetrics();

  const fetchResourceNames = useResourceNameStore((state) => state.fetchResources);

  // Dynamically extract all available resources and metrics directly from the store memory
  const getResourceList = useMetricStore((state) => state.getResourceList);
  const getMetricList = useMetricStore((state) => state.getMetricList);
  const availableResources = getResourceList();
  const availableMetricTypes = getMetricList();

  // Prepare a dynamic fallback default configuration for new widgets/ensures no empty fields in config to break stuff
  const defaultResource = availableResources.length > 0 ? availableResources[0] : "";
  const defaultMetric = defaultResource && availableMetricTypes[defaultResource]?.length > 0
    ? availableMetricTypes[defaultResource][0]
    : "anon" as MetricType;

  const defaultNewWidgetConfig = useMemo<WidgetConfigData>(() => ({
    id: "new-widget-temp-id",
    forTitle: "New Widget",
    resourceId: defaultResource,
    metricType: defaultMetric,
    forWidgetType: "line"
  }), [defaultResource, defaultMetric]);

  const { 
    setInitialState, 
    addWidget, 
    removeWidget, 
    updateLayouts, 
    setActiveDashboard, 
    addDashboard 
  } = useDashboardStore((state: DashboardStore) => state.actions);

  useEffect(() => {
    const loadDashboardData = async () => {
      // only initialize if we don't have any dashboards in the store yet make full use of zustand caching. 
      setIsLoading(true);

      // Leverage fact that dashboards need to be loaded into mem to trigger initial resource fetch
      await fetchResourceNames();
      if (Object.keys(dashboards).length === 0) {
        // simulate API Fetch: const response = await fetch('/api/dashboards');
        const initialConfigs: WidgetConfigData[] = [
          { id: "w-1", forTitle: "Live CPU Usage (Mock)", resourceId: "74266597-141c-3ecc-8f68-8667ff7163a7", metricType: "cpu", forWidgetType: "line" },
          { id: "w-2", forTitle: "Live Memory (Mock)", resourceId: "74266597-141c-3ecc-8f68-8667ff7163a7", metricType: "cpu", forWidgetType: "gauge" },
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
        const currentUrlId = searchParams.get("id");

        if (currentUrlId && initialDashboards[currentUrlId]) {
          setActiveDashboard(currentUrlId);
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
  }, [setInitialState, setActiveDashboard, router, dashboards]);

  // sync Zustand store when the URL changes (i.e browser back/forward buttons)
  useEffect(() => {
    if (urlId && dashboards[urlId] && urlId !== activeDashboardId) {
      setActiveDashboard(urlId);
    }
  }, [urlId, dashboards, activeDashboardId, setActiveDashboard]);

  const handleDashboardChange = useCallback((id: string) => {
    router.push(`?id=${id}`);
  }, [router]);

  // compute the layouts array for the active dashboard
  const activeDashboard = activeDashboardId ? dashboards[activeDashboardId] : undefined;
  const widgetLayouts = activeDashboard?.layoutItemIds
    .map((id: string) => layoutsMap[id])
    .filter((l): l is LayoutItem => !!l) ?? [];

  // maps normalized dashboards back to stub format for thetoollbar dropdown
  const dashboardStubs: DashboardStub[] = Object.values(dashboards).map((d: DashboardConfig) => ({
    id: d.id,
    label: d.name
  }));

  const handleStartEditing = useCallback(() => {
    setOriginalLayout(widgetLayouts.map(l => ({ ...l })));
    setOriginalConfigs(Object.values(widgetsMap).map(c => ({ ...c })));
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

  const handleCreateDashboard = useCallback((name: string) => {
    const newId = crypto.randomUUID();
    const newDashboard: DashboardConfig = {
      id: newId,
      name: name,
      layoutItemIds: [],
    };
    addDashboard(newDashboard);
    router.push(`?id=${newId}`); //push new id search params to url to redirect user to new dash
  }, [addDashboard, router]);

  //  UUID's for widget and layout id's so in theory won't be an id clash
  const handleAddWidget = useCallback(() => {
    setIsConfigModalOpen(true);
  }, []);

  const handleConfirmAddWidget = useCallback((customConfig: WidgetConfigData) => {
    const widgetId = crypto.randomUUID();
    const layoutId = crypto.randomUUID();
    const newConfig: WidgetConfigData = {
      ...customConfig,
      id: widgetId,
    };
    const newLayout: LayoutItem = { id: layoutId, widgetId, x: 0, y: 0, w: 6, h: 4, autoPosition: true };

    addWidget(newLayout, newConfig);
    setIsConfigModalOpen(false);
  }, [addWidget]);

  const handleDeleteWidget = useCallback((layoutId: string, widgetId: string) => {
    removeWidget(layoutId, widgetId);
  }, [removeWidget]);

  const handleLayoutChange = useCallback((newLayout: LayoutItem[]) => {
    updateLayouts(newLayout);
  }, [updateLayouts]);

  const handleDateRangeChange = useCallback((range: DateRange | undefined) => {
    if (range?.from && range?.to) {
      setWindow(range.from, range.to);
    }
  }, [setWindow]);

  return (
    <>
      <header className="sticky top-0 z-10 border-b bg-background/95 px-6 py-4">
        <Toolbar
          dashboards={dashboardStubs}
          isEditMode={isEditMode}
          handleAddWidget={handleAddWidget}
          handleStartEditing={handleStartEditing}
          handleSaveEdit={handleSaveEdit}
          handleCancelEdit={handleCancelEdit}
          selectedDashboardId={activeDashboardId || ""}
          onDashboardChange={handleDashboardChange}
          onCreateDashboard={handleCreateDashboard}
          dateRange={dateRange}
          onDateRangeChange={handleDateRangeChange}
        />
      </header>

      {streamError && (
        <div className="mx-6 mt-4 p-3 bg-destructive/10 border border-destructive/80 rounded-md text-destructive text-xs">
          Stream Error: {streamError.message}. Real-time updates may be paused.
        </div>
      )}

      <main className="flex-1 overflow-y-auto overflow-x-hidden m-3 flex flex-col">
        {isLoading ? (
          <div className="flex-1 flex items-center justify-center text-muted-foreground animate-pulse">
            Loading dashboards...
          </div>
        ) : !activeDashboard ? (
          <div className="flex-1 flex flex-col items-center justify-center text-center p-10">
            <h2 className="text-xl font-semibold mb-2">No Dashboards Found</h2>
            <p className="text-muted-foreground mb-6">Create your first dashboard to start monitoring your cloud resources.</p>
          </div>
        ) : widgetLayouts.length === 0 ? //display if dashboard doesnt contain any widgets
        (
          <div className="flex-1 flex flex-col items-center justify-center text-center p-10">
            <h2 className="text-xl font-semibold mb-2">This Dashboard is Empty</h2>
            <p className="text-muted-foreground mb-6">Start building your layout by adding a new widget.</p>
            <Button onClick={handleAddWidget}>
              Add Your First Widget
            </Button>
          </div>
        ) : (
          <Grid
            isEditMode={isEditMode}
            dashboardId={activeDashboardId || ""}
            onLayoutChange={handleLayoutChange}
            layouts={widgetLayouts}
            onDeleteWidget={handleDeleteWidget}
          />
        )}

        {isConfigModalOpen && (
          <WidgetConfig
            isOpen={isConfigModalOpen}
            onClose={() => setIsConfigModalOpen(false)}
            onSave={handleConfirmAddWidget}
            forExistingConfig={defaultNewWidgetConfig}
            forAvailableResources={availableResources}
            forAvailableMetricTypes={availableMetricTypes}
          />
        )}
      </main>
    </>
  );
}

// this part of the page depends on runtime info (like searchparams) that isn't available during the static build.
// still prerender the static parts of your dashboard
// fixes lighthouse issues hopefully 
export default function DashboardPage() {
  return (
    <Suspense fallback={<div className="flex-1 flex items-center justify-center text-muted-foreground animate-pulse">Loading dashboard...</div>}>
      <DashboardContent />
    </Suspense>
  );
}
