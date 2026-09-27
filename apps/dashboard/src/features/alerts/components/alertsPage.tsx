"use client";

import { useMemo, useState } from "react";
import { Card, CardContent } from "@/components/atoms/card";
import { Input } from "@/components/atoms/input";
import { AlertsList } from "@/features/alerts/components/alertsList";
import { useAlerts } from "@/features/alerts/hooks/useAlerts";
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
import { Button } from "@/components/atoms/button";
import { toast } from "sonner";

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
    const disabledCount = alerts.filter((alert) => alert.status === "DISABLED").length;

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
                        info={handlingInfo}
                        onDelete={handlingDelete}
                    />
                )}
            </div>

            <Dialog open={forInfo !== null} onOpenChange={(change) => !change && setForInfo(null)}>
                <DialogContent className="sm:max-w-md">
                    <DialogHeader>
                        <DialogTitle> {forInfo?.title} </DialogTitle>

                        <DialogDescription>
                            {" "}
                            {forInfo?.alertType} {forInfo?.severity} {forInfo?.status}{" "}
                        </DialogDescription>
                    </DialogHeader>

                    <div className="space-y-3 text-sm">
                        <div>
                            <p className="font-medium text-foreground"> Message </p>

                            <p className="text-muted-foreground"> {forInfo?.message} </p>
                        </div>

                        <div>
                            <p className="font-medium text-foreground"> Created </p>

                            <p className="text-muted-foreground">
                                {" "}
                                {forInfo?.createdAt
                                    ? new Date(forInfo.createdAt).toLocaleString()
                                    : "-"}{" "}
                            </p>
                        </div>

                        {forInfo?.payload && (
                            <div>
                                <p className="font-medium text-foreground"> Payload </p>

                                <pre className="mt-1 max-h-64 overflow-auto rounded-md bg-muted p-3 text-xs">
                                    {" "}
                                    {JSON.stringify(forInfo.payload, null, 2)}{" "}
                                </pre>
                            </div>
                        )}
                    </div>

                    <DialogFooter>
                        <Button type="button" variant="outline" onClick={() => setForInfo(null)}>
                            {" "}
                            Close{" "}
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
                        <AlertDialogTitle> Delete alert? </AlertDialogTitle>

                        <AlertDialogDescription>
                            {" "}
                            This will permanently delete the &quot;{deleteAlert?.title}&quot; alert.
                            This action can not be undone.{" "}
                        </AlertDialogDescription>
                    </AlertDialogHeader>

                    <AlertDialogFooter>
                        <AlertDialogCancel> Cancel </AlertDialogCancel>

                        <AlertDialogAction onClick={confirmingDelete}> Delete </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </div>
    );
}
