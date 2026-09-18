"use client";

import {useEffect, useMemo, useState} from "react";
import {Plus, Search, Edit, Trash2, ChevronLeft, ChevronRight} from "lucide-react";
import {useReactTable, getCoreRowModel, flexRender, ColumnDef, getPaginationRowModel} from "@tanstack/react-table";
import {fetchWebhooks, fetchWebhookEvents, fetchWebhookDeliveries, deleteWebhook} from "@/features/webhooks/webhooks";
import { getAwsAccountConnections } from "@/lib/fetch/cloud-account-api";
import {Webhook, WebhookDelivery, WebhookEvent, CloudAccount} from "@/features/webhooks/types";
import {AddWebhook} from "@/features/webhooks/components/addWebhooks";
import {Popup} from "@/features/webhooks/components/confirmPopup";
import {ExampleForPayload} from "@/features/webhooks/components/payload";
import {Button} from "@/components/atoms/button";
import {Input} from "@/components/atoms/input";
import {Badge} from "@/components/atoms/badge";
import {Card, CardContent, CardHeader, CardTitle, CardDescription} from "@/components/atoms/card";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/atoms/select";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/atoms/table";
import {Label} from "@/components/atoms/label";

export const Webhooks = () => {
    const [webhooks, setWebhooks] = useState<Webhook[]>([]);

    const [delivery, setDelivery] = useState<WebhookDelivery[]>([]);

    const [eventsAvailable, setEventsAvailable] = useState<WebhookEvent[]>([]);

    const [cloudAccounts, setCloudAccounts] = useState<CloudAccount[]>([]);

    const [loading, setLoading] = useState(true);

    const [addWebhookOpen, setAddWebhookOpen] = useState(false);

    const [editWebhook, setEditWebhook] = useState<Webhook | null>(null);

    const [secret, setSecret] = useState<string | null>(null);

    const [eventSelectedForPayload, setEventSelectedForPayload] = useState<WebhookEvent | null>(null);

    const [webhookSearch, setWebhookSearch] = useState("");

    const [deliverySearch, setDeliverySearch] = useState("");

    const [filterForStatus, setFilterForStatus] = useState<string>("all");

    const [paginationForWebhook, setPaginationForWebhook] = useState({pageIndex : 0, pageSize : 5});

    const [paginationForDelivery, setPaginationForDelivery] = useState({pageIndex : 0, pageSize : 5});

    const handlingDelete = async (id : string) => {
        if(!confirm("Are you sure you want to delete this webhook?")){
            return;
        }

        try{
            await deleteWebhook(id);

            setWebhooks((previous) => previous.filter((webhook) => webhook.id !== id));
        }catch{
            alert("Failed to delete webhook");
        }
    };

    const handlingEdit = (webhook : Webhook) => {
        setEditWebhook(webhook);

        setAddWebhookOpen(true);
    };

    const handlingAddSuccess = (secret : string) => {
        if(secret){
            setSecret(secret);
        }

        fetchWebhooks().then(setWebhooks);
    };

    return(
        <div className = "p-6 max-w-7xl mx-auto space-y-8 bg-background text-foreground">

            <div className = "flex justify-between items-center">
                <div>
                    <h1 className = "text-2xl font-bold"> Webhooks </h1>

                    <p className = "text-muted-foreground text-sm mt-1"> Manage endpoints and inspect event deliveries </p>
                </div>

                <Button onClick = {() => {setEditWebhook(null); setAddWebhookOpen(true);}}> <Plus size = {16} className = "mr-2"/> Add webhook </Button>
            </div>

        </div>
    );
};