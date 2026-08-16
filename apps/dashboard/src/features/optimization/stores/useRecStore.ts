import { create } from "zustand";
import { Recommendation, RecommendationGroup } from "@/features/optimization/types/recommendations";
import { getAwsAccountConnections, getAwsAccountResources } from "@/lib/fetch/aws-connection-api";
import {
    getOptimizationRecommendations,
    acknowledgeRecommendation,
    dismissRecommendation,
    applyRecommendation,
} from "@/lib/fetch/api-optimization";

interface RecStore {
    recommendationGroups: RecommendationGroup[];
    isLoading: boolean;
    fetchRecGroups: () => Promise<void>;
    acknowledgeRec: (recommendationId: string) => Promise<void>;
    dismissRec: (recommendationId: string) => Promise<void>;
    applyRec: (recommendationId: string) => Promise<void>;
}

export const useRecStore = create<RecStore>((set, get) => ({
    recommendationGroups: [],
    isLoading: false,

    fetchRecGroups: async () => {
        set({ isLoading: true });

        try {
            const [fetchedRecommendations, fetchedConnections] = await Promise.all([
                getOptimizationRecommendations(),
                getAwsAccountConnections(),
            ]);

            const tempAccountNameMap: Record<string, string> = {};
            fetchedConnections.forEach((account) => {
                tempAccountNameMap[account.id] = account.displayName;
            });

            const resourcePromises = fetchedConnections.map((account) =>
                getAwsAccountResources(account.id).then((resources) => ({
                    accountId: account.id,
                    resources,
                }))
            );
            const accountResources = await Promise.all(resourcePromises);

            const resourceToAccountMap: Record<string, string> = {};
            const tempResourceNameMap: Record<string, string> = {};

            accountResources.forEach(({ accountId, resources }) => {
                resources.forEach((resource) => {
                    resourceToAccountMap[resource.id] = accountId;
                    tempResourceNameMap[resource.id] = resource.resourceName;
                });
            });

            const groupedMap: Record<string, Recommendation[]> = {};

            fetchedRecommendations.forEach((rec) => {
                const accountId = resourceToAccountMap[rec.resourceId] || "unassigned";

                const enrichedRec: Recommendation = {
                    ...rec,
                    resourceDisplayName: tempResourceNameMap[rec.resourceId] || undefined,
                };

                if (!groupedMap[accountId]) {
                    groupedMap[accountId] = [];
                }
                groupedMap[accountId].push(enrichedRec);
            });

            const finalGroups: RecommendationGroup[] = Object.entries(groupedMap).map(
                ([accountId, recommendations]) => ({
                    accountId: accountId === "unassigned" ? null : accountId,
                    displayName: tempAccountNameMap[accountId] || null,
                    recommendations,
                })
            );

            set(() => ({
                recommendationGroups: finalGroups,
                isLoading: false,
            }));
        } catch {
            set({ isLoading: false });
        }
    },

    acknowledgeRec: async (recommendationId: string) => {
        try {
            await acknowledgeRecommendation(recommendationId);
            await get().fetchRecGroups();
        } catch (error) {
            console.error("Failed to acknowledge recommendation:", error);
        }
    },

    dismissRec: async (recommendationId: string) => {
        try {
            await dismissRecommendation(recommendationId);
            await get().fetchRecGroups();
        } catch (error) {
            console.error("Failed to dismiss recommendation:", error);
        }
    },

    applyRec: async (recommendationId: string) => {
        try {
            await applyRecommendation(recommendationId);
            await get().fetchRecGroups();
        } catch (error) {
            console.error("Failed to apply recommendation:", error);
        }
    },
}));
