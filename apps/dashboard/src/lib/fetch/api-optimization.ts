import {
    Recommendation,
    RecommendationSummary,
} from "@/features/optimization/types/recommendations";
import apiClient from "@/lib/fetch/api-client";

export const getOptimizationRecommendations = async (): Promise<Recommendation[]> => {
    try {
        const data = await apiClient<Recommendation[]>("/optimization/recommendations", {
            method: "GET",
        });
        return data;
    } catch (error) {
        console.error("Failed to fetch recommendations:", error);
        throw error;
    }
};

export const getRecommendationSummary = async (): Promise<RecommendationSummary> => {
    try {
        const data = await apiClient<RecommendationSummary>(
            "/optimization/recommendations/summary",
            {
                method: "GET",
            }
        );
        return data as RecommendationSummary;
    } catch (error) {
        console.error("Failed to fetch summary of recommendations:", error);
        throw error;
    }
};

export const dismissRecommendation = async (recommendationId: string): Promise<Recommendation> => {
    try {
        const data = await apiClient<Recommendation>(
            `/optimization/recommendations/${recommendationId}/dismiss`,
            {
                method: "POST",
            }
        );
        return data;
    } catch (error) {
        console.error("Failed to dismiss recommendation:", error);
        throw error;
    }
};

export const applyRecommendation = async (recommendationId: string): Promise<Recommendation> => {
    try {
        const data = await apiClient<Recommendation>(
            `/optimization/recommendations/${recommendationId}/apply`,
            {
                method: "POST",
            }
        );
        return data;
    } catch (error) {
        console.error("Failed to flag recommendation as applied:", error);
        throw error;
    }
};

export const reEnableRecommendation = async (recommendationId: string): Promise<Recommendation> => {
    try {
        const data = await apiClient<Recommendation>(
            `/optimization/recommendations/${recommendationId}/re-enable`,
            {
                method: "POST",
            }
        );
        return data;
    } catch (error) {
        console.error("Failed to re-enable recommendation:", error);
        throw error;
    }
};
