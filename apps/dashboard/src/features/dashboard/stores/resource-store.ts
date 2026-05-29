import { create } from "zustand";
import apiClient from "@/lib/fetch/api-client";

type ResourceNameStore = {
    resourcesById: Record<string, string>,
    fetchResources: () => Promise<void>
}

export const useResourceNameStore = create<ResourceNameStore>(
    (set) => ({
        resourcesById: {},
        fetchResources: async () => {
            const fetchedResources: Record<string, string> = await apiClient("/analytics/resource-names");
            
            set((state) => ({
                resourcesById: {
                    ...state.resourcesById,
                    ...fetchedResources
                }
            }));
        }
    })
)