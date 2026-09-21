import apiClient from "@/lib/fetch/api-client";
import {
    Webhook,
    WebhookEvent,
    CreateWebhookPayload,
    UpdateWebhookPayload,
    WebhookDeliveryPagedResult,
} from "@/features/webhooks/types";

type DataWebhookEvent = {
    type: string;
    timestamp: string;
    account: { name: string; provider: string };
    data: Record<string, unknown>;
};

type DataWebhookEventResponse = Record<string, Record<string, DataWebhookEvent>>;

export const fetchWebhooks = async (): Promise<Webhook[]> => {
    return apiClient<Webhook[]>("/webhooks", { method: "GET" });
};

export const fetchWebhookEvents = async (): Promise<WebhookEvent[]> => {
    const data = await apiClient<DataWebhookEventResponse>("/webhooks/events", { method: "GET" });

    const events: WebhookEvent[] = [];

    Object.entries(data).forEach(([category, eventsByName]) => {
        Object.entries(eventsByName).forEach(([name, payload]) => {
            events.push({
                id: payload.type,
                type: payload.type,
                category,
                description: name,
                jsonBody: {
                    type: payload.type,
                    timestamp: payload.timestamp,
                    account: payload.account,
                    data: payload.data,
                },
            });
        });
    });

    return events;
};

export const fetchWebhookDeliveries = async (
    page: number,
    pageSize: number
): Promise<WebhookDeliveryPagedResult> => {
    return apiClient<WebhookDeliveryPagedResult>(
        `/webhooks/deliveries?page=${page}&pageSize=${pageSize}`,
        { method: "GET" }
    );
};

export const addWebhook = async (payload: CreateWebhookPayload): Promise<{ secret: string }> => {
    return apiClient<{ secret: string }>("/webhooks/add", {
        method: "POST",
        body: JSON.stringify(payload),
    });
};

export const editWebhook = async (
    webhookId: string,
    payload: UpdateWebhookPayload
): Promise<void> => {
    return apiClient<void>(`/webhooks/edit/${webhookId}`, {
        method: "POST",
        body: JSON.stringify(payload),
    });
};

export const deleteWebhook = async (webhookId: string): Promise<void> => {
    return apiClient<void>(`/webhooks/delete/${webhookId}`, {
        method: "DELETE",
    });
};
