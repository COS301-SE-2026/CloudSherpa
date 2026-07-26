"use client";

import { Card, CardHeader, CardTitle } from "@/components/atoms/card";
import { KpiWidgetConfig } from "../../types/widgets";
import { useFetchKpiValue } from "./config/hooks/useFetchKpiValue";
import { Spinner } from "@/components/atoms/spinner";
import { Button } from "@/components/atoms/button";
import { EllipsisVertical, Pencil, Trash } from "lucide-react";
import { useDashboardStore } from "@/features/dashboard/stores/dashboard-store";
import { useRouter } from "next/navigation";
import {
    ContextMenu,
    ContextMenuContent,
    ContextMenuItem,
    ContextMenuSeparator,
    ContextMenuTrigger,
} from "@/components/atoms/context-menu";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/atoms/dropdown-menu";

interface WidgetProps {
    readonly config: KpiWidgetConfig;
    readonly preview?: boolean;
    readonly isEditMode?: boolean;
}

export function KPIWidget({ config, preview = false, isEditMode = false }: WidgetProps) {
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
        <ContextMenu>
            <ContextMenuTrigger>
                <Card className={`flex flex-col gap-4 p-6 ${preview ? "bg-muted/40" : ""}`}>
                    <CardHeader className="flex flex-row items-center justify-between p-0">
                        <CardTitle>{config.displayName}</CardTitle>

                        {!preview && !showSaveBeforeConfigure && (
                            <DropdownMenu>
                                <DropdownMenuTrigger asChild>
                                    <Button variant="ghost" size="icon" className="h-8 w-8">
                                        <EllipsisVertical className="h-4 w-4" />
                                    </Button>
                                </DropdownMenuTrigger>
                                <DropdownMenuContent align="end" className="w-fit">
                                    <DropdownMenuItem onClick={openConfig} disabled={isEditMode}>
                                        <Pencil className="mr-2 h-4 w-4" />
                                        Configure Widget
                                    </DropdownMenuItem>
                                    <DropdownMenuSeparator />
                                    <DropdownMenuItem
                                        onClick={() => removeWidget(id, id)}
                                        className="text-destructive focus:text-destructive"
                                    >
                                        <Trash className="mr-2 h-4 w-4" />
                                        Delete Widget
                                    </DropdownMenuItem>
                                </DropdownMenuContent>
                            </DropdownMenu>
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
            </ContextMenuTrigger>
            <ContextMenuContent className="w-48">
                <ContextMenuItem onClick={openConfig} disabled={isEditMode}>
                    <Pencil className="mr-2 h-4 w-4" />
                    Configure Widget
                </ContextMenuItem>
                <ContextMenuSeparator />
                <ContextMenuItem
                    onClick={() => removeWidget(id, id)}
                    className="text-destructive focus:text-destructive"
                >
                    <Trash className="mr-2 h-4 w-4" />
                    Delete Widget
                </ContextMenuItem>
            </ContextMenuContent>
        </ContextMenu>
    );
}
