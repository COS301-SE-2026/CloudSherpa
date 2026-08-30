"use client";

import { useCallback, useEffect, Suspense, useMemo, useRef } from "react";
import { useSearchParams, useRouter, usePathname } from "next/navigation";
import { useToolbar } from "@/features/dashboard/components/toolbar/toolbarProvider";

import { Spinner } from "@/components/atoms/spinner";
import Grid from "@/features/dashboard/components/widgetGrid/grid";
import { LayoutItem } from "@/features/dashboard/types/widgets";
import { useDashboardStore, DashboardStore } from "@/features/dashboard/stores/dashboard-store";
import { useMetricStream } from "@/features/dashboard/services/sse/metric-stream";
import { useLoadDashboardData } from "@/features/dashboard/hooks/useLoadDash";
import { toast } from "sonner";

function DashboardContent() {
    const { error: streamError } = useMetricStream();
    const searchParams = useSearchParams();
    const urlId = searchParams.get("id");
    const { isEditMode } = useToolbar();

    const { isLoading, metricFetchError } = useLoadDashboardData();

    const dashboards = useDashboardStore((state: DashboardStore) => state.dashboards);
    const activeDashboardId = useDashboardStore((state: DashboardStore) => state.activeDashboardId);
    const layoutsMap = useDashboardStore((state: DashboardStore) => state.layouts);

    const { updateLayouts, setActiveDashboard } = useDashboardStore(
        (state: DashboardStore) => state.actions
    );

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

    const handleLayoutChange = useCallback(
        (newLayout: LayoutItem[]) => {
            updateLayouts(newLayout);
        },
        [updateLayouts]
    );

    const router = useRouter();
    const pathname = usePathname();
    const authToastHandled = useRef(false);

    useEffect(() => {
        // checks if where user is coming from
        const isNewLogin = searchParams.get("new_login") === "true";
        const isNewAccount = searchParams.get("new_account") === "true";

        // this lock prevents the toast from triggering more than once on page render
        if (!authToastHandled.current) {
            authToastHandled.current = true;

            const params = new URLSearchParams(searchParams.toString());

            if (isNewLogin) {
                toast.success("Successfully logged in!");
                params.delete("new_login");
            } else if (isNewAccount) {
                toast.success("Successfully created account!");
                params.delete("new_account");
            }

            // cleans url
            router.replace(`${pathname}?${params.toString()}`, { scroll: false });
        }
    }, [searchParams, pathname, router]);

    const renderMainContent = () => {
        if (isLoading) {
            return (
                <div className="h-full w-full flex flex-col justify-center items-center gap-2">
                    <Spinner className="w-10 h-10" />
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

            <main className="flex-1 overflow-x-hidden m-3 flex flex-col" data-testid="dashboard">
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
