"use client";

import { useRouter } from "next/navigation";
import { useCallback } from "react";
import Toolbar from "@/features/dashboard/components/toolbar/toolbar";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import { useWindowStore } from "@/features/dashboard/stores/window-store";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { LayoutItem, WidgetConfig } from "@/features/dashboard/types/widgets";
import {
    ToolbarProvider,
    useToolbar,
} from "@/features/dashboard/components/toolbar/toolbarProvider";
import { DateRange } from "react-day-picker";

function DashboardLayoutInner({ children }: Readonly<{ children: React.ReactNode }>) {
    const router = useRouter();
    const { isEditMode, setIsEditMode } = useToolbar();

    const activeDashboardId = useDashboardStore((state) => state.activeDashboardId);
    const dashboardsMap = useDashboardStore((state) => state.dashboards);
    const { addDashboard, addWidget } = useDashboardStore((state) => state.actions);

    const fromMs = useWindowStore((state) => state.fromMs);
    const toMs = useWindowStore((state) => state.toMs);
    const setWindow = useWindowStore((state) => state.setWindow);

    const getMetricList = useMetricStore((state) => state.getMetricList);

    const dateRange = { from: new Date(fromMs), to: new Date(toMs) };
    const dashboardStubs = Object.values(dashboardsMap).map((d) => ({
        id: d.id,
        label: d.name,
    }));

    const handleDashboardChange = useCallback(
        (id: string) => {
            router.push(`?id=${id}`);
        },
        [router]
    );

    const handleCreateDashboard = useCallback(
        (name: string) => {
            const newId = crypto.randomUUID();
            addDashboard({ id: newId, name: name, layoutItemIds: [] });
            router.push(`?id=${newId}`);
        },
        [addDashboard, router]
    );

    const handleDateRangeChange = useCallback(
        (range: DateRange | undefined) => {
            if (range?.from && range?.to) setWindow(range.from, range.to);
        },
        [setWindow]
    );

    const handleStartEditing = useCallback(() => {
        setIsEditMode(true);
    }, [setIsEditMode]);

    const handleSaveEdit = useCallback(() => {
        setIsEditMode(false);
    }, [setIsEditMode]);

    const handleCancelEdit = useCallback(() => {
        setIsEditMode(false);
    }, [setIsEditMode]);

    const handleAddWidget = useCallback(() => {
        const widgetId = crypto.randomUUID();
        const layoutId = crypto.randomUUID();
        const metricsByResource = getMetricList();
        const resourceId = Object.keys(metricsByResource)[0];

        const newConfig: WidgetConfig = {
            id: widgetId,
            widgetType: "chart",
            title: "New Widget (Click to Customize)",
            chartType: "line",
            resourceId: resourceId,
            metricType: resourceId ? (metricsByResource[resourceId]?.[0] ?? "anon") : "anon",
        };

        const newLayout: LayoutItem = {
            id: layoutId,
            widgetId,
            x: 0,
            y: 0,
            w: 6,
            h: 4,
            autoPosition: true,
        };
        addWidget(newLayout, newConfig);
        setIsEditMode(true);
    }, [addWidget, getMetricList, setIsEditMode]);

    return (
        <div className="flex flex-col flex-1 h-full w-full">
            <Toolbar
                dashboards={dashboardStubs}
                isEditMode={isEditMode}
                selectedDashboardId={activeDashboardId || ""}
                onDashboardChange={handleDashboardChange}
                onCreateDashboard={handleCreateDashboard}
                dateRange={dateRange}
                onDateRangeChange={handleDateRangeChange}
                handleAddWidget={handleAddWidget}
                handleStartEditing={handleStartEditing}
                handleSaveEdit={handleSaveEdit}
                handleCancelEdit={handleCancelEdit}
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
