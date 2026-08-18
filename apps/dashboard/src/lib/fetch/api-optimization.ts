import {
    Recommendation,
    RecommendationSummary,
} from "@/features/optimization/types/recommendations";

const now = new Date();
const nextWeek = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000);
const twoDaysAgo = new Date(now.getTime() - 2 * 24 * 60 * 60 * 1000);

const mockRecommendations: Recommendation[] = [
    {
        recommendationId: "123455",
        resourceId: "e302b553-94cd-4c8f-9fce-dc179ac1a39a",
        resourceType: "compute_instance",
        provider: "AWS",
        ruleId: "rule-suspend-idle-resources",
        actionType: "SUSPEND",
        status: "ACTIVE",
        evidence: {
            cpuPercentP95_4d: 18,
            completenessRatio: 0.99,
        },
        createdAt: twoDaysAgo,
        updatedAt: now,
        expiresAt: nextWeek,
    },
    {
        recommendationId: "b2c3d4e5",
        resourceId: "a8e4b986-51c7-4b39-8d14-e9ad9db56c88",
        resourceType: "compute_instance",
        provider: "AWS",
        ruleId: "rule-downsize-underutilized",
        actionType: "DOWNSIZE",
        status: "ACTIVE",
        evidence: {
            cpuPercentP95_4d: 12,
            completenessRatio: 0.95,
        },
        createdAt: twoDaysAgo,
        updatedAt: now,
        expiresAt: nextWeek,
    },
];

export const getOptimizationRecommendations = async (): Promise<Recommendation[]> => {
    return new Promise((resolve) => {
        setTimeout(() => resolve(mockRecommendations), 500);
    });
};

export const getRecommendationSummary = async (): Promise<RecommendationSummary> => {
    return new Promise((resolve) => {
        setTimeout(
            () =>
                resolve({
                    total: 2,
                    active: 2,
                    acknowledged: 0,
                    dismissed: 0,
                    applied: 0,
                    byActionType: { SUSPEND: 1, DOWNSIZE: 1 },
                }),
            500
        );
    });
};

export const acknowledgeRecommendation = async (
    recommendationId: string
): Promise<Recommendation> => {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            const recIndex = mockRecommendations.findIndex(
                (r) => r.recommendationId === recommendationId
            );
            if (recIndex !== -1) {
                mockRecommendations[recIndex].status = "ACKNOWLEDGED";
                resolve({ ...mockRecommendations[recIndex] });
            } else {
                reject(new Error("Recommendation not found"));
            }
        }, 500);
    });
};

export const dismissRecommendation = async (recommendationId: string): Promise<Recommendation> => {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            const recIndex = mockRecommendations.findIndex(
                (r) => r.recommendationId === recommendationId
            );
            if (recIndex !== -1) {
                mockRecommendations[recIndex].status = "DISMISSED";
                resolve({ ...mockRecommendations[recIndex] });
            } else {
                reject(new Error("Recommendation not found"));
            }
        }, 500);
    });
};

export const applyRecommendation = async (recommendationId: string): Promise<Recommendation> => {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            const recIndex = mockRecommendations.findIndex(
                (r) => r.recommendationId === recommendationId
            );
            if (recIndex !== -1) {
                mockRecommendations[recIndex].status = "APPLIED";
                resolve({ ...mockRecommendations[recIndex] });
            } else {
                reject(new Error("Recommendation not found"));
            }
        }, 500);
    });
};
