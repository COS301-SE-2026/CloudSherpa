import { cn } from "@/lib/utils";
import { LayoutItem } from "@/features/dashboard/types/widgets";
import { useDashboardStore, DashboardStore } from "@/features/dashboard/stores/dashboard-store";
import Widget from "@/features/dashboard/components/widgetGrid/widgets/widget";

interface WidgetWrapperProps {
    layout: LayoutItem;
    isEditMode: boolean;
}

export const WidgetWrapper = ({ layout, isEditMode }: WidgetWrapperProps) => {
    const { id, x, y, w, h, autoPosition } = layout;
    const config = useDashboardStore((state: DashboardStore) => state.widgets[id]);

    if (!config) return null;

    const gridStackAttributes = {
        "gs-id": id,
        "gs-x": x,
        "gs-y": y,
        "gs-w": w,
        "gs-h": h,
        "gs-min-w": 3,
        "gs-min-h": 3,
        "data-widget-id": id,
        ...(autoPosition ? { "gs-auto-position": "true" } : {}),
    };

    return (
        <div className="grid-stack-item" {...gridStackAttributes}>
            <div className="grid-stack-item-content relative overflow-visible! rounded-md group">
                {isEditMode && (
                    <div className="drag-handle absolute inset-0 z-40 cursor-grab active:cursor-grabbing rounded-xl" />
                )}

                <div
                    className={cn(
                        "h-full w-full",
                        isEditMode &&
                            "pointer-events-none ring-2 ring-primary/20 rounded-xl transition-all"
                    )}
                >
                    <Widget config={config} />
                </div>
            </div>
        </div>
    );
};
