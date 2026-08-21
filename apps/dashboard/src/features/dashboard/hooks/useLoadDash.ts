import { useState, useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useDashboardStore, DashboardStore } from "@/features/dashboard/stores/dashboard-store";
import { useAuthContext } from "@/features/authentication/providers/AuthContext";
import { useResourceNameStore } from "@/features/dashboard/stores/resource-store";
import { useFetchMetrics } from "@/features/dashboard/hooks/useFetchMetrics";
import { fetchDashboards, DashboardDTO } from "@/lib/fetch/api-dashboard";
import {
    DashboardConfig,
    LayoutItem,
    WidgetConfig,
    ChartType,
} from "@/features/dashboard/types/widgets";
import { MetricType } from "@/features/dashboard/types/metric";

// Extracted helper function
export function processFetchedDashboards(fetchedData: DashboardDTO[]) {
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
            layoutItemIds: db.widgets.map((widget) => widget.id),
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

            if (w.widgetType === "CHART") {
                configsArray.push({
                    id: w.id,
                    chartType: w.chartType as ChartType,
                    widgetType: "CHART",
                    displayName: w.displayName,
                    provider: w.provider,
                    accountId: w.accountId,
                    resourceId: w.resourceId,
                    metricType: w.metricType as MetricType | null,
                });
            } else if (w.widgetType === "KPI") {
                configsArray.push({
                    id: w.id,
                    widgetType: "KPI",
                    displayName: w.displayName,
                    chargeIds: w.chargeIds,
                    aggregationWindowDays: w.aggregationWindowDays,
                });
            }
        }
    }
    return { dashboardsMap, layoutsArray, configsArray };
}

export function useLoadDashboardData() {
    const [isLoading, setIsLoading] = useState(true);
    const router = useRouter();
    const searchParams = useSearchParams();
    const urlId = searchParams.get("id");

    const { isAuthReady, isAuthenticated } = useAuthContext();
    const { metricFetchError, metricFetchLoad } = useFetchMetrics();

    const dashboards = useDashboardStore((state: DashboardStore) => state.dashboards);
    const hydrateWindow = useDashboardStore(
        (state: DashboardStore) => state.hydrateWindowOnDashboardLoad
    );
    const fetchResourceNames = useResourceNameStore((state) => state.fetchResources);

    const { setInitialState, setActiveDashboard } = useDashboardStore(
        (state: DashboardStore) => state.actions
    );

    useEffect(() => {
        const loadDashboardData = async () => {
            // If we already have dashboards in the store, no need to refetch
            if (Object.keys(dashboards).length > 0) {
                setIsLoading(false);
                return;
            }

            if (!isAuthReady || !isAuthenticated) {
                return;
            }

            await fetchResourceNames();

            if (metricFetchLoad) return;

            if (metricFetchError) {
                setIsLoading(false);
            }

            try {
                const fetchedData = await fetchDashboards();
                const { dashboardsMap, layoutsArray, configsArray } =
                    processFetchedDashboards(fetchedData);

                setInitialState(dashboardsMap, layoutsArray, configsArray);

                const currentDb = fetchedData.find((d) => d.current);
                let defaultId = fetchedData[0]?.id;

                if (urlId && dashboardsMap[urlId]) {
                    defaultId = urlId;
                } else if (currentDb) {
                    defaultId = currentDb.id;
                }

                if (defaultId) {
                    setActiveDashboard(defaultId);

                    const selectedDashboard = fetchedData.find((d) => d.id === defaultId);
                    if (selectedDashboard?.predefinedTime) {
                        hydrateWindow(selectedDashboard.predefinedTime);
                    }

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
        metricFetchLoad,
        isAuthReady,
        isAuthenticated,
        hydrateWindow,
    ]);

    return { isLoading, metricFetchError, metricFetchLoad };
}
