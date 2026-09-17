import apiClient from "@/lib/fetch/api-client";
import {Webhook, WebhookDelivery, WebhookEvent, CreateWebhookPayload, UpdateWebhookPayload} from "@/features/webhooks/types";

export const fetchWebhooks = async () : Promise<Webhook[]> => {
    return apiClient<Webhook[]>("/webhooks", {method : "GET"});
};

export const fetchWebhookEvents = async () : Promise<WebhookEvent[]> => {
    return apiClient<WebhookEvent[]>("/webhooks/events", {method : "GET"});
};

export const fetchWebhookDeliveries = async () : Promise<WebhookDelivery[]> => {
    return apiClient<WebhookDelivery[]>("/webhooks/deliveries", {method : "GET"});
};

export const addWebhook = async (
    payload : CreateWebhookPayload
) : Promise<{secret : string}> => {
    return apiClient<{secret : string}>("/webhooks/add", {
        method : "POST",
        body : JSON.stringify(payload),
    });
};

export const editWebhook = async (
    webhookId : string,
    payload : UpdateWebhookPayload
) : Promise<void> => {
    return apiClient<void>(`/webhooks/edit/${webhookId}`, {
        method : "POST",
        body : JSON.stringify(payload),
    });
};

export const deleteWebhook = async (webhookId : string) : Promise<void> => {
    return apiClient<void>(`/webhooks/delete/${webhookId}`, {
        method : "DELETE",
    });
};