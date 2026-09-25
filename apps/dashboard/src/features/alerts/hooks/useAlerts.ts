"use client";

import { useCallback, useEffect, useState } from "react";
import { disableAlert, enableAlert, fetchAlerts } from "@/features/alerts/alerts";
import { useAlertStore } from "@/features/alerts/stores/alert-store";
import type { TypeForAlerts } from "@/features/alerts/types/alertTypes";

interface AlertsResult {
    alerts: ReturnType<typeof useAlertStore.getState>["alerts"];
    loading: boolean;
    forError: string | null;
    refreshing: () => Promise<void>;
    disable: (alertId: string) => Promise<void>;
    enable: (alertId: string) => Promise<void>;
}

export function useAlerts(typeForAlert?: TypeForAlerts): AlertsResult {
    const allAlerts = useAlertStore((state) => state.alerts);
    const setAlerts = useAlertStore((state) => state.setAlerts);
    const setStatus = useAlertStore((state) => state.setStatus);

    const [loading, setLoading] = useState<boolean>(true);
    const [forError, setForError] = useState<string | null>(null);

    const alerts = typeForAlert
        ? allAlerts.filter((alert) => alert.alertType === typeForAlert)
        : allAlerts;

    const refreshing = useCallback(async () => {
        setLoading(true);
        setForError(null);

        try {
            const data = await fetchAlerts(typeForAlert);
            setAlerts(data);
        } catch (error) {
            setForError(error instanceof Error ? error.message : "Failed to load alerts");
        } finally {
            setLoading(false);
        }
    }, [setAlerts, typeForAlert]);

    useEffect(() => {
        let cancelled = false;

        (async () => {
            try {
                const data = await fetchAlerts(typeForAlert);
                if (!cancelled) {
                    setAlerts(data);
                }
            } catch (error) {
                if (!cancelled) {
                    setForError(error instanceof Error ? error.message : "Failed to load alerts");
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        })();

        return () => {
            cancelled = true;
        };
    }, [setAlerts, typeForAlert]);

    const disable = useCallback(
        async (alertId: string) => {
            await disableAlert(alertId);
            setStatus(alertId, "DISABLED");
        },
        [setStatus]
    );

    const enable = useCallback(
        async (alertId: string) => {
            await enableAlert(alertId);
            setStatus(alertId, "ACTIVE");
        },
        [setStatus]
    );

    return {
        alerts,
        loading,
        forError,
        refreshing,
        disable,
        enable,
    };
}
