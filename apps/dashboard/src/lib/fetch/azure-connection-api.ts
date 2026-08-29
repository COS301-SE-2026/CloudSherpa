import apiClient from "./api-client";
import { AzureCredentialsDto } from "./dto/cloud-credentials";

export interface ResourceSelectionDto {
    resourceId: string;
    serviceType: string;
    resourceType: string;
    resourceName: string;
    region: string;
    tags: Record<string, string>;
    active: boolean;
}

export interface PersistAzureConnectionRequest {
    userId: string;
    displayName: string;
    ingestionPeriod: string;
    credentials: AzureCredentialsDto;
    resources: ResourceSelectionDto[];
}

export async function createGcpConnection(request: PersistAzureConnectionRequest): Promise<void> {
    await apiClient<void>("/azure/connections", {
        method: "POST",
        body: JSON.stringify(request),
    });
}
