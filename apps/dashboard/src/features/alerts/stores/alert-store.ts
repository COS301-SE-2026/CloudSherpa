import { create } from "zustand";
import type { Alert } from "@/features/alerts/types/alertTypes";

interface AlertStore {
    alerts: Alert[];
    inAppNotificationsEnabled: boolean | null;

    setAlerts: (alerts: Alert[]) => void;
    upsertAlert: (incoming: Alert) => void;
    setStatus: (alertId: string, status: Alert["status"]) => void;
    setNotificationSilence: (alertId: string, silenced: boolean) => void;
    setInAppNotificationsEnabled: (enabled: boolean) => void;
    removeAlert: (alertId: string) => void;
}

function byRecencyDescending(alertA: Alert, alertB: Alert): number {
    const timeA = new Date(alertA.lastSeen ?? alertA.createdAt ?? 0).getTime();
    const timeB = new Date(alertB.lastSeen ?? alertB.createdAt ?? 0).getTime();

    return timeB - timeA;
}

function sortAlerts(alerts: Alert[]): Alert[] {
    return [...alerts].sort(byRecencyDescending);
}

export const useAlertStore = create<AlertStore>((set) => ({
    alerts: [],
    inAppNotificationsEnabled: null,

    setAlerts: (alerts) => {
        set({ alerts: sortAlerts(alerts) });
    },

    upsertAlert: (incomingAlert) => {
        set((state) => {
            const canonicalKey = incomingAlert.canonicalKey?.trim();

            const existingIndex = canonicalKey
                ? state.alerts.findIndex((alert) => alert.canonicalKey === canonicalKey)
                : state.alerts.findIndex((alert) => alert.alertId === incomingAlert.alertId);

            if (existingIndex === -1) {
                return { alerts: sortAlerts([incomingAlert, ...state.alerts]) };
            }

            const alerts = [...state.alerts];
            alerts[existingIndex] = { ...alerts[existingIndex], ...incomingAlert };

            return { alerts: sortAlerts(alerts) };
        });
    },

    setStatus: (alertId, status) => {
        set((state) => ({
            alerts: state.alerts.map((alert) =>
                alert.alertId === alertId ? { ...alert, status } : alert
            ),
        }));
    },

    setNotificationSilence: (alertId, silenced) => {
        set((state) => ({
            alerts: state.alerts.map((alert) =>
                alert.alertId === alertId
                    ? { ...alert, inAppNotificationsSilenced: silenced }
                    : alert
            ),
        }));
    },

    setInAppNotificationsEnabled: (enabled) => {
        set({ inAppNotificationsEnabled: enabled });
    },

    removeAlert: (alertId) => {
        set((state) => ({
            alerts: state.alerts.filter((alert) => alert.alertId !== alertId),
        }));
    },
}));
