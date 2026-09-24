import apiClient from "@/lib/fetch/api-client";

import type { Alert, TypeForAlerts } from "@/features/alerts/types/alertTypes";

export const fetchAlerts = async (typeForAlert?: TypeForAlerts): Promise<Alert[]> => {
    const forPath = typeForAlert
        ? `/alerts?alertType=${encodeURIComponent(typeForAlert)}`
        : "/alerts";

    return apiClient<Alert[]>(forPath, { method: "GET" });
};

export const disableAlert = async (alertId: string): Promise<void> => {
    return apiClient<void>(`/alerts/${alertId}/disable`, { method: "POST" });
};

export const enableAlert = async (alertId: string): Promise<void> => {
    return apiClient<void>(`/alerts/${alertId}/enable`, { method: "POST" });
};
