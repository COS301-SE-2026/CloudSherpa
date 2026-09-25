"use client";

import { useCallback, useEffect } from "react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import type { Alert } from "@/features/alerts/types/alertTypes";
import { useAlertStream } from "@/features/alerts/services/sse/alert-stream";
import { useAlertStore } from "@/features/alerts/stores/alert-store";
import { fetchAlerts } from "@/features/alerts/alerts";
import { useAuthContext } from "@/features/authentication/providers/AuthContext";

function findExistingAlert(previous: Alert[], incoming: Alert): Alert | undefined {
    const canonical = incoming.canonicalKey?.trim();

    if (canonical) {
        return previous.find((alert) => alert.canonicalKey === canonical);
    }

    return previous.find((alert) => alert.alertId === incoming.alertId);
}

function shouldShowAlertToast(existing: Alert | undefined, incoming: Alert): boolean {
    if (incoming.status !== "ACTIVE") {
        return false;
    }

    if (!existing) {
        return true;
    }

    return existing.severity === "WARNING" && incoming.severity === "CRITICAL";
}

export function AlertStreamBridge() {
    const router = useRouter();
    const { isAuthReady, isAuthenticated } = useAuthContext();
    const upsertAlert = useAlertStore((state) => state.upsertAlert);
    const setAlerts = useAlertStore((state) => state.setAlerts);

    const onAlert = useCallback(
        (incoming: Alert) => {
            const previousAlerts = useAlertStore.getState().alerts;
            const existing = findExistingAlert(previousAlerts, incoming);
            const showToast = shouldShowAlertToast(existing, incoming);

            upsertAlert(incoming);

            if (!showToast) {
                return;
            }

            const escalated = Boolean(
                existing && existing.severity === "WARNING" && incoming.severity === "CRITICAL"
            );

            const toastTitle = escalated ? "Alert severity CRITICAL" : "New alert";
            const toastDescription = incoming.title || "A new alert needs your attention.";

            toast.warning(toastTitle, {
                description: toastDescription,
                position: "top-center",
                duration: 9000,
                action: {
                    label: "View alert",
                    onClick: () => router.push("/alerts"),
                },
                id: `alert-${incoming.alertId}-${incoming.severity}-${incoming.lastSeen ?? incoming.createdAt ?? ""}`,
            });
        },
        [router, upsertAlert]
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
