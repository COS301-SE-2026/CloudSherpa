import { WidgetConfig } from "@/features/dashboard/types/widgets";
import { ChartWidget } from "./chartWidget";
import { KPIWidget } from "../../kpi/kpi-widget";
import { useToolbar } from "@/features/dashboard/components/toolbar/toolbarProvider";

interface WidgetProps {
    config: WidgetConfig;
}

export default function Widget({ config }: Readonly<WidgetProps>) {
    const { isEditMode } = useToolbar();

    return (
        <>
            {config.widgetType === "CHART" ? (
                <ChartWidget config={config} />
            ) : (
                <KPIWidget config={config} isEditMode={isEditMode} />
            )}
        </>
    );
}
