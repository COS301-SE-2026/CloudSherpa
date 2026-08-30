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

export interface CloudAccountDetails {
    id: string;
    displayName: string;
    accountType: AccountType;
    accountEmail: string;
    ingestionPeriod: string;
    createdAt: string;
}
