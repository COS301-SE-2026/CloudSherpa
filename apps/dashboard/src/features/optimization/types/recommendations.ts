import { CloudProviderEnum } from "@/features/dashboard/types/provider";

//Terminate = switch off resource
//Modernize = get newer/more modern package for better performance
//Downsize = get smaller package (ie. t3.xlarge to t3.medium)
//Suspend = recommend power schedule (ie. siwtch off at night)
export type RecommendedAction = "TERMINATE" | "MODERNIZE" | "DOWNSIZE" | "SUSPEND";

export type RecommendationStatus =
    "ACTIVE" | "ACKNOWLEDGED" | "DISMISSED" | "APPLIED" | "SUSPENDED" | "EXPIRED";

//don't yet know how to handle curr and target config for now, gonna make it dynamic:
export type ResourceConfiguration = Record<string, string | number | boolean | null>;
export type RecommendationEvidence = Record<string, number | string | null>;

export type Recommendation = {
    recommendation: string;
    resource_id: string;
    provider: CloudProviderEnum;
    action_type: RecommendedAction;
    status: RecommendationStatus;

    current_configuration: ResourceConfiguration;
    target_configuration: ResourceConfiguration | null;

    estimated_monthly_savings: number;
    currency: string;
    evidence: RecommendationEvidence;
};
