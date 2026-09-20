import apiClient from "@/lib/fetch/api-client";

import type {Alert, TypeForAlerts} from "@/features/alerts/types/alertTypes";

export const fetchAlerts = async (typeForAlert?: TypeForAlerts) : Promise<Alert[]> => {
    const forPath = typeForAlert ? `/alerts?alertType=${encodeURIComponent(typeForAlert)}` : "/alerts";

    return apiClient<Alert[]>(forPath, {method : "GET"});
};

export const acknowledgeAlert = async (alertId : string) : Promise<void> => {
    return apiClient<void>(`/alerts/${alertId}/acknowledge`, {method : "POST"});
};

export const dismissAlert = async (alertId : string) : Promise<void> => {
    return apiClient<void>(`/alerts/${alertId}/dismiss`, {method : "POST"});
};