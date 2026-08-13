import { CloudProviderEnum } from "@/features/dashboard/types/provider";

//Terminate = switch off resource
//Modernize = get newer/more modern package for better performance
//Downsize = get smaller package (ie. t3.xlarge to t3.medium)
//Suspend = recommend power schedule (ie. siwtch off at night)
export type RecommendedAction = "TERMINATE" | "MODERNIZE" | "DOWNSIZE" | "SUSPEND";

export type RecommendationStatus =
    "ACTIVE" | "ACKNOWLEDGED" | "DISMISSED" | "APPLIED" | "SUSPENDED" | "EXPIRED";

export type Recommendation = {
    recommendation_id: string;
    resource_id: string;
    resource_displayName?: string | null;
    provider: CloudProviderEnum;
    action_type: RecommendedAction;
    status: RecommendationStatus;

    current_configuration: string;
    target_configuration: string | null;

    estimated_monthly_savings: number;
    currency: string;
    evidence: number;
    completenessRatio: number;
};

export type RecommendationGroup = {
    accountId: string | null;
    displayName?: string | null;
    recommendations: Recommendation[];
};
