import { create } from "zustand";
import type { Alert } from "@/features/alerts/types/alertTypes";

interface AlertStore {
    alerts: Alert[];

    setAlerts: (alerts: Alert[]) => void;
    upsertAlert: (incoming: Alert) => void;
    setStatus: (alertId: string, status: Alert["status"]) => void;
}

// This function determines which of two alerts is newer.
function byRecencyDescending(alertA: Alert, alertB: Alert): number {
    let timeA = 0;
    if (alertA.lastSeen) {
        timeA = new Date(alertA.lastSeen).getTime();
    } else if (alertA.createdAt) {
        timeA = new Date(alertA.createdAt).getTime();
    }

    let timeB = 0;
    if (alertB.lastSeen) {
        timeB = new Date(alertB.lastSeen).getTime();
    } else if (alertB.createdAt) {
        timeB = new Date(alertB.createdAt).getTime();
    }

    return timeB - timeA;
}

function sortAlerts(alerts: Alert[]): Alert[] {
    const safeCopy = [...alerts];

    return safeCopy.sort(byRecencyDescending);
}

export const useAlertStore = create<AlertStore>((set) => ({
    alerts: [],

    setAlerts: (newAlerts) => {
        set({ alerts: sortAlerts(newAlerts) });
    },

    upsertAlert: (incomingAlert) => {
        set((state) => {
            const canonicalKey = incomingAlert.canonicalKey?.trim();

            // Search for the alert in our current state
            let existingIndex = -1;

            if (canonicalKey) {
                existingIndex = state.alerts.findIndex(
                    (alert) => alert.canonicalKey === canonicalKey
                );
            } else {
                existingIndex = state.alerts.findIndex(
                    (alert) => alert.alertId === incomingAlert.alertId
                );
            }

            // Alert was not found
            if (existingIndex === -1) {
                // Put the new alert at the front, followed by all old alerts
                const newArray = [incomingAlert, ...state.alerts];

                return {
                    alerts: sortAlerts(newArray),
                };
            }

            // Alert was found
            const updatedAlerts = [...state.alerts];

            updatedAlerts[existingIndex] = {
                ...updatedAlerts[existingIndex],
                ...incomingAlert,
            };

            return {
                alerts: sortAlerts(updatedAlerts),
            };
        });
    },

    // Change the status (e.g., ACTIVE or DISABLED) of a specific alert
    setStatus: (alertId, newStatus) => {
        set((state) => {
            const updatedAlerts = state.alerts.map((alert) => {
                // If it's the exact alert we are looking for, apply the new status
                if (alert.alertId === alertId) {
                    return { ...alert, status: newStatus };
                }

                // If it's any other alert, leave it completely alone
                return alert;
            });

            // Save the updated list back to the store
            return {
                alerts: updatedAlerts,
            };
        });
    },
}));
