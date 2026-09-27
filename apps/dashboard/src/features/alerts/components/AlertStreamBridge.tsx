"use client";

import { useCallback, useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";
import { toast } from "sonner";
import { TriangleAlertIcon } from "lucide-react";
import type { Alert } from "@/features/alerts/types/alertTypes";
import { fetchAlerts } from "@/features/alerts/alerts";
import { fetchAlertNotifications } from "@/lib/fetch/api-preferences";
import { useAlertStream } from "@/features/alerts/services/sse/alert-stream";
import { useAlertStore } from "@/features/alerts/stores/alert-store";

function findExistingAlert(previous: Alert[], incoming: Alert): Alert | undefined {
    const canonical = incoming.canonicalKey?.trim();

    if (canonical) {
        return previous.find((alert) => alert.canonicalKey === canonical);
    }

    return previous.find((alert) => alert.alertId === incoming.alertId);
}

function shouldShowAlertToast(
    existing: Alert | undefined,
    incoming: Alert,
    inAppNotificationsEnabled: boolean | null
): boolean {
    if (inAppNotificationsEnabled !== true || incoming.inAppNotificationsSilenced === true) {
        return false;
    }

    if (existing?.status === "DISABLED" || incoming.status !== "ACTIVE") {
        return false;
    }

    return !existing || (existing.severity === "WARNING" && incoming.severity === "CRITICAL");
}

export function AlertStreamBridge() {
    const router = useRouter();
    const pathname = usePathname();

    const upsertAlert = useAlertStore((state) => state.upsertAlert);
    const setAlerts = useAlertStore((state) => state.setAlerts);
    const inAppNotificationsEnabled = useAlertStore((state) => state.inAppNotificationsEnabled);
    const setInAppNotificationsEnabled = useAlertStore(
        (state) => state.setInAppNotificationsEnabled
    );

    const onAlert = useCallback(
        (incoming: Alert) => {
            const previousAlerts = useAlertStore.getState().alerts;
            const existing = findExistingAlert(previousAlerts, incoming);
            const showToast = shouldShowAlertToast(existing, incoming, inAppNotificationsEnabled);

            upsertAlert(incoming);

            if (pathname.startsWith("/alerts") || !showToast) {
                return;
            }

            const escalated = existing?.severity === "WARNING" && incoming.severity === "CRITICAL";

            toast(escalated ? "Alert severity CRITICAL" : "New alert", {
                description: incoming.title || "A new alert needs your attention.",
                icon: <TriangleAlertIcon className="size-4" />,
                position: "top-center",
                duration: 9000,
                className:
                    "bg-[var(--color-orange-300)]! text-[var(--color-orange-800)]! border-[var(--color-orange-400)]! dark:bg-[var(--color-orange-700)]! dark:text-[var(--color-orange-50)]! dark:border-[var(--color-orange-800)]!",
                classNames: {
                    actionButton:
                        "bg-[var(--color-orange-300)]! text-[var(--color-orange-800)]! hover:brightness-90! dark:bg-[var(--color-orange-700)]! dark:text-[var(--color-orange-50)]!",
                },
                action: {
                    label: "View alert",
                    onClick: () => router.push("/alerts"),
                },
                id: `alert-${incoming.alertId}-${incoming.severity}-${incoming.lastSeen ?? incoming.createdAt ?? ""}`,
            });
        },
        [inAppNotificationsEnabled, pathname, router, upsertAlert]
    );

    useAlertStream(onAlert);

    useEffect(() => {
        let cancelled = false;

        void fetchAlerts()
            .then((alerts) => {
                if (!cancelled) {
                    setAlerts(alerts);
                }
            })
            .catch(() => {});

        void fetchAlertNotifications()
            .then(({ enabled }) => {
                if (!cancelled) {
                    setInAppNotificationsEnabled(enabled);
                }
            })
            .catch(() => {
                if (!cancelled) {
                    setInAppNotificationsEnabled(true);
                }
            });

        return () => {
            cancelled = true;
        };
    }, [setAlerts, setInAppNotificationsEnabled]);

    return null;
}
