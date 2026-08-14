import { create } from "zustand";
import { Recommendation, RecommendationGroup } from "@/features/optimization/types/recommendations";
import { getAwsAccountConnections, getAwsAccountResources } from "@/lib/fetch/aws-connection-api";

const now = new Date();
const nextWeek = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000);
const twoDaysAgo = new Date(now.getTime() - 2 * 24 * 60 * 60 * 1000);

const mockRecommendations: Recommendation[] = [
    {
        recommendation_id: "123455",
        resource_id: "e302b553-94cd-4c8f-9fce-dc179ac1a39a",
        provider: "AWS",
        rule_id: "rule-suspend-idle-resources",
        action_type: "SUSPEND",
        status: "ACTIVE",
        evidence: 18,
        created_at: twoDaysAgo,
        update_at: now,
        expires_at: nextWeek,
    },
    {
        recommendation_id: "b2c3d4e5",
        resource_id: "a8e4b986-51c7-4b39-8d14-e9ad9db56c88",
        provider: "AWS",
        rule_id: "rule-suspend-idle-resources",
        action_type: "DOWNSIZE",
        status: "ACTIVE",
        evidence: 12,
        created_at: twoDaysAgo,
        update_at: now,
        expires_at: nextWeek,
    },
];

interface RecStore {
    recommendationGroups: RecommendationGroup[];
    isLoading: boolean;
    fetchRecGroups: () => void;
}

export const useRecStore = create<RecStore>((set) => ({
    recommendationGroups: [],
    isLoading: false,

    fetchRecGroups: async () => {
        try {
            const fetchedRecommendations = mockRecommendations as Recommendation[];

            const fetchedConnections = await getAwsAccountConnections();

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
                const accountId = resourceToAccountMap[rec.resource_id] || "unassigned";

                const enrichedRec: Recommendation = {
                    ...rec,
                    resource_displayName: tempResourceNameMap[rec.resource_id] || undefined,
                };

                if (!groupedMap[accountId]) {
                    groupedMap[accountId] = [];
                }
                groupedMap[accountId].push(enrichedRec);
            });

            const finalGroups: RecommendationGroup[] = Object.entries(groupedMap).map(
                ([accountId, recommendations]) => ({
                    accountId: accountId === "unassigned" ? null : accountId,
                    displayName: tempAccountNameMap[accountId] || null, // Inject account name!
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
}));
