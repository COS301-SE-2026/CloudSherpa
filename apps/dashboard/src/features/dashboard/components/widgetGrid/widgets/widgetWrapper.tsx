import { GripVertical } from "lucide-react";
import { cn } from "@/lib/utils";
import { LayoutItem } from "@/features/dashboard/types/widgets";
import { useDashboardStore, DashboardStore } from "@/features/dashboard/stores/dashboard-store";
import Widget from "@/features/dashboard/components/widgetGrid/widgets/widget";

interface WidgetWrapperProps {
    layout: LayoutItem;
    isEditMode: boolean;
    onDeleteWidget: (layoutId: string, widgetId: string) => void;
}

export const WidgetWrapper = ({ layout, isEditMode, onDeleteWidget }: WidgetWrapperProps) => {
    const { id, x, y, w, h, autoPosition } = layout;
    const config = useDashboardStore((state: DashboardStore) => state.widgets[id]);

    if (!config) return null;

    const gridStackAttributes = {
        "gs-id": id,
        "gs-x": x,
        "gs-y": y,
        "gs-w": w,
        "gs-h": h,
        "data-widget-id": id,
        ...(autoPosition ? { "gs-auto-position": "true" } : {}),
    };

    return (
        <div className="grid-stack-item" {...gridStackAttributes}>
            <div className="grid-stack-item-content relative overflow-visible! rounded-md group">
                {isEditMode && (
                    <div className="absolute top-2 right-2 z-50 flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                        <div className="drag-handle cursor-grab active:cursor-grabbing bg-background border border-border shadow-md p-1 rounded-md text-muted-foreground hover:text-primary transition-all">
                            <GripVertical className="h-3.5 w-3.5" />
                        </div>
                    </div>
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
