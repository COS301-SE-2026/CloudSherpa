import apiClient from "./api-client";

export interface CloudCredentials {
  accessKey?: string;
  secretKey?: string;
  region?: string;
  subscriptionId?: string;
  tenantId?: string;
  clientId?: string;
  clientSecret?: string;
}

export interface ResourceDetail {
  id: string;
  name: string;
  type: string;
  region?: string;
  [key: string]: unknown;
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
export async function getCloudServices(
  provider: string
): Promise<string[]> {
  return apiClient<string[]>(
    "/api/cloud-resources/services",
    {
      method: "POST",
      body: provider,
    }
  );
}

/**
 * Discover all resources accessible using the supplied credentials and permissions previously configured.
 */
export async function getCloudResources(
  provider: string,
  credentials: CloudCredentials
): Promise<ResourceDetail[]> {
  return apiClient<ResourceDetail[]>(
    `/api/cloud-resources/resources/${provider}`,
    {
      method: "POST",
      body: JSON.stringify(credentials),
    }
  );
}

/**
 * Generate a least-privilege AWS IAM policy for a set of selected services.
 */
export async function generateAwsPermissionsPolicy(
  services: string[]
): Promise<AwsPolicy> {
  return apiClient<AwsPolicy>(
    "/api/cloud-resources/aws/permissions",
    {
      method: "POST",
      body: JSON.stringify(services),
    }
  );
}
