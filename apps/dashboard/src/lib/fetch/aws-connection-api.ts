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

export enum AccountType {
  AWS_ACCOUNT = "aws_account",
  AZURE_SUBSCRIPTION = "azure_subscription",
  GCP_PROJECT = "gcp_project",
}


export interface CloudAccount {
  id: string;
  connectionId: string;
  accountType: AccountType;
  displayName: string;
  ingestionPeriod: string;
  createdAt: string;
}

export enum ResourceStatus {
  ACTIVE = "active",
  DISABLED = "disabled",
}

export interface CloudResource {
  id: string;
  accountId: string;
  resourceType: string;
  resourceName: string;
  status: ResourceStatus;
  tags: Record<string, string>;
  lastUpdated: string;
  createdAt: string;
}

export interface CloudAccountDetails {
  id: string;
  displayName: string;
  accountType: AccountType;
  accountEmail: string;
  ingestionPeriod: string;
  createdAt: string;
}

export interface ResourceCountResponse {
  count: number;
}

export interface UpdateAccountNameRequest {
  name: string;
}

export async function createAwsConnection(request: PersistAwsConnectionRequest): Promise<void> {
  await apiClient<void>("/aws/connections", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export async function getAwsAccountConnections(): Promise<CloudAccount[]> {
  return apiClient<CloudAccount[]>("/aws/connections", { method: "GET", });
}

export async function getAwsAccount(
  accountId: string,
): Promise<CloudAccountDetails> {
  return apiClient<CloudAccountDetails>(
    `/aws/accounts/${accountId}`,
    {
      method: "GET",
    },
  );
}

export async function getAwsAccountResources(
  accountId: string,
): Promise<CloudResource[]> {
  return apiClient<CloudResource[]>(
    `/aws/accounts/${accountId}/resources`,
    {
      method: "GET",
    },
  );
}

export async function getAwsAccountResourceCount(
  accountId: string,
): Promise<number> {
  const response = await apiClient<ResourceCountResponse>(
    `/aws/accounts/${accountId}/resources/count`,
    {
      method: "GET",
    },
  );

  return response.count;
}

export async function updateAwsResourceStatus(
  resourceId: string,
  status: ResourceStatus,
): Promise<void> {
  await apiClient<void>(
    `/aws/resources/${resourceId}/status`,
    {
      method: "PATCH",
      body: JSON.stringify({
        status,
      }),
    },
  );
}

export async function deleteAwsAccount(
  accountId: string,
): Promise<void> {
  await apiClient<void>(
    `/aws/connections/${accountId}`,
    {
      method: "DELETE",
    },
  );
}

export async function updateAwsAccountName(
  accountId: string,
  name: string,
): Promise<void> {
  await apiClient<void>(
    `/aws/connections/${accountId}/name`,
    {
      method: "PATCH",
      body: JSON.stringify({
        name,
      }),
    },
  );
}
