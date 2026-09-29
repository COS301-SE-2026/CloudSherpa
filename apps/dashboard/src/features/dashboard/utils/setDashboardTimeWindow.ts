import apiClient from "@/lib/fetch/api-client";
import { TimeWindowPreset } from "../types/timewindow";

// Type is string | null to account for zustand get() returning string | null, this function called in store when
// preset is set.
export async function setDashboardPresetTimeWindow(
    preset: TimeWindowPreset,
    dashboardId: string | null,
    timeFrom: string | null,
    timeTo: string | null
) {
    if (!dashboardId) {
        return;
    }
    try {
        await apiClient(`/dashboards/${dashboardId}/window`, {
            method: "PATCH",
            body: JSON.stringify({ newTime: preset, from: timeFrom, to: timeTo }),
        });
    } catch (error) {
        if (error instanceof Error) {
            console.error(error.message);
        }
    }
}
