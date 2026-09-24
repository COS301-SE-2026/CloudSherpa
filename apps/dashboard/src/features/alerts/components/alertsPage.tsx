"use client";

import { useMemo, useState } from "react";
import { Card, CardContent } from "@/components/atoms/card";
import { Input } from "@/components/atoms/input";
import { AlertsList } from "@/features/alerts/components/alertsList";
import { useAlerts } from "@/features/alerts/hooks/useAlerts";
import type { TypeForAlerts } from "@/features/alerts/types/alertTypes";

const FILTERS: Array<{ value: "ALL" | TypeForAlerts; label: string }> = [
    { value: "ALL", label: "All" },
    { value: "THRESHOLD", label: "Threshold" },
    { value: "ANOMALY", label: "Anomaly" },
    { value: "BUDGET", label: "Budget" },
];

export function AlertsPage() {
    const { alerts, loading, forError, disable, enable } = useAlerts();

    const [search, setSearch] = useState("");

    const [filterType, setFilterType] = useState<"ALL" | TypeForAlerts>("ALL");

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

    return (
        <div className="mx-auto max-w-6xl px-6 py-8">
            <header className="mb-6">
                <h1 className="mb-4 text-2xl font-semibold tracking-tight text-foreground">
                    {" "}
                    Alerts{" "}
                </h1>

                <div className="mb-4 flex flex-wrap items-center gap-3">
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
                                        ? "bg-primary text-foreground shadow-sm"
                                        : "text-muted-foreground hover:text-foreground")
                                }
                            >
                                {filter.label}
                            </button>
                        ))}
                    </div>
                </div>

                <p className="text-sm text-muted-foreground">
                    {" "}
                    {activeCount} active of {alerts.length} alerts{" "}
                </p>
            </header>

            {loading && (
                <Card>
                    <CardContent className="py-8 text-center text-sm text-muted-foreground">
                        {" "}
                        Loading alerts{" "}
                    </CardContent>
                </Card>
            )}

            {forError && (
                <Card>
                    <CardContent className="py-8 text-center text-sm text-destructive">
                        {" "}
                        {forError}{" "}
                    </CardContent>
                </Card>
            )}

            {!loading && !forError && (
                <AlertsList alerts={forFilters} disable={disable} enable={enable} />
            )}
        </div>
    );
}
