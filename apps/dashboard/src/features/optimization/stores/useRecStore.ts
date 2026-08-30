import { create } from "zustand";
import {
    Recommendation,
    RecommendationGroup,
    RecommendationSummary,
} from "@/features/optimization/types/recommendations";
import { getAwsAccountConnections, getAwsAccountResources } from "@/lib/fetch/aws-connection-api";
import {
    getOptimizationRecommendations,
    dismissRecommendation,
    applyRecommendation,
    getRecommendationSummary,
    reEnableRecommendation,
} from "@/lib/fetch/api-optimization";

interface RecStore {
    recommendationGroups: RecommendationGroup[];
    summary: RecommendationSummary | null;
    isLoading: boolean;
    failedLoading: boolean;
    failedLoadingMessage: string;
    reEnableRec: (recommendationId: string) => Promise<void>;
    fetchRecGroups: () => Promise<void>;
    fetchSummary: () => Promise<void>;
    dismissRec: (recommendationId: string) => Promise<void>;
    applyRec: (recommendationId: string) => Promise<void>;
}

export const useRecStore = create<RecStore>((set, get) => ({
    recommendationGroups: [],
    summary: null,
    isLoading: true,
    failedLoading: false,
    failedLoadingMessage: "",

    fetchSummary: async () => {
        try {
            const summary = await getRecommendationSummary();
            set({ summary, failedLoading: false });
        } catch (error) {
            console.error("Failed to fetch recommendation summary:", error);
            set({
                failedLoading: true,
                failedLoadingMessage: "Failed to fetch summary of recommendations",
            });
        }
    },

    fetchRecGroups: async () => {
        if (get().recommendationGroups.length === 0) {
            set({ isLoading: true });
        }

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
                failedLoading: false,
            }));
        } catch {
            set({
                isLoading: false,
                failedLoading: true,
                failedLoadingMessage: "Failed to fetch recommendations",
            });
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

    reEnableRec: async (recommendationId: string) => {
        try {
            await reEnableRecommendation(recommendationId);
            await get().fetchRecGroups();
        } catch (error) {
            console.error("Failed to re-enable recommendation:", error);
        }
    },
}));
