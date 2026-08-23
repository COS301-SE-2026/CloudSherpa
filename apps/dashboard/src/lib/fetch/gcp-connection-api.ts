import { GcpBillingConfigType } from "@/features/connectionManager/components/connectionManager/gcp/validTypes";
import apiClient from "./api-client";

export interface GcpCredentialsJson {
    type: string;
    project_id: string;
    private_key_id: string;
    private_key: string;
    client_email: string;
    client_id: string;
    auth_uri: string;
    token_uri: string;
}

export interface GcpCredentialsDto {
    projectId: string;
    serviceAccountJson: string;
}

export interface ResourceSelectionDto {
    resourceId: string;
    serviceType: string;
    resourceType: string;
    resourceName: string;
    region: string;
    tags: Record<string, string>;
    active: boolean;
}

export interface PersistGcpConnectionRequest {
    userId: string;
    displayName: string;
    ingestionPeriod: string;
    credentials: GcpCredentialsDto;
    resources: ResourceSelectionDto[];
    billingConfig: GcpBillingConfigType | null;
}

export async function createGcpConnection(request: PersistGcpConnectionRequest): Promise<void> {
    await apiClient<void>("/gcp/connections", {
        method: "POST",
        body: JSON.stringify(request),
    });
}
