"use client";

import { Card, CardHeader, CardTitle } from "@/components/atoms/card";
import { KpiWidgetConfig } from "../../types/widgets";
import { useFetchKpiValue } from "./config/hooks/useFetchKpiValue";
import { Spinner } from "@/components/atoms/spinner";
import { Button } from "@/components/atoms/button";
import { EllipsisVertical } from "lucide-react";
import { useRouter } from "next/navigation";

interface WidgetProps {
    readonly config: KpiWidgetConfig;
    readonly preview?: boolean;
}

export function KPIWidget({ config, preview = false }: WidgetProps) {
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

    const router = useRouter();

    return (
        <Card className={`flex flex-col gap-4 p-6 ${preview ? "bg-muted/40" : ""}`}>
            <CardHeader className="flex flex-row items-center justify-between p-0">
                <CardTitle>{config.title}</CardTitle>
                {!preview && (
                    <Button
                        onClick={() => router.push(`/edit/kpi/${config.id}`)}
                        className="text-muted-foreground bg-transparent hover:bg-muted/10"
                    >
                        <EllipsisVertical />
                    </Button>
                )}
            </CardHeader>
            {loadingKpiValue ? (
                <Spinner />
            ) : (
                <h1 className="text-xl">${kpiPreview?.value.toFixed(5)}</h1>
            )}
            <p>Accross {config.chargeIds.length} Resources</p>
            <div className="flex flex-row justify-between">
                <p>Last {config.aggregationWindowDays} days</p>
                {loadingKpiValue ? <Spinner /> : <p>Updated {formattedUpdatedAtDate}</p>}
            </div>
        </Card>
    );
}
