import { create } from "zustand";
import apiClient from "@/lib/fetch/api-client";

export type ResourceNameStore = {
    resources: Resource[];
    resourcesById: Record<string, string>;
    fetchResources: () => Promise<void>;
    reset: () => void;
};

export type Resource = {
    resourceId: string;
    resourceName: string;
};

export const useResourceNameStore = create<ResourceNameStore>((set) => ({
    resources: [],
    resourcesById: {},
    fetchResources: async () => {
        try {
            const fetched = await apiClient<Resource[]>("/analytics/resource-names");
            const resources = Array.isArray(fetched) ? fetched : [];
            const resourcesById = resources.reduce<Record<string, string>>((acc, resource) => {
                acc[resource.resourceId] = resource.resourceName;
                return acc;
            }, {});

            set(() => ({
                resources,
                resourcesById,
            }));
        } catch {
            set((state) => ({
                resources: state.resources,
                resourcesById: { ...state.resourcesById },
            }));
        }
    },
    reset: () => set({ resources: [], resourcesById: {} }),
}));
