import apiClient from "./api-client";

export interface AwsCredentialsDto {
    accessKey: string;
    secretKey: string;
    awsRegion: string;
}

export interface ResourceSelectionDto {
    resourceId: string;
    resourceType: string;
    resourceName: string;
    tags: Record<string, string>;
    active: boolean;
}

export interface PersistAwsConnectionRequest {
    userId: string;
    displayName: string;
    ingestionPeriod: string;
    credentials: AwsCredentialsDto;
    resources: ResourceSelectionDto[];
}

export async function createAwsConnection(request: PersistAwsConnectionRequest): Promise<void> {
    await apiClient<void>("/aws/connections", {
        method: "POST",
        body: JSON.stringify(request),
    });
}
