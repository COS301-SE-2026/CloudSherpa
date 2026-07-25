import { WidgetConfig } from "@/features/dashboard/types/widgets";
import { ChartWidget } from "./chartWidget";
import { KPIWidget } from "../../kpi/kpi-widget";

interface WidgetProps {
    config: WidgetConfig;
}

export default function Widget({ config }: Readonly<WidgetProps>) {
    return (
        <>
            {config.widgetType === "CHART" ? (
                <ChartWidget config={config} />
            ) : (
                <KPIWidget config={config} />
            )}
        </>
    );
}
