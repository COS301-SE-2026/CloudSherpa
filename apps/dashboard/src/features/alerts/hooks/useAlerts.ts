"use client";

import { useCallback, useEffect, useState } from "react";
import { disableAlert, enableAlert, fetchAlerts } from "@/features/alerts/alerts";
import { useAlertStream } from "@/features/alerts/services/sse/alert-stream";
import type { Alert, TypeForAlerts } from "@/features/alerts/types/alertTypes";

interface AlertsResult {
    alerts: Alert[];
    loading: boolean;
    forError: string | null;
    streamError: string | null;
    refreshing: () => Promise<void>;
    disable: (alertId: string) => Promise<void>;
    enable: (alertId: string) => Promise<void>;
}

function upsertAlert(previousAlerts: Alert[], incomingAlert: Alert): Alert[] {
    const canonicalKey = incomingAlert.canonicalKey?.trim();

    // Try to find the position (index) of the alert in our existing list
    let existingIndex = -1;

    // If a canonicalKey exists, look for a match using that.
    // Otherwise, fall back to searching by the standard alertId
    if (canonicalKey) {
        existingIndex = previousAlerts.findIndex((alert) => alert.canonicalKey === canonicalKey);
    } else {
        existingIndex = previousAlerts.findIndex(
            (alert) => alert.alertId === incomingAlert.alertId
        );
    }

    // The alert is brand new (not found in the list)
    if (existingIndex === -1) {
        // Return a new array with the incoming alert placed at the very top,
        // followed by all the previous alerts after it
        return [incomingAlert, ...previousAlerts];
    }

    // The alert already exists, so we must merge the new data into it.
    const updatedAlerts = [...previousAlerts];

    updatedAlerts[existingIndex] = {
        ...updatedAlerts[existingIndex],
        ...incomingAlert,
    };

    return updatedAlerts;
}

export function useAlerts(typeForAlert?: TypeForAlerts): AlertsResult {
    const [alerts, setAlerts] = useState<Alert[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [forError, setForError] = useState<string | null>(null);

    // This listens for new alerts pushed from the server in real-time.
    const handleIncomingAlert = useCallback(
        (incoming: Alert) => {
            // Ignore alerts that don't match the specific type we are looking for
            if (typeForAlert && incoming.alertType !== typeForAlert) {
                return;
            }

            // Safely merge the new alert into our existing list using our upsert function
            setAlerts((previous) => upsertAlert(previous, incoming));
        },
        [typeForAlert]
    );

    const { error: streamError } = useAlertStream(handleIncomingAlert);

    const refreshing = useCallback(async () => {
        setLoading(true);
        setForError(null);

        try {
            const forData = await fetchAlerts(typeForAlert);
            setAlerts(forData);
        } catch (error) {
            setForError(error instanceof Error ? error.message : "Failed to load alerts");
        } finally {
            setLoading(false);
        }
    }, [typeForAlert]);

    useEffect(() => {
        let cancelled = false;

        (async () => {
            try {
                const forData = await fetchAlerts(typeForAlert);
                if (!cancelled) setAlerts(forData);
            } catch (error) {
                if (!cancelled) {
                    setForError(error instanceof Error ? error.message : "Failed to load alerts");
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        })();

        return () => {
            cancelled = true;
        };
    }, [typeForAlert]);

    const disable = useCallback(async (alertId: string) => {
        await disableAlert(alertId);
        setAlerts((previous) =>
            previous.map((alert) =>
                alert.alertId === alertId ? { ...alert, status: "DISABLED" } : alert
            )
        );
    }, []);

    const enable = useCallback(async (alertId: string) => {
        await enableAlert(alertId);
        setAlerts((previous) =>
            previous.map((alert) =>
                alert.alertId === alertId ? { ...alert, status: "ACTIVE" } : alert
            )
        );
    }, []);

    return {
        alerts,
        loading,
        forError,
        streamError: streamError?.message ?? null,
        refreshing,
        disable,
        enable,
    };
}
