export interface AwsCredentialsDto {
    accessKeyId: string;
    secretAccessKey: string;
}

export interface GcpCredentialsDto {
    projectId: string;
    serviceAccountJson: string;
}

export interface AzureCredentialsDto {
    subscriptionId: string;
    tenantId: string;
    clientId: string;
    clientSecret: string;
}

export interface CloudCredentials {
    accessKeyId?: string;
    secretAccessKey?: string;
    awsRegion?: string;
    tenantId?: string;
    clientId?: string;
    clientSecret?: string;
    projectId?: string;
    serviceAccountJson?: string;
}

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
