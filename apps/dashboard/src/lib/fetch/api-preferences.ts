import apiClient from "@/lib/fetch/api-client";

export interface ThemeResponse {
    theme: "light" | "dark";
}

export interface AlertNotificationsResponse {
    enabled: boolean;
}

export const fetchUserTheme = async (): Promise<ThemeResponse> => {
    return apiClient<ThemeResponse>("/preferences/theme", {
        method: "GET",
    });
};

export const updateUserTheme = async (theme: "light" | "dark"): Promise<void> => {
    await apiClient<void>("/preferences/theme", {
        method: "POST",
        body: JSON.stringify({ theme }),
    });
};

export const fetchAlertNotifications = (): Promise<AlertNotificationsResponse> => {
    return apiClient<AlertNotificationsResponse>("/preferences/alert-notifications", {
        method: "GET",
    });
};

export const updateAlertNotifications = (enabled: boolean): Promise<void> => {
    return apiClient<void>("/preferences/alert-notifications", {
        method: "POST",
        body: JSON.stringify({ enabled }),
    });
};
