import apiClient from "@/lib/fetch/api-client";
import { TimeWindowPreset } from "../types/timewindow";

// Type is string | null to account for zustand get() returning string | null, this function called in store when
// preset is set.
export async function setDashboardPresetTimeWindow(
    preset: TimeWindowPreset,
    dashboardId: string | null
) {
    if (!dashboardId) {
        return;
    }
    try {
        apiClient(`/dashboards/${dashboardId}/window`, {
            method: "POST",
            body: JSON.stringify({ newTime: preset }),
        });
    } catch (error) {
        if (error instanceof Error) {
            console.error(error.message);
        }
    }
}
