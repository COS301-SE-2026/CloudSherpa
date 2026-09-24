"use client";

import {Button} from "@/components/atoms/button";
import {Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle} from "@/components/atoms/dialog";
import {SEVERITY_COLOURS, STATUS_LABELS, TYPE} from "@/features/alerts/types/alertTypes";
import type {Alert} from "@/features/alerts/types/alertTypes";

interface PropsForAlertDialog{
    alert : Alert | null;
    open : boolean;
    onClose : () => void;
    disable : (alertId : string) => Promise<void>;
    enable : (alertId : string) => Promise<void>;
}

export function AlertDialog({
    alert, open, onClose, disable, enable,
} : Readonly<PropsForAlertDialog>){
    if(!alert){
        return null;
    }

    const active = alert.status === "ACTIVE";

    const severityClass = active ? SEVERITY_COLOURS[alert.severity] : "text-muted-foreground";

    const handlingDisable = async () => {
        await disable(alert.alertId);

        onClose();
    };

    const handlingEnable = async () => {
        await enable(alert.alertId);

        onClose();
    };

    return(
        <Dialog open = {open} onOpenChange = {(change) => !change && onClose()}>
            <DialogContent className = "sm:max-w-lg">
                <DialogHeader>
                    <DialogTitle className = "text-foreground"> {alert.title} </DialogTitle>
                </DialogHeader>

                <div className = "space-y-4 text-sm">
                    <div className = "flex flex-wrap items-center gap-2">
                        <span className = {"text-sm font-semibold uppercase tracking-wider" + severityClass}> {alert.severity} </span>

                        <span className = "rounded-sm border border-border px-2 py-0.5 text-xs text-muted-foreground"> {TYPE[alert.alertType]} </span>

                        <span className = "rounded-sm border border-border px-2 py-0.5 text-xs text-muted-foreground"> {STATUS_LABELS[alert.status]} </span>
                    </div>

                    <div className = "grid grid-cols-2 gap-2">
                        <span className = "text-muted-foreground"> Created </span>

                        <span className = "text-foreground"> {alert.createdAt ? new Date(alert.createdAt).toLocaleString() : "-"} </span>

                        {alert.resolvedAt && (
                            <>
                                <span className = "text-muted-foreground"> Resolved </span>

                                <span className = "text-foreground"> {new Date(alert.resolvedAt).toLocaleString()} </span>
                            </>
                        )}
                    </div>

                    {alert.message && (
                        <div>
                            <p className = "mb-1 text-muted-foreground"> Message </p>

                            <p className = "text-foreground"> {alert.message} </p>
                        </div>
                    )}

                </div>

                <DialogFooter className = "pt-2">
                    <Button variant = "outline" onClick = {onClose}> Close </Button>

                    {active ? (
                        <Button variant = "destructive" onClick = {handlingDisable}> Disable </Button>
                    ) : (
                        <Button onClick = {handlingEnable}> Enable </Button>
                    )}
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}