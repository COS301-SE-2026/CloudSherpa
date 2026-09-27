"use client";

import { AlertTable } from "@/features/alerts/components/alertTable";
import type { Alert } from "@/features/alerts/types/alertTypes";

interface PropsForAlerts {
    alerts: Alert[];
    disable: (alertId: string) => Promise<void>;
    globalInAppNotificationsEnabled: boolean;
    enable: (alertId: string) => Promise<void>;
    info: (alert: Alert) => void;
    onDelete: (alert: Alert) => void;
    onToggleNotificationSilence: (alert: Alert) => Promise<void>;
}

export function AlertsList({
    alerts,
    disable,
    enable,
    onToggleNotificationSilence,
    globalInAppNotificationsEnabled,
    info,
    onDelete,
}: Readonly<PropsForAlerts>) {
    return (
        <AlertTable
            alerts={alerts}
            onToggle={async (alert, checked) => {
                if (checked) {
                    await enable(alert.alertId);
                } else {
                    await disable(alert.alertId);
                }

                return true;
            }}
            info={info}
            onDelete={onDelete}
            onToggleNotificationSilence={onToggleNotificationSilence}
            globalInAppNotificationsEnabled={globalInAppNotificationsEnabled}
        />
    );
}
