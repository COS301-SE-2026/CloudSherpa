"use client";

import { Card, CardHeader, CardTitle } from "@/components/atoms/card";
import { KpiWidgetConfig } from "../../types/widgets";
import { useFetchKpiValue } from "./config/hooks/useFetchKpiValue";
import { Spinner } from "@/components/atoms/spinner";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import { useRouter } from "next/navigation";
import { WidgetDropdown } from "@/features/dashboard/components/widgetDropdown";
import { WidgetMenu } from "@/features/dashboard/components/widgetMenu";

interface WidgetProps {
    config: KpiWidgetConfig;
    preview?: boolean;
    isEditMode?: boolean;
}

export function KPIWidget({ config, preview = false, isEditMode = false }: Readonly<WidgetProps>) {
    const { kpiPreview, loadingKpiValue } = useFetchKpiValue(config);
    const { id } = config;

    const openConfig = () => {
        if (!isEditMode) {
            router.push(`/edit/kpi/${config.id}`);
        }
    };
    const removeWidget = useDashboardStore((state) => state.actions.removeWidget);
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
    const showSaveBeforeConfigure = isEditMode && !preview;

    return (
        <WidgetMenu
            onConfigure={openConfig}
            isEditMode={isEditMode}
            preview={preview}
            onDelete={() => removeWidget(id, id)}
        >
            <Card
                className={`flex flex-col gap-4 p-6 h-full w-full justify-between ${preview ? "bg-muted/40 h-50" : ""}`}
            >
                <CardHeader className="flex flex-row items-center justify-between p-0">
                    <CardTitle>{config.displayName}</CardTitle>

                    {!preview && !showSaveBeforeConfigure && (
                        <WidgetDropdown
                            onConfigure={openConfig}
                            onDelete={() => removeWidget(id, id)}
                            isEditMode={isEditMode}
                        />
                    )}
                </CardHeader>

                {showSaveBeforeConfigure ? (
                    <div className="flex flex-1 items-center justify-center">
                        <p className="text-xs text-muted-foreground italic text-center">
                            Save dashboard changes before configuring this widget.
                        </p>
                    </div>
                ) : (
                    <>
                        {loadingKpiValue ? (
                            <Spinner />
                        ) : (
                            <h1 className="text-xl">${kpiPreview?.value.toFixed(5)}</h1>
                        )}
                        <p>Accross {config.chargeIds.length} Resources</p>
                        <div className="flex flex-row justify-between">
                            <p>Last {config.aggregationWindowDays} days</p>
                            {loadingKpiValue ? (
                                <Spinner />
                            ) : (
                                <p>Updated {formattedUpdatedAtDate}</p>
                            )}
                        </div>
                    </>
                )}
            </Card>
        </WidgetMenu>
    );
}
