import apiClient from "./api-client";
import { BillingConfigDto } from "./dto/cloud-billing";
import { AwsCredentialsDto } from "./dto/cloud-credentials";
import { ResourceSelectionDto } from "./dto/cloud-resource";

export interface PersistAwsConnectionRequest {
    userId: string;
    displayName: string;
    ingestionPeriod: string;
    credentials: AwsCredentialsDto;
    resources: ResourceSelectionDto[];
    billingConfig: BillingConfigDto;
}

export async function createAwsConnection(request: PersistAwsConnectionRequest): Promise<void> {
    await apiClient<void>("/aws/connections", {
        method: "POST",
        body: JSON.stringify(request),
    });
}
