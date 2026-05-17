import { WidgetConfig, LayoutItem } from "@/types/widgets";

interface DashboardState {
    //layout and widgets are keyed by id
    layouts: Record<string, LayoutItem>;
    widgets: Record<string, WidgetConfig>; 
    
    updateLayoutGeometry: (id: string, x: number, y: number, w: number, h: number) => void;
    updateWidgetConfig: (id: string, config: Partial<WidgetConfig>) => void;
}