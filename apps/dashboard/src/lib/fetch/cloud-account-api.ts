import apiClient from "./api-client";
import { CloudAccount, CloudAccountDetails } from "./dto/cloud-account";
import { CloudResource, ResourceStatus } from "./dto/cloud-resource";

export interface ResourceCountResponse {
    count: number;
}

export interface UpdateAccountNameRequest {
    name: string;
}

export async function getAwsAccountConnections(): Promise<CloudAccount[]> {
    return apiClient<CloudAccount[]>("/aws/connections", { method: "GET" });
}

export async function getAwsAccount(accountId: string): Promise<CloudAccountDetails> {
    return apiClient<CloudAccountDetails>(`/aws/accounts/${accountId}`, {
        method: "GET",
    });
}

export async function getAwsAccountResources(accountId: string): Promise<CloudResource[]> {
    return apiClient<CloudResource[]>(`/aws/accounts/${accountId}/resources`, {
        method: "GET",
    });
}

export async function getAwsAccountResourceCount(accountId: string): Promise<number> {
    const response = await apiClient<ResourceCountResponse>(
        `/aws/accounts/${accountId}/resources/count`,
        {
            method: "GET",
        }
    );

    return response.count;
}

export async function updateAwsResourceStatus(
    resourceId: string,
    status: ResourceStatus
): Promise<void> {
    await apiClient<void>(`/aws/resources/${resourceId}/status`, {
        method: "PATCH",
        body: JSON.stringify({
            status,
        }),
    });
}

export async function deleteAwsAccount(accountId: string): Promise<void> {
    await apiClient<void>(`/aws/connections/${accountId}`, {
        method: "DELETE",
    });
}

export async function updateAwsAccountName(accountId: string, name: string): Promise<void> {
    await apiClient<void>(`/aws/connections/${accountId}/name`, {
        method: "PATCH",
        body: JSON.stringify({
            name,
        }),
    });
}
