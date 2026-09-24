"use client";

import {Card, CardContent, CardHeader} from "@/components/atoms/card";
import { SEVERITY_COLOURS, STATUS_LABELS, TYPE } from "@/features/alerts/types/alertTypes";
import type {Alert} from "@/features/alerts/types/alertTypes";
interface PropsForAlertCard{
    alert : Alert;
    open : (alert : Alert) => void;
}

export function AlertCard({alert, open} : Readonly<PropsForAlertCard>){
    const inactive = alert.status !== "ACTIVE";

    const classForSeverity = inactive ? "text-muted-foreground" : SEVERITY_COLOURS[alert.severity];

    return(
        <Card className = {"cursor-pointer transition-colors hover:border-ring" + (inactive ? "opacity-60" : "")} onClick = {() => open(alert)}>
            <CardHeader className = "pb-2">
                <div className = "flex items-start justify-between gap-4">
                    <div className = "flex flex-wrap items-center gap-2">
                        <span className = {"text-xs font-semibold uppercase tracking-wider" + classForSeverity}> {alert.severity} </span>

                        <span className = "rounded-sm border border-border px-2 py-0.5 text-xs text-muted-foreground"> {TYPE[alert.alertType]} </span>
                    </div>

                    <span className = "shrink-0 text-xs text-muted-foreground"> {STATUS_LABELS[alert.status]} </span>
                </div>
            </CardHeader>

            <CardContent className = "space-y-2">
                <h3 className = "text-sm font-medium text-foreground"> {alert.title} </h3>

                {alert.message && (
                    <p className = "line-clamp-2 text-sm text-muted-foreground"> {alert.message} </p>
                )}

                <p className = "text-xs text-muted-foreground"> {alert.createdAt ? new Date(alert.createdAt).toLocaleString() : "-"} </p>
            </CardContent>
        </Card>
    );
}