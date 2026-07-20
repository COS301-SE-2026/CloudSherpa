"use client";

import { Card } from "@/components/atoms/card";
import { KpiWidgetConfig } from "../../types/widgets";
import { useFetchKpiValue } from "./config/hooks/useFetchKpiValue";
import { Spinner } from "@/components/atoms/spinner";

interface WidgetProps {
    readonly config: KpiWidgetConfig;
}

export function KPIWidget({ config }: WidgetProps) {
    const { kpiPreview, loadingKpiValue } = useFetchKpiValue(config);
    const options: Intl.DateTimeFormatOptions = {
        year: "numeric",
        month: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
    };

    const formattedUpdatedAtDate: string = kpiPreview
        ? new Date(kpiPreview.updatedAt).toLocaleDateString("en-GB", options)
        : "unknown";

    return (
        <Card className="flex flex-col gap-4 p-6 bg-muted/40">
            <h1 className="text-lg font-bold">{config.title}</h1>
            {loadingKpiValue ? (
                <Spinner />
            ) : (
                <h1 className="text-xl">${kpiPreview?.value.toFixed(5)}</h1>
            )}
            <p>Accross {config.resourceIds.length} Resources</p>
            <div className="flex flex-row justify-between">
                <p>Last {config.aggregationWindowDays} days</p>
                {loadingKpiValue ? <Spinner /> : <p>Updated {formattedUpdatedAtDate}</p>}
            </div>
        </Card>
    );
}
