"use client";

import { useMemo, useState } from "react";
import { Card, CardContent } from "@/components/atoms/card";
import { Input } from "@/components/atoms/input";
import { Switch } from "@/components/atoms/switch";
import { Button } from "@/components/atoms/button";
import { AlertsList } from "@/features/alerts/components/alertsList";
import { updateAlertNotificationSilence } from "@/features/alerts/alerts";
import { useAlerts } from "@/features/alerts/hooks/useAlerts";
import { useAlertStore } from "@/features/alerts/stores/alert-store";
import { updateAlertNotifications } from "@/lib/fetch/api-preferences";
import type { TypeForAlerts, Alert } from "@/features/alerts/types/alertTypes";
import RecommendationCardHero from "@/features/optimization/components/recCardHero";
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from "@/components/atoms/alert-dialog";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/atoms/dialog";
import { toast } from "sonner";
import { SEVERITY_COLOURS, STATUS_LABELS, TYPE } from "@/features/alerts/types/alertTypes";

const COLOURS_FOR_STATUS: Record<Alert["status"], string> = {
    ACTIVE: "text-success",
    DISABLED: "text-muted-foreground",
};

const TAG =
    "rounded-md border border-border px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wider";

const FILTERS: Array<{ value: "ALL" | TypeForAlerts; label: string }> = [
    { value: "ALL", label: "All" },
    { value: "THRESHOLD", label: "Threshold" },
    { value: "ANOMALY", label: "Anomaly" },
    { value: "BUDGET", label: "Budget" },
];

