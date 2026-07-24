import apiClient from "./api-client";

export interface CloudCredentials {
  accessKey?: string;
  secretKey?: string;
  awsRegion?: string;
  tenantId?: string;
  clientId?: string;
  clientSecret?: string;
  projectId?: string;
}

export interface ResourceDiscoveryRequest {
  services: string[];
  credentials: CloudCredentials;
}

export interface ResourceDetail {
  resourceId: string;
  name: string;
  resourceType: string;
  serviceCategory: string;
  region: string;
  tags: Record<string, string>;
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
    credentials: credentials
  }
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
