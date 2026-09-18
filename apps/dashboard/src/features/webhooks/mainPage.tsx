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

    useEffect(() => {
        const loadingData = async () => {
            try{
                const [webhooks, events, deliveries, accounts] = await Promise.all([
                    fetchWebhooks(), fetchWebhookEvents(), fetchWebhookDeliveries(), getAwsAccountConnections(),
                ]);

                setWebhooks(webhooks);
                setEventsAvailable(events);
                setDelivery(deliveries);
                setCloudAccounts(accounts);

                if(events.length>0){
                    setEventSelectedForPayload(events[0]);
                }
            }catch(error){
                //console.error("error loading webhook data");
            }finally{
                setLoading(false);
            }
        };

        loadingData();
    }, []);

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

    const filteredWebhooks = useMemo(() => {
        return webhooks.filter((webhook) => webhook.name.toLowerCase().includes(webhookSearch.toLowerCase()) && (filterForStatus === "all" || webhook.status === filterForStatus));
    }, [webhooks, webhookSearch, filterForStatus]);

    const filteredDeliveries = useMemo(() => {
        return delivery.filter((forDelivery) => forDelivery.eventType.toLowerCase().includes(deliverySearch.toLowerCase()));
    }, [delivery, deliverySearch]);

    const webhookColumns = useMemo<ColumnDef<Webhook>[]>(() => [
        {accessorKey : "name", header : "Name",
         cell : (info) => (<span className = "font-medium text-foreground"> {info.getValue() as string} </span>),
        },

        {accessorKey : "endpointUrl", header : "Endpoint URL",
         cell : (info) => (<span className = "text-primary"> {info.getValue() as string} </span>),
        },

        {accessorKey : "eventTypes", header : "Events",
         cell : (info) => `${(info.getValue() as string []).length} events`,
        },

        {accessorKey : "cloudAccounts", header : "Accounts",
         cell : (info) => `${(info.getValue() as string[]).length} accounts`,
        },

        {accessorKey : "status", header : "Status", cell : (info) => {
            const status = info.getValue() as string;

            return(
                <Badge variant = {status === "ACTIVE" ? "default" : "secondary"}

                className = {status === "ACTIVE" ? "bg-success/20 text-success hover:bg-success/30" : "bg-warning/20 text-warning hover:bg-warning/30"}> {status === "ACTIVE" ? "Active" : "Paused"} </Badge>
            );
        },},

        {id : "actions", header : "Actions", cell : ({row}) => (
            <div className = "flex items-center gap-2">
                <Button variant = "ghost" size = "icon" onClick = {() => handlingEdit(row.original)} className = "h-8 w-8 text-muted-foreground hover:text-foreground"> <Edit size = {16}/> </Button>

                <Button variant = "ghost" size = "icon" onClick = {() => handlingDelete(row.original.id)} className = "h-8 w-8 text-destructive hover:text-destructive-foreground"> <Trash2 size = {16}/> </Button>
            </div>
        ),},
    ], []);

    const deliveryColumns = useMemo<ColumnDef<WebhookDelivery>[]>(() => [
        {accessorKey : "timestamp", header : "Time", cell : (info) => new Date(info.getValue() as string).toLocaleString(),},

        {accessorKey : "webhookId", header : "Webhook", cell : (info) => webhooks.find((webhook) => webhook.id === info.getValue())?.name ?? (info.getValue() as string),},

        {accessorKey : "eventType", header : "Event"},

        {accessorKey : "cloudAccount", header : "Account"},

        {accessorKey : "result", header : "Result", cell : (info) => {
            const forResult = info.getValue() as string;

            return(
                <span className = {forResult === "DELIVERED" ? "text-success" : "text-destructive"}> {forResult === "DELIVERED" ? "Delivered" : "Failed"} </span>
            );
        },},

        {accessorKey : "responseCode", header : "HTTP"},

    ], [webhooks]);

    const tableForWebhook = useReactTable({
        data : filteredWebhooks, columns : webhookColumns, getCoreRowModel : getCoreRowModel(), getPaginationRowModel : getPaginationRowModel(), state : {pagination : paginationForWebhook}, onPaginationChange : setPaginationForWebhook,
    });

    const tableForDelivery = useReactTable({
        data : filteredDeliveries, columns : deliveryColumns, getCoreRowModel : getCoreRowModel(), getPaginationRowModel : getPaginationRowModel(), state : {pagination : paginationForDelivery}, onPaginationChange : setPaginationForDelivery,
    });

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