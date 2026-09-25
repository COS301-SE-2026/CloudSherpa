"use client";

import { useCallback, useEffect } from "react";
import type { Alert } from "@/features/alerts/types/alertTypes";
import { useAlertStream } from "@/features/alerts/services/sse/alert-stream";
import { useAlertStore } from "@/features/alerts/stores/alert-store";
import { fetchAlerts } from "@/features/alerts/alerts";
import { useAuthContext } from "@/features/authentication/providers/AuthContext";

export function AlertStreamBridge() {
    const { isAuthReady, isAuthenticated } = useAuthContext();
    const upsertAlert = useAlertStore((state) => state.upsertAlert);
    const setAlerts = useAlertStore((state) => state.setAlerts);

    const onAlert = useCallback(
        (incoming: Alert) => {
            upsertAlert(incoming);
        },
        [upsertAlert]
    );

    useAlertStream(onAlert);

    useEffect(() => {
        if (!isAuthReady || !isAuthenticated) {
            return;
        }

        queueMicrotask(() => {
            void (async () => {
                try {
                    const data = await fetchAlerts();
                    setAlerts(data);
                } catch {}
            })();
        });
    }, [isAuthReady, isAuthenticated, setAlerts]);

    return null;
}
