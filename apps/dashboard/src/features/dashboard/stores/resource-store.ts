import { create } from "zustand";
import apiClient from "@/lib/fetch/api-client";

export type ResourceNameStore = {
    resourcesById: Record<string, string>,
    fetchResources: () => Promise<void>
}

export const useResourceNameStore = create<ResourceNameStore>(
    (set) => ({
        resourcesById: {},
        fetchResources: async () => {
        try {
            const fetched = await apiClient("/analytics/resource-names");

            const fetchedResources =
                fetched && typeof fetched === "object" && !Array.isArray(fetched)
                    ? (fetched as Record<string, string>)
                    : {};

            set((state) => ({
                resourcesById: {
                    ...state.resourcesById,
                    ...fetchedResources
                }
            }));
        } catch {
            set((state) => ({
                resourcesById: { ...state.resourcesById }
            }));
        }
    }
    }
))