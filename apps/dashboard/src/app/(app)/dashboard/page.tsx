"use client";

import { useState, useCallback, useEffect, Suspense, useMemo } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useToolbar } from "@/features/dashboard/components/toolbar/toolbarProvider";

import { Spinner } from "@/components/atoms/spinner";
import Grid from "@/features/dashboard/components/widgetGrid/grid";
import {
    LayoutItem,
    DashboardConfig,
    WidgetConfig,
    ChartType,
} from "@/features/dashboard/types/widgets";
import { useDashboardStore, DashboardStore } from "@/features/dashboard/stores/dashboard-store";
import { useMetricStream } from "@/features/dashboard/services/sse/metric-stream";
import { useFetchMetrics } from "@/features/dashboard/hooks/useFetchMetrics";
import { useResourceNameStore } from "@/features/dashboard/stores/resource-store";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { fetchDashboards, DashboardDTO } from "@/lib/fetch/api-dashboard";
import { MetricType } from "@/features/dashboard/types/metric";

function DashboardContent() {
    const { error: streamError } = useMetricStream();
    const router = useRouter();
    const searchParams = useSearchParams();
    const urlId = searchParams.get("id");
    const [isLoading, setIsLoading] = useState(true);
    const { isEditMode } = useToolbar();

    const dashboards = useDashboardStore((state: DashboardStore) => state.dashboards);
    const activeDashboardId = useDashboardStore((state: DashboardStore) => state.activeDashboardId);
    const layoutsMap = useDashboardStore((state: DashboardStore) => state.layouts);

    // Metrics and resource name stores
    const { metricFetchError, metricFetchLoad } = useFetchMetrics();
    const fetchResourceNames = useResourceNameStore((state) => state.fetchResources);
    const getMetricList = useMetricStore((state) => state.getMetricList);

    const { setInitialState, removeWidget, updateLayouts, setActiveDashboard } = useDashboardStore(
        (state: DashboardStore) => state.actions
    );

    const createDefaultWidgetConfig = useCallback(
        (id: string, displayName: string, type: ChartType): WidgetConfig => {
            const metricsByResource = getMetricList();
            const resourceId = Object.keys(metricsByResource)[0];

            return {
                id,
                displayName,
                type,
                resourceId,
                metricType: resourceId ? (metricsByResource[resourceId]?.[0] ?? "anon") : "anon",
            };
        },
        [getMetricList]
    );

    function processFetchedDashboards(fetchedData: DashboardDTO[]) {
        const dashboardsMap: Record<string, DashboardConfig> = {};
        const layoutsArray: LayoutItem[] = [];
        const configsArray: WidgetConfig[] = [];

        for (const db of fetchedData) {
            dashboardsMap[db.id] = {
                id: db.id,
                displayName: db.displayName,
                timeFrom: db.timeFrom,
                timeTo: db.timeTo,
                predefinedTime: db.predefinedTime,
                current: db.current,
                layoutItemIds: db.widgets.map((w) => w.id),
            };

            for (const w of db.widgets) {
                layoutsArray.push({
                    id: w.id,
                    x: w.startX,
                    y: w.startY,
                    w: w.width,
                    h: w.height,
                    autoPosition: false,
                });

                configsArray.push({
                    id: w.id,
                    type: w.type as ChartType,
                    displayName: w.displayName,
                    resourceId: w.resourceId,
                    metricType: w.metricType as MetricType | null,
                });
            }
        }

        return { dashboardsMap, layoutsArray, configsArray };
    }

    useEffect(() => {
        const loadDashboardData = async () => {
            if (Object.keys(dashboards).length > 0) {
                setIsLoading(false);
                return;
            }

            await fetchResourceNames();
            if (metricFetchLoad) {
                return;
            }
            if (metricFetchError) {
                setIsLoading(false);
            }
            try {
                const fetchedData = await fetchDashboards();
                const { dashboardsMap, layoutsArray, configsArray } =
                    processFetchedDashboards(fetchedData);
                setInitialState(dashboardsMap, layoutsArray, configsArray);
                const currentDb = fetchedData.find((d) => d.current);
                const defaultId =
                    urlId && dashboardsMap[urlId]
                        ? urlId
                        : currentDb
                          ? currentDb.id
                          : fetchedData[0]?.id;

                if (defaultId) {
                    setActiveDashboard(defaultId);
                    if (urlId !== defaultId) {
                        router.replace(`?id=${defaultId}`);
                    }
                }
            } catch (error) {
                console.error("Failed to load dashboards from API", error);
            } finally {
                setIsLoading(false);
            }
        };

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

    // computes the layouts array for the active dashboard
    const activeDashboard = activeDashboardId ? dashboards[activeDashboardId] : undefined;
    const widgetLayouts = useMemo(() => {
        return (
            activeDashboard?.layoutItemIds
                ?.map((id: string) => layoutsMap[id])
                .filter((l): l is LayoutItem => !!l) ?? []
        );
    }, [activeDashboard, layoutsMap]);

    const handleDeleteWidget = useCallback(
        (layoutId: string, widgetId: string) => {
            removeWidget(layoutId, widgetId);
        },
        [removeWidget]
    );

    const handleLayoutChange = useCallback(
        (newLayout: LayoutItem[]) => {
            updateLayouts(newLayout);
        },
        [updateLayouts]
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
                    <p className="text-muted-foreground mb-6">
                        Widgets are paused until historical metrics are available.
                    </p>
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
                    Metric Error: {metricFetchError.message}. Widgets will render once historical
                    metrics are available.
                </div>
            )}

            <main
                className="flex-1 overflow-y-auto overflow-x-hidden m-3 flex flex-col"
                data-testid="dashboard"
            >
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
            }
        >
            <DashboardContent />
        </Suspense>
    );
}
