"use client";

import { useRouter } from "next/navigation";
import { useCallback } from "react";
import Toolbar from "@/features/dashboard/components/toolbar/toolbar";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { KpiWidgetConfig, LayoutItem, WidgetConfig } from "@/features/dashboard/types/widgets";
import { createDashboard, updateDashboardLayout, createWidget } from "@/lib/fetch/api-dashboard";

import {
    ToolbarProvider,
    useToolbar,
} from "@/features/dashboard/components/toolbar/toolbarProvider";
import { DateRange } from "react-day-picker";

// The layout ID and widget IDs are shared, i.e. layout id ===  widget_id, hence only one UUID is generated
function generateSharedId() {
    const sharedId = crypto.randomUUID();

    return { sharedId };
}

function DashboardLayoutInner({ children }: Readonly<{ children: React.ReactNode }>) {
    const router = useRouter();
    const { isEditMode, setIsEditMode } = useToolbar();

    const activeDashboardId = useDashboardStore((state) => state.activeDashboardId);
    const hasActiveDashboard = Boolean(activeDashboardId);
    const dashboardsMap = useDashboardStore((state) => state.dashboards);
    const {
        addDashboard,
        addWidget,
        removeDashboard,
        createSnapshot,
        restoreSnapshot,
        clearSnapshot,
    } = useDashboardStore((state) => state.actions);

    const fromMs = useDashboardStore((state) => state.fromMs);
    const toMs = useDashboardStore((state) => state.toMs);
    const setWindow = useDashboardStore((state) => state.setWindow);

    const getMetricList = useMetricStore((state) => state.getMetricList);

    const dateRange = { from: new Date(fromMs), to: new Date(toMs) };
    const dashboardStubs = Object.values(dashboardsMap).map((d) => ({
        id: d.id,
        displayName: d.displayName,
    }));

    const handleDashboardChange = useCallback(
        (id: string) => {
            router.push(`/dashboard?id=${id}`);
        },
        [router]
    );

    const handleCreateDashboard = useCallback(
        async (name: string) => {
            const newId = crypto.randomUUID();
            addDashboard({
                id: newId,
                displayName: name,
                timeFrom: null,
                timeTo: null,
                predefinedTime: "T_24_HOUR",
                current: true,
                layoutItemIds: [],
            });
            router.push(`?id=${newId}`);
            try {
                await createDashboard({ id: newId, displayName: name });
            } catch (error) {
                console.error("Failed to persist new dashboard", error);
            }
        },
        [addDashboard, router]
    );

    const handleDeleteDashboard = useCallback(
        async (id: string) => {
            removeDashboard(id);

            if (activeDashboardId === id) {
                const remainingIds = Object.keys(dashboardsMap).filter((dId) => dId !== id);

                if (remainingIds.length > 0) {
                    router.push(`?id=${remainingIds[0]}`);
                } else {
                    router.push(`/dashboard`);
                }
            }
        },
        [removeDashboard, activeDashboardId, dashboardsMap, router]
    );

    const handleDateRangeChange = useCallback(
        (range: DateRange | undefined) => {
            if (!activeDashboardId) return;
            if (range?.from && range?.to) setWindow(range.from, range.to);
        },
        [activeDashboardId, setWindow]
    );

    const handleStartEditing = useCallback(() => {
        if (!activeDashboardId) return;
        createSnapshot();
        setIsEditMode(true);
    }, [activeDashboardId, setIsEditMode, createSnapshot]);

    const handleSaveEdit = useCallback(async () => {
        clearSnapshot();
        setIsEditMode(false);
        if (!activeDashboardId) return;
        const currentLayouts = useDashboardStore.getState().layouts;
        const activeDashboard = useDashboardStore.getState().dashboards[activeDashboardId];
        const layoutPayload = activeDashboard.layoutItemIds.map((id) => {
            const l = currentLayouts[id];
            return {
                id: l.id,
                x: l.x,
                y: l.y,
                w: l.w,
                h: l.h,
            };
        });
        try {
            await updateDashboardLayout(activeDashboardId, layoutPayload);
        } catch (error) {
            console.error("Failed to persist dashboard layout sync", error);
        }
    }, [setIsEditMode, clearSnapshot, activeDashboardId]);

    const handleCancelEdit = useCallback(() => {
        restoreSnapshot();
        setIsEditMode(false);
    }, [setIsEditMode, restoreSnapshot]);

    const handleAddKpi = useCallback(async () => {
        if (!activeDashboardId) {
            return;
        }
        const { sharedId } = generateSharedId();

        const newKpiConfig: KpiWidgetConfig = {
            id: sharedId,
            widgetType: "KPI",
            displayName: "New KPI",
            chargeIds: [],
            aggregationWindowDays: 30,
        };

        const newLayout: LayoutItem = {
            id: sharedId,
            x: 0,
            y: 0,
            w: 4,
            h: 4,
            autoPosition: true,
        };

        addWidget(newLayout, newKpiConfig);

        try {
            await createWidget(activeDashboardId, {
                ...newKpiConfig,
                startX: newLayout.x,
                startY: newLayout.y,
                width: newLayout.w,
                height: newLayout.h,
            });
        } catch (error) {
            console.error("Failed to persist new widget", error);
        }
    }, [addWidget, activeDashboardId]);

    const handleAddWidget = useCallback(async () => {
        if (!activeDashboardId) return;

        const { sharedId } = generateSharedId();

        const newConfig: WidgetConfig = {
            widgetType: "CHART",
            id: sharedId,
            displayName: "New Chart",
            chartType: "line_chart",
            resourceId: null,
            metricType: null,
        };

        const newLayout: LayoutItem = {
            id: sharedId,
            x: 0,
            y: 0,
            w: 6,
            h: 4,
            autoPosition: true,
        };
        if (!isEditMode) {
            createSnapshot();
        }

        addWidget(newLayout, newConfig);
        setIsEditMode(true);

        try {
            await createWidget(activeDashboardId, {
                id: newConfig.id,
                widgetType: "CHART",
                chartType: newConfig.chartType,
                displayName: newConfig.displayName,
                startX: newLayout.x,
                startY: newLayout.y,
                width: newLayout.w,
                height: newLayout.h,
                resourceId: newConfig.resourceId,
                metricType: newConfig.metricType,
            });
        } catch (error) {
            console.error("Failed to persist new widget", error);
        }
    }, [addWidget, getMetricList, setIsEditMode, isEditMode, createSnapshot, activeDashboardId]);

    return (
        <div className="flex flex-col flex-1 h-full w-full">
            <Toolbar
                dashboards={dashboardStubs}
                isEditMode={isEditMode}
                hasActiveDashboard={hasActiveDashboard}
                selectedDashboardId={activeDashboardId || ""}
                onDashboardChange={handleDashboardChange}
                onCreateDashboard={handleCreateDashboard}
                dateRange={dateRange}
                onDateRangeChange={handleDateRangeChange}
                handleAddWidget={handleAddWidget}
                handleAddKpi={handleAddKpi}
                handleStartEditing={handleStartEditing}
                handleSaveEdit={handleSaveEdit}
                handleCancelEdit={handleCancelEdit}
                onDeleteDashboard={handleDeleteDashboard}
            />
            <div className="flex-1 overflow-y-auto overflow-x-hidden flex flex-col relative">
                {children}
            </div>
        </div>
    );
}

export default function DashboardLayout({ children }: Readonly<{ children: React.ReactNode }>) {
    return (
        <ToolbarProvider>
            <DashboardLayoutInner>{children}</DashboardLayoutInner>
        </ToolbarProvider>
    );
}