export function AlertsPage() {
    const { alerts, loading, forError, disable, enable, removeAlert } = useAlerts();

    const [search, setSearch] = useState("");
    const [filterType, setFilterType] = useState<"ALL" | TypeForAlerts>("ALL");
    const [deleteAlert, setDeleteAlert] = useState<Alert | null>(null);
    const [forInfo, setForInfo] = useState<Alert | null>(null);

    const inAppNotificationsEnabled = useAlertStore((state) => state.inAppNotificationsEnabled);
    const setInAppNotificationsEnabled = useAlertStore(
        (state) => state.setInAppNotificationsEnabled
    );
    const setNotificationSilence = useAlertStore((state) => state.setNotificationSilence);

    const forFilters = useMemo(() => {
        const wordSearched = search.toLowerCase();

        return alerts.filter((alert) => {
            const matches = filterType === "ALL" || alert.alertType === filterType;

            const matchesSearch =
                alert.title.toLowerCase().includes(wordSearched) ||
                (alert.message?.toLowerCase().includes(wordSearched) ?? false);

            return matches && matchesSearch;
        });
    }, [alerts, search, filterType]);

    const activeCount = alerts.filter((alert) => alert.status === "ACTIVE").length;
    const forTotalCount = alerts.length;
    const criticalCount = alerts.filter((alert) => alert.severity === "CRITICAL").length;
    const warningCount = alerts.filter((alert) => alert.severity === "WARNING").length;

    const handlingInfo = (alert: Alert) => {
        setForInfo(alert);
    };

    const handlingDelete = (alert: Alert) => {
        setDeleteAlert(alert);
    };

    const confirmingDelete = async () => {
        if (!deleteAlert) {
            return;
        }

        try {
            await removeAlert(deleteAlert.alertId);
            toast.success("Alert deleted");
            setDeleteAlert(null);
        } catch {
            toast.error("Failed to delete alert");
        }
    };

    const toggleAlertNotificationSilence = async (alert: Alert) => {
        const silenced = !alert.inAppNotificationsSilenced;

        try {
            await updateAlertNotificationSilence(alert.alertId, silenced);
            setNotificationSilence(alert.alertId, silenced);

            toast.success(silenced ? "In-app notifications muted" : "In-app notifications enabled");
        } catch {
            toast.error("Failed to update in-app notifications");
        }
    };

    const toggleInfoAlertNotifications = async () => {
        if (!forInfo) {
            return;
        }

        const silenced = !forInfo.inAppNotificationsSilenced;

        try {
            await updateAlertNotificationSilence(forInfo.alertId, silenced);
            setNotificationSilence(forInfo.alertId, silenced);
            setForInfo({ ...forInfo, inAppNotificationsSilenced: silenced });
            toast.success(silenced ? "In-app notifications muted" : "In-app notifications unmuted");
        } catch {
            toast.error("Failed to update in-app notifications");
        }
    };

    const toggleGlobalNotifications = async (enabled: boolean) => {
        try {
            await updateAlertNotifications(enabled);
            setInAppNotificationsEnabled(enabled);
            toast.success(enabled ? "In-app notifications enabled" : "In-app notifications muted");
        } catch {
            toast.error("Failed to update in-app notification settings");
        }
    };

    return (
        <div className="min-h-screen bg-background text-foreground">
            <div className="mx-auto max-w-6xl px-6 py-8">
                <header className="mb-6">
                    <h1 className="mb-4 text-2xl font-semibold tracking-tight text-foreground">
                        Alerts
                    </h1>

                    <div className="mb-4 grid grid-cols-2 gap-3 sm:grid-cols-3">
                        <RecommendationCardHero
                            value={`Total ${forTotalCount}`}
                            className="text-card-foreground"
                        />
                        <RecommendationCardHero
                            value={`Critical ${criticalCount}`}
                            className="text-destructive"
                        />
                        <RecommendationCardHero
                            value={`Warning ${warningCount}`}
                            className="text-orange-500"
                        />
                    </div>

                    <div className="mb-4 flex items-center gap-2">
                        <Input
                            className="h-9 w-64"
                            placeholder="Search alerts"
                            value={search}
                            onChange={(change) => setSearch(change.target.value)}
                        />

                        <div className="inline-flex rounded-md border border-border bg-muted p-1">
                            {FILTERS.map((filter) => (
                                <button
                                    key={filter.value}
                                    type="button"
                                    onClick={() => setFilterType(filter.value)}
                                    className={
                                        "rounded-sm px-3 py-1.5 text-sm font-medium transition-colors " +
                                        (filterType === filter.value
                                            ? "bg-primary text-primary-foreground shadow-sm"
                                            : "text-muted-foreground hover:text-foreground")
                                    }
                                >
                                    {filter.label}
                                </button>
                            ))}
                        </div>
                    </div>

                    <div className="mb-4 flex items-center justify-between">
                        <span className="text-sm text-muted-foreground">
                            {activeCount} active of {forTotalCount} alerts
                        </span>

                        <div className="flex items-center gap-2">
                            <Switch
                                checked={inAppNotificationsEnabled ?? false}
                                disabled={inAppNotificationsEnabled === null}
                                onCheckedChange={toggleGlobalNotifications}
                            />

                            <span className="text-sm text-muted-foreground">
                                In-app alert notifications
                            </span>
                        </div>
                    </div>
                </header>

                {loading && (
                    <Card>
                        <CardContent className="py-8 text-center text-sm text-muted-foreground">
                            Loading alerts
                        </CardContent>
                    </Card>
                )}

                {forError && (
                    <Card>
                        <CardContent className="py-8 text-center text-sm text-destructive">
                            {forError}
                        </CardContent>
                    </Card>
                )}

                {!loading && !forError && (
                    <AlertsList
                        alerts={forFilters}
                        disable={disable}
                        enable={enable}
                        onToggleNotificationSilence={toggleAlertNotificationSilence}
                        globalInAppNotificationsEnabled={inAppNotificationsEnabled === true}
                        info={handlingInfo}
                        onDelete={handlingDelete}
                    />
                )}
            </div>

            <Dialog open={forInfo !== null} onOpenChange={(change) => !change && setForInfo(null)}>
                <DialogContent className="sm:max-w-md">
                    <DialogHeader>
                        <DialogTitle>{forInfo?.title}</DialogTitle>

                        <DialogDescription asChild>
                            <div className="flex flex-wrap items-center gap-2 pt-1">
                                {forInfo && (
                                    <>
                                        <span className={`${TAG} text-muted-foreground`}>
                                            {" "}
                                            {TYPE[forInfo.alertType]}{" "}
                                        </span>

                                        <span
                                            className={`${TAG} ${SEVERITY_COLOURS[forInfo.severity]}`}
                                        >
                                            {" "}
                                            {forInfo.severity}{" "}
                                        </span>

                                        <span
                                            className={`${TAG} ${COLOURS_FOR_STATUS[forInfo.status]}`}
                                        >
                                            {" "}
                                            {STATUS_LABELS[forInfo.status]}{" "}
                                        </span>
                                    </>
                                )}
                            </div>
                        </DialogDescription>
                    </DialogHeader>

                    <div className="space-y-3 text-sm">
                        <div>
                            <p className="font-medium text-foreground">Message</p>
                            <p className="text-muted-foreground">{forInfo?.message}</p>
                        </div>

                        <div>
                            <p className="font-medium text-foreground">Created</p>
                            <p className="text-muted-foreground">
                                {forInfo?.createdAt
                                    ? new Date(forInfo.createdAt).toLocaleString()
                                    : "-"}
                            </p>
                        </div>
                    </div>

                    <DialogFooter>
                        <Button
                            type="button"
                            variant="outline"
                            onClick={toggleInfoAlertNotifications}
                        >
                            {forInfo?.inAppNotificationsSilenced
                                ? "Unmute in-app notifications"
                                : "Mute in-app notifications"}
                        </Button>

                        <Button type="button" variant="outline" onClick={() => setForInfo(null)}>
                            Close
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            <AlertDialog
                open={deleteAlert !== null}
                onOpenChange={(change) => !change && setDeleteAlert(null)}
            >
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>Delete alert?</AlertDialogTitle>

                        <AlertDialogDescription>
                            This will permanently delete the &quot;{deleteAlert?.title}&quot; alert.
                            This action cannot be undone.
                        </AlertDialogDescription>
                    </AlertDialogHeader>

                    <AlertDialogFooter>
                        <AlertDialogCancel>Cancel</AlertDialogCancel>
                        <AlertDialogAction onClick={confirmingDelete} className = "bg-destructive hover:bg-destructive/90" variant = "destructive">Delete</AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </div>
    );
}
