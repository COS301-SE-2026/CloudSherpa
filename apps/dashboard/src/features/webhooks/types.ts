export type { CloudAccount } from "@/lib/fetch/dto/cloud-account";

export type WebhookStatus = "ACTIVE" | "PAUSED";

export interface WebhookEvent {
    id: string;
    type: string;
    category: string;
    description: string;
    requestHeaders?: Record<string, string>;
    jsonBody?: Record<string, unknown>;
}

export interface Webhook {
    id: string;
    name: string;
    endpointUrl: string;
    eventTypes: string[];
    status: WebhookStatus;
    cloudAccounts: string[];
    createdAt: string;
}

export interface WebhookDelivery {
    deliveryId: string;
    timestamp: string;
    webhookId: string;
    eventType: string;
    cloudAccountId: string | null;
    cloudAccountName: string | null;
    result: "DELIVERED" | "FAILED";
    responseCode: number;
}

export interface CreateWebhookPayload {
    name: string;
    endpointUrl: string;
    eventTypes: string[];
    cloudAccounts: string[];
}

export interface UpdateWebhookPayload extends CreateWebhookPayload {
    status: WebhookStatus;
}
