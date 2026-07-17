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

export async function createAwsConnection(request: PersistAwsConnectionRequest): Promise<void> {
  await apiClient<void>("/aws/connections", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export async function getAwsAccountConnections(): Promise<CloudAccount[]> {
  return apiClient<CloudAccount[]>("/aws/connections", { method: "GET", });
}

export async function getAwsAccountResources(accountId: string): Promise<CloudResource[]> {
  return apiClient<CloudResource[]>("/aws/resources", { method: "GET", body: JSON.stringify(accountId), });
}
