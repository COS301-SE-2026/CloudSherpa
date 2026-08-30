import apiClient from "./api-client";
import { CloudCredentials } from "./dto/cloud-credentials";
import { ResourceDetail } from "./dto/cloud-resource";

export interface ResourceDiscoveryRequest {
    services: string[];
    credentials: CloudCredentials;
}

export interface AwsPolicy {
    Version: string;
    Statement: unknown[];
}

/**
 * Retrieve all supported services for a selected cloud provider.
 *
 * Examples:
 * - aws
 * - azure
 * - gcp
 */
export async function getCloudServices(provider: string): Promise<string[]> {
    return apiClient<string[]>("/api/cloud-resources/services", {
        method: "POST",
        body: provider,
    });
}

/**
 * Discover all resources accessible using the supplied credentials for selected services.
 */
export async function getCloudResources(
    provider: string,
    credentials: CloudCredentials,
    services: string[]
): Promise<ResourceDetail[]> {
    const request: ResourceDiscoveryRequest = {
        services: services,
        credentials: credentials,
    };
    return apiClient<ResourceDetail[]>(`/api/cloud-resources/resources/${provider}`, {
        method: "POST",
        body: JSON.stringify(request),
    });
}

/**
 * Generate a least-privilege AWS IAM policy for a set of selected services.
 */
export async function generateAwsPermissionsPolicy(services: string[]): Promise<AwsPolicy> {
    return apiClient<AwsPolicy>("/api/cloud-resources/aws/permissions", {
        method: "POST",
        body: JSON.stringify(services),
    });
}

/**
 * Generate a least-privilege GCP permission set for a set of selected services.
 */
export async function generateGcpPermissionsPolicy(services: string[]): Promise<string[]> {
    return apiClient<string[]>("/api/cloud-resources/gcp/permissions", {
        method: "POST",
        body: JSON.stringify(services),
    });
}

/**
 * Generate a least-privilege Azure permission set for a set of selected services.
 */
export async function generateAzurePermissionsPolicy(services: string[]): Promise<string[]> {
    return apiClient<string[]>("/api/cloud-resources/azure/permissions", {
        method: "POST",
        body: JSON.stringify(services),
    });
}
