export interface ResourceSelectionDto {
    resourceId: string;
    serviceType: string;
    resourceType: string;
    resourceName: string;
    region: string;
    tags: Record<string, string>;
    active: boolean;
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

export interface ResourceDetail {
    resourceId: string;
    name: string;
    resourceType: string;
    serviceCategory: string;
    region: string;
    tags: Record<string, string>;
}
