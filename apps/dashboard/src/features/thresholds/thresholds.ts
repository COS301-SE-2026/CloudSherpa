import apiClient from "@/lib/fetch/api-client";

import type {
    CreateThresholdRequest,
    Threshold,
    UpdateThresholdRequest,
} from "@/features/thresholds/types/thresholdTypes";

export const fetchThresholds = async (resourceId?: string): Promise<Threshold[]> => {
    const forPath = resourceId
        ? `/thresholds?resourceId=${encodeURIComponent(resourceId)}`
        : "/thresholds";

    return apiClient<Threshold[]>(forPath, { method: "GET" });
};

export const addThreshold = async (forPayload: CreateThresholdRequest): Promise<Threshold> => {
    return apiClient<Threshold>("/thresholds", {
        method: "POST",
        body: JSON.stringify(forPayload),
    });
};

export const editThreshold = async (
    thresholdId: string,
    forPayload: UpdateThresholdRequest
): Promise<void> => {
    return apiClient<void>(`/thresholds/${thresholdId}`, {
        method: "PUT",
        body: JSON.stringify(forPayload),
    });
};

export const deleteThreshold = async (thresholdId: string): Promise<void> => {
    return apiClient<void>(`/thresholds/${thresholdId}`, { method: "DELETE" });
};
