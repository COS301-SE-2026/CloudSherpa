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
    resource_displayName?: string;
    provider: CloudProviderEnum;
    rule_id: string;
    action_type: RecommendedAction;
    status: RecommendationStatus;
    evidence: number;
    created_at: Date;
    update_at: Date;
    expires_at: Date;
};

export type RecommendationGroup = {
    accountId: string | null;
    displayName?: string | null;
    recommendations: Recommendation[];
};
