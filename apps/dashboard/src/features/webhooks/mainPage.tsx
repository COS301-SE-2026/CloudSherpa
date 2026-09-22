"use client";

import { useEffect, useMemo, useState, useCallback } from "react";
import { Plus, Search, Edit, Trash2, ChevronLeft, ChevronRight, RefreshCcw } from "lucide-react";
import {
    useReactTable,
    getCoreRowModel,
    flexRender,
    ColumnDef,
    getPaginationRowModel,
} from "@tanstack/react-table";
import {
    fetchWebhooks,
    fetchWebhookEvents,
    fetchWebhookDeliveries,
    deleteWebhook,
} from "@/features/webhooks/webhooks";
import { getAwsAccountConnections } from "@/lib/fetch/cloud-account-api";
import { Webhook, WebhookDelivery, WebhookEvent, CloudAccount } from "@/features/webhooks/types";
import { AddWebhook } from "@/features/webhooks/components/addWebhooks";
import { Popup } from "@/features/webhooks/components/confirmPopup";
import { ExampleForPayload } from "@/features/webhooks/components/payload";
import { Button } from "@/components/atoms/button";
import { Input } from "@/components/atoms/input";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/atoms/card";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/atoms/select";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/atoms/table";
import { Label } from "@/components/atoms/label";
import { DeletePopup } from "@/features/webhooks/components/deletePopup";
import { ButtonGroup } from "@/components/atoms/button-group";
import { toast } from "sonner";

//moved to outside to correct sonarqube errors
const helperForWebhookColumns = (
    onEdit: (webhook: Webhook) => void,
    onDelete: (webhook: Webhook) => void
): ColumnDef<Webhook>[] => [
    {
        accessorKey: "webhookName",
        header: "Name",
        cell: (info) => (
            <span className="font-medium text-foreground"> {info.getValue() as string} </span>
        ),
    },

    {
        accessorKey: "endpointUrl",
        header: "Endpoint URL",
        cell: (info) => <span className="text-primary"> {info.getValue() as string} </span>,
    },

    {
        accessorKey: "eventTypes",
        header: "Events",
        cell: (info) => `${(info.getValue() as string[]).length} events`,
    },

    {
        accessorKey: "cloudAccounts",
        header: "Accounts",
        cell: (info) => {
            const numCloudAccounts = (info.getValue() as string[]).length;

            if (numCloudAccounts == 0) {
                return "All accounts";
            }

            return `${numCloudAccounts} accounts`;
        },
    },

    {
        accessorKey: "webhookStatus",
        header: "Status",
        cell: (info) => {
            const status = info.getValue() as string;

            return (
                <span className={status === "ACTIVE" ? "text-success" : "text-warning"}>
                    {" "}
                    {status === "ACTIVE" ? "Active" : "Paused"}{" "}
                </span>
            );
        },
    },

    {
        id: "actions",
        header: "Actions",
        cell: ({ row }) => (
            <div className="flex items-center gap-2">
                <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => onEdit(row.original)}
                    className="h-8 w-8 text-muted-foreground hover:text-foreground"
                >
                    {" "}
                    <Edit size={16} />{" "}
                </Button>

                <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => onDelete(row.original)}
                    className="h-8 w-8 text-destructive hover:text-destructive-foreground"
                >
                    {" "}
                    <Trash2 size={16} />{" "}
                </Button>
            </div>
        ),
    },
];

const helperForDeliveryColumns = (webhooks: Webhook[]): ColumnDef<WebhookDelivery>[] => [
    {
        accessorKey: "timestamp",
        header: "Time",
        cell: (info) => new Date(info.getValue() as string).toLocaleString(),
    },

    {
        accessorKey: "webhookId",
        header: "Webhook",
        cell: (info) =>
            webhooks.find((webhook) => webhook.webhookId === info.getValue())?.webhookName ??
            "Deleted Webhook",
    },

    { accessorKey: "eventType", header: "Event" },

    {
        accessorKey: "cloudAccountName",
        header: "Account",
        cell: (info) => info.getValue() ?? "Deleted Account",
    },

    {
        accessorKey: "result",
        header: "Result",
        cell: (info) => {
            const forResult = info.getValue() as string;

            return (
                <span className={forResult === "DELIVERED" ? "text-success" : "text-destructive"}>
                    {" "}
                    {forResult === "DELIVERED" ? "Delivered" : "Failed"}{" "}
                </span>
            );
        },
    },

    { accessorKey: "responseCode", header: "HTTP" },
];

export const Webhooks = () => {
    const [webhooks, setWebhooks] = useState<Webhook[]>([]);

    const [delivery, setDelivery] = useState<WebhookDelivery[]>([]);

    const [totalDeliveryElements, setTotalDeliveryElements] = useState(0);

    const [eventsAvailable, setEventsAvailable] = useState<WebhookEvent[]>([]);

    const [cloudAccounts, setCloudAccounts] = useState<CloudAccount[]>([]);

    const [addWebhookOpen, setAddWebhookOpen] = useState(false);

    const [editWebhook, setEditWebhook] = useState<Webhook | null>(null);

    const [secret, setSecret] = useState<string | null>(null);

    const [eventSelectedForPayload, setEventSelectedForPayload] = useState<WebhookEvent | null>(
        null
    );

    const [webhookSearch, setWebhookSearch] = useState("");

    const [deliverySearch, setDeliverySearch] = useState("");
    const [submittedDeliverySearch, setSubmittedDeliverySearch] = useState("");

    const [filterForStatus, setFilterForStatus] = useState<string>("all");

    const [paginationForWebhook, setPaginationForWebhook] = useState({ pageIndex: 0, pageSize: 5 });

    const [paginationForDelivery, setPaginationForDelivery] = useState({
        pageIndex: 0,
        pageSize: 5,
    });

    const [filterForDeliveryStatus, setFilterForDeliveryStatus] = useState<string>("all");

    const [filterForDeliveryWebhook, setFilterForDeliveryWebhook] = useState<string>("all");

    const [webhookToDelete, setWebhookToDelete] = useState<Webhook | null>(null);

    const [deliveryLoading, setDeliveryLoading] = useState(false);
    const [deliveryError, setDeliveryError] = useState<string | null>(null);

    useEffect(() => {
        const loadingData = async () => {
            try {
                const [webhooks, events, accounts] = await Promise.all([
                    fetchWebhooks(),
                    fetchWebhookEvents(),
                    getAwsAccountConnections(),
                ]);

                setWebhooks(webhooks);
                setEventsAvailable(events);
                setCloudAccounts(accounts);

                if (events.length > 0) {
                    setEventSelectedForPayload(events[0]);
                }
            } catch (error) {
                //console.error("error loading webhook data");
            }
        };

        loadingData();
    }, []);

    const handlingDelete = useCallback((webhook: Webhook) => {
        setWebhookToDelete(webhook);
    }, []);

    const confirmDelete = useCallback(async () => {
        if (!webhookToDelete) {
            return;
        }

        try {
            await deleteWebhook(webhookToDelete.webhookId);

            setWebhooks((previous) =>
                previous.filter((webhook) => webhook.webhookId !== webhookToDelete.webhookId)
            );

            toast.success("Webhook has been successfully deleted");
        } catch {
            toast.error("Failed to delete webhook");
        } finally {
            setWebhookToDelete(null);
        }
    }, [webhookToDelete]);

    const handlingEdit = useCallback((webhook: Webhook) => {
        setEditWebhook(webhook);

        setAddWebhookOpen(true);
    }, []);

    const handlingAddSuccess = (secret: string) => {
        if (secret) {
            setSecret(secret);
        }

        fetchWebhooks().then(setWebhooks);
    };

    const filteredWebhooks = useMemo(() => {
        return webhooks.filter(
            (webhook) =>
                webhook.webhookName.toLowerCase().includes(webhookSearch.toLowerCase()) &&
                (filterForStatus === "all" || webhook.webhookStatus === filterForStatus)
        );
    }, [webhooks, webhookSearch, filterForStatus]);

    const [deliveryRefreshKey, setDeliveryRefreshKey] = useState(0);

    useEffect(() => {
        let ignore = false;

        const loadDeliveries = async () => {
            setDeliveryLoading(true);
            setDeliveryError(null);

            const status =
                filterForDeliveryStatus === "DELIVERED" || filterForDeliveryStatus === "FAILED"
                    ? filterForDeliveryStatus
                    : undefined;

            try {
                const result = await fetchWebhookDeliveries(
                    paginationForDelivery.pageIndex,
                    paginationForDelivery.pageSize,
                    submittedDeliverySearch,
                    filterForDeliveryWebhook === "all" ? undefined : filterForDeliveryWebhook,
                    status
                );

                if (!ignore) {
                    setDelivery(result.deliveries);
                    setTotalDeliveryElements(result.totalElements);
                }
            } catch {
                if (!ignore) {
                    setDeliveryError("Could not load deliveries.");
                }
            } finally {
                if (!ignore) {
                    setDeliveryLoading(false);
                }
            }
        };

        loadDeliveries();

        return () => {
            ignore = true;
        };
    }, [
        paginationForDelivery.pageIndex,
        paginationForDelivery.pageSize,
        submittedDeliverySearch,
        filterForDeliveryWebhook,
        filterForDeliveryStatus,
        deliveryRefreshKey,
    ]);

    const webhookColumns = useMemo(
        () => helperForWebhookColumns(handlingEdit, handlingDelete),
        [handlingEdit, handlingDelete]
    );

    const resetDeliveryPage = () => {
        setPaginationForDelivery((previous) => ({
            ...previous,
            pageIndex: 0,
        }));
    };

    const handleDeliverySearch = () => {
        setSubmittedDeliverySearch(deliverySearch.trim());
        resetDeliveryPage();
    };

    const handleDeliverySearchReset = () => {
        setDeliverySearch("");
        setSubmittedDeliverySearch("");
        resetDeliveryPage();
    };

    const deliveryColumns = useMemo(() => helperForDeliveryColumns(webhooks), [webhooks]);

    const tableForWebhook = useReactTable({
        data: filteredWebhooks,
        columns: webhookColumns,
        getCoreRowModel: getCoreRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        state: { pagination: paginationForWebhook },
        onPaginationChange: setPaginationForWebhook,
    });

    const tableForDelivery = useReactTable({
        data: delivery,
        columns: deliveryColumns,
        getCoreRowModel: getCoreRowModel(),
        manualPagination: true,
        rowCount: totalDeliveryElements,
        state: { pagination: paginationForDelivery },
        onPaginationChange: setPaginationForDelivery,
    });

    let deliveryTableBody;

    if (deliveryError) {
        deliveryTableBody = (
            <TableRow>
                <TableCell
                    colSpan={tableForDelivery.getVisibleLeafColumns().length}
                    className="h-24 text-center text-destructive"
                >
                    <span role="alert">{deliveryError}</span>
                </TableCell>
            </TableRow>
        );
    } else {
        deliveryTableBody = tableForDelivery.getRowModel().rows.map((forRow) => (
            <TableRow key={forRow.id}>
                {forRow.getVisibleCells().map((forCells) => (
                    <TableCell key={forCells.id}>
                        {flexRender(forCells.column.columnDef.cell, forCells.getContext())}
                    </TableCell>
                ))}
            </TableRow>
        ));
    }

    return (
        <div className="p-6 max-w-7xl mx-auto space-y-8 bg-background text-foreground">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold"> Webhooks </h1>

                    <p className="text-muted-foreground text-sm mt-1">
                        {" "}
                        Manage endpoints and inspect event deliveries{" "}
                    </p>
                </div>

                <Button
                    onClick={() => {
                        setEditWebhook(null);
                        setAddWebhookOpen(true);
                    }}
                >
                    {" "}
                    <Plus size={16} className="mr-2" /> Add webhook{" "}
                </Button>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle> Configured webhooks </CardTitle>
                </CardHeader>

                <CardContent className="space-y-4">
                    <div className="flex gap-4">
                        <div className="relative flex-1 max-w-sm">
                            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />

                            <Input
                                placeholder="Search webhooks"
                                value={webhookSearch}
                                onChange={(change) => setWebhookSearch(change.target.value)}
                                className="pl-8"
                            />
                        </div>

                        <Select value={filterForStatus} onValueChange={setFilterForStatus}>
                            <SelectTrigger className="w-[180px]">
                                {" "}
                                <SelectValue placeholder="All statuses" />{" "}
                            </SelectTrigger>

                            <SelectContent>
                                <SelectItem value="all"> All statuses </SelectItem>

                                <SelectItem value="ACTIVE"> Active </SelectItem>

                                <SelectItem value="PAUSED"> Paused </SelectItem>
                            </SelectContent>
                        </Select>
                    </div>

                    <div className="rounded-md border border-border">
                        <Table>
                            <TableHeader>
                                {tableForWebhook.getHeaderGroups().map((headerGroup) => (
                                    <TableRow key={headerGroup.id}>
                                        {headerGroup.headers.map((header) => (
                                            <TableHead key={header.id}>
                                                {flexRender(
                                                    header.column.columnDef.header,
                                                    header.getContext()
                                                )}
                                            </TableHead>
                                        ))}
                                    </TableRow>
                                ))}
                            </TableHeader>

                            <TableBody>
                                {tableForWebhook.getRowModel().rows.map((forRows) => (
                                    <TableRow key={forRows.id}>
                                        {forRows.getVisibleCells().map((forCells) => (
                                            <TableCell key={forCells.id}>
                                                {flexRender(
                                                    forCells.column.columnDef.cell,
                                                    forCells.getContext()
                                                )}
                                            </TableCell>
                                        ))}
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </div>

                    <div className="grid grid-cols-3 items-center text-sm text-muted-foreground">
                        <span>
                            Showing{" "}
                            {tableForWebhook.getState().pagination.pageIndex *
                                tableForWebhook.getState().pagination.pageSize +
                                1}
                            -
                            {Math.min(
                                (tableForWebhook.getState().pagination.pageIndex + 1) *
                                    tableForWebhook.getState().pagination.pageSize,
                                filteredWebhooks.length
                            )}{" "}
                            of {filteredWebhooks.length} webhooks
                        </span>

                        <div className="flex items-center justify-center gap-2">
                            <Button
                                variant="ghost"
                                size="icon"
                                className="h-8 w-8"
                                onClick={() => tableForWebhook.previousPage()}
                                disabled={!tableForWebhook.getCanPreviousPage()}
                            >
                                {" "}
                                <ChevronLeft size={16} />{" "}
                            </Button>

                            <span>
                                {" "}
                                Page {tableForWebhook.getState().pagination.pageIndex + 1} of{" "}
                                {tableForWebhook.getPageCount()}{" "}
                            </span>

                            <Button
                                variant="ghost"
                                size="icon"
                                className="h-8 w-8"
                                onClick={() => tableForWebhook.nextPage()}
                                disabled={!tableForWebhook.getCanNextPage() || deliveryLoading}
                            >
                                {" "}
                                <ChevronRight size={16} />{" "}
                            </Button>
                        </div>

                        <div className="flex items-center justify-end gap-2">
                            <span> Rows per page: </span>
                            <Select
                                value={String(tableForWebhook.getState().pagination.pageSize)}
                                onValueChange={(value) =>
                                    tableForWebhook.setPageSize(Number(value))
                                }
                            >
                                <SelectTrigger className="w-[70px] h-8">
                                    {" "}
                                    <SelectValue />{" "}
                                </SelectTrigger>

                                <SelectContent>
                                    <SelectItem value="5"> 5 </SelectItem>
                                    <SelectItem value="10"> 10 </SelectItem>
                                    <SelectItem value="15"> 15 </SelectItem>
                                </SelectContent>
                            </Select>
                        </div>
                    </div>
                </CardContent>
            </Card>

            <Card>
                <CardHeader>
                    <CardTitle> Delivery log </CardTitle>
                </CardHeader>

                <CardContent className="space-y-4">
                    <div className="flex gap-4">
                        <div className="relative flex-1 max-w-sm">
                            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                            <ButtonGroup>
                                <Input
                                    placeholder="Search deliveries"
                                    value={deliverySearch}
                                    onChange={(change) => {
                                        setDeliverySearch(change.target.value);
                                    }}
                                    className="pl-8"
                                />

                                <Button variant={"outline"} onClick={handleDeliverySearch}>
                                    Search
                                </Button>
                                <Button variant={"outline"} onClick={handleDeliverySearchReset}>
                                    Reset
                                </Button>
                            </ButtonGroup>
                        </div>

                        <Select
                            value={filterForDeliveryWebhook}
                            onValueChange={(value) => {
                                setFilterForDeliveryWebhook(value);
                                resetDeliveryPage();
                            }}
                        >
                            <SelectTrigger className="w-[220px]">
                                {" "}
                                <SelectValue placeholder="All webhooks" />{" "}
                            </SelectTrigger>

                            <SelectContent>
                                <SelectItem value="all"> All webhooks </SelectItem>

                                {webhooks.map((webhook) => (
                                    <SelectItem key={webhook.webhookId} value={webhook.webhookId}>
                                        {" "}
                                        {webhook.webhookName}{" "}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>

                        <Select
                            value={filterForDeliveryStatus}
                            onValueChange={(value) => {
                                setFilterForDeliveryStatus(value);
                                resetDeliveryPage();
                            }}
                        >
                            <SelectTrigger className="w-[180px]">
                                {" "}
                                <SelectValue placeholder="All statuses" />{" "}
                            </SelectTrigger>

                            <SelectContent>
                                <SelectItem value="all"> All statuses </SelectItem>
                                <SelectItem value="DELIVERED"> Delivered </SelectItem>
                                <SelectItem value="FAILED"> Failed </SelectItem>
                            </SelectContent>
                        </Select>
                        <div className="ml-auto">
                            <Button
                                onClick={() => setDeliveryRefreshKey((current) => current + 1)}
                                disabled={deliveryLoading}
                            >
                                <RefreshCcw />
                                Refresh
                            </Button>
                        </div>
                    </div>

                    <div className="rounded-md border border-border">
                        <Table>
                            <TableHeader>
                                {tableForDelivery.getHeaderGroups().map((headerGroup) => (
                                    <TableRow key={headerGroup.id}>
                                        {headerGroup.headers.map((header) => (
                                            <TableHead key={header.id}>
                                                {flexRender(
                                                    header.column.columnDef.header,
                                                    header.getContext()
                                                )}
                                            </TableHead>
                                        ))}
                                    </TableRow>
                                ))}
                            </TableHeader>

                            <TableBody>{deliveryTableBody}</TableBody>
                        </Table>
                    </div>

                    <div className="grid grid-cols-3 items-center text-sm text-muted-foreground">
                        <span>
                            {" "}
                            Showing{" "}
                            {tableForDelivery.getState().pagination.pageIndex *
                                tableForDelivery.getState().pagination.pageSize +
                                1}
                            -
                            {Math.min(
                                (tableForDelivery.getState().pagination.pageIndex + 1) *
                                    tableForDelivery.getState().pagination.pageSize,
                                totalDeliveryElements
                            )}{" "}
                            of {totalDeliveryElements} deliveries{" "}
                        </span>

                        <div className="flex items-center justify-center gap-2">
                            <Button
                                variant="ghost"
                                size="icon"
                                className="h-8 w-8"
                                onClick={() => tableForDelivery.previousPage()}
                                disabled={!tableForDelivery.getCanPreviousPage() || deliveryLoading}
                            >
                                {" "}
                                <ChevronLeft size={16} />{" "}
                            </Button>

                            <span>
                                {" "}
                                Page {tableForDelivery.getState().pagination.pageIndex + 1} of{" "}
                                {tableForDelivery.getPageCount()}{" "}
                            </span>

                            <Button
                                variant="ghost"
                                size="icon"
                                className="h-8 w-8"
                                onClick={() => tableForDelivery.nextPage()}
                                disabled={!tableForDelivery.getCanNextPage()}
                            >
                                {" "}
                                <ChevronRight size={16} />{" "}
                            </Button>
                        </div>

                        <div className="flex items-center justify-end gap-2">
                            <span> Rows per page: </span>
                            <Select
                                value={String(tableForDelivery.getState().pagination.pageSize)}
                                onValueChange={(value) =>
                                    tableForDelivery.setPageSize(Number(value))
                                }
                            >
                                <SelectTrigger className="w-[70px] h-8">
                                    {" "}
                                    <SelectValue />{" "}
                                </SelectTrigger>

                                <SelectContent>
                                    <SelectItem value="5"> 5 </SelectItem>
                                    <SelectItem value="10"> 10 </SelectItem>
                                    <SelectItem value="15"> 15 </SelectItem>
                                </SelectContent>
                            </Select>
                        </div>
                    </div>
                </CardContent>
            </Card>

            <Card>
                <CardHeader>
                    <CardTitle> Payload examples </CardTitle>

                    <CardDescription>
                        {" "}
                        Explore the JSON your endpoint would receive{" "}
                    </CardDescription>
                </CardHeader>

                <CardContent>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        <div className="space-y-6">
                            <div className="space-y-2">
                                <Label htmlFor="event-type"> Event type </Label>

                                <Select
                                    value={eventSelectedForPayload?.id}
                                    onValueChange={(value) =>
                                        setEventSelectedForPayload(
                                            eventsAvailable.find((events) => events.id === value) ??
                                                null
                                        )
                                    }
                                >
                                    <SelectTrigger id="event-type">
                                        <SelectValue placeholder="Select event" />
                                    </SelectTrigger>

                                    <SelectContent>
                                        {eventsAvailable.map((events) => (
                                            <SelectItem key={events.id} value={events.id}>
                                                {" "}
                                                {events.description}{" "}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </div>

                            <div className="bg-muted/50 p-4 rounded border border-border">
                                <h3 className="text-sm font-semibold mb-2">
                                    {" "}
                                    Verify the signature{" "}
                                </h3>

                                <p className="text-xs text-muted-foreground mb-2">
                                    {" "}
                                    Sign webhook-id.webhook-timestamp.raw_body with
                                    HMAC-SHA256.{" "}
                                </p>
                            </div>
                        </div>

                        <div className="md:col-span-2">
                            {eventSelectedForPayload && (
                                <ExampleForPayload event={eventSelectedForPayload} />
                            )}
                        </div>
                    </div>
                </CardContent>
            </Card>

            {addWebhookOpen && (
                <AddWebhook
                    key={editWebhook?.webhookId ?? "new"}
                    isOpen={addWebhookOpen}
                    onClose={() => setAddWebhookOpen(false)}
                    eventsAvailable={eventsAvailable}
                    cloudAccounts={cloudAccounts}
                    initialData={editWebhook}
                    onSuccess={handlingAddSuccess}
                />
            )}

            {secret && <Popup secret={secret} onClose={() => setSecret(null)} />}

            {webhookToDelete && (
                <DeletePopup
                    isOpen={true}
                    webhookName={webhookToDelete.webhookName}
                    onCancel={() => setWebhookToDelete(null)}
                    onConfirm={confirmDelete}
                />
            )}
        </div>
    );
};
