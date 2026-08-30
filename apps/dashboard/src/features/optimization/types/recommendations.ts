import { CloudProviderEnum } from "@/features/dashboard/types/provider";

//Terminate = switch off resource
//Modernize = get newer/more modern package for better performance
//Downsize = get smaller package (ie. t3.xlarge to t3.medium)
//Suspend = recommend power schedule (ie. siwtch off at night)
export type RecommendedAction = "TERMINATE" | "MODERNIZE" | "DOWNSIZE" | "SUSPEND";

export type RecommendationStatus = "ACTIVE" | "DISMISSED" | "APPLIED" | "SUSPENDED" | "EXPIRED";

export type Recommendation = {
    recommendationId: string;
    resourceId: string;
    resourceType: string;
    resourceDisplayName?: string;
    provider: CloudProviderEnum;
    ruleId: string;
    actionType: RecommendedAction;
    status: RecommendationStatus;
    evidence: Record<string, number>;
    createdAt?: Date | string;
    updatedAt?: Date | string;
    expiresAt?: Date | string;
};

export type RecommendationGroup = {
    accountId: string | null;
    displayName?: string | null;
    recommendations: Recommendation[];
};

export type RecommendationSummary = {
    total: number;
    active: number;
    dismissed: number;
    applied: number;
    byActionType: Partial<Record<RecommendedAction, number>>;
};
