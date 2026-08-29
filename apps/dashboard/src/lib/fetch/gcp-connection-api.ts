import { GcpBillingConfigType } from "@/features/connectionManager/components/connectionManager/gcp/validTypes";
import apiClient from "./api-client";
import { GcpCredentialsDto } from "./dto/cloud-credentials";
import { ResourceSelectionDto } from "./dto/cloud-resource";

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
