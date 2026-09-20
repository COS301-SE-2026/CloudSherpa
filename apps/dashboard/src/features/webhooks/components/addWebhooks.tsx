"use client";

import { useState, useMemo } from "react";
import { ChevronDown, ChevronRight, Cloud, Search } from "lucide-react";
import {
    WebhookEvent,
    CreateWebhookPayload,
    Webhook,
    CloudAccount,
} from "@/features/webhooks/types";
import { addWebhook, editWebhook } from "@/features/webhooks/webhooks";
import { Button } from "@/components/atoms/button";
import { Input } from "@/components/atoms/input";
import { Label } from "@/components/atoms/label";
import { Checkbox } from "@/components/atoms/checkbox";
import { ScrollArea } from "@/components/atoms/scroll-area";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogFooter,
} from "@/components/atoms/dialog";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/atoms/select";
import { AccountType } from "@/lib/fetch/dto/cloud-account";

const ACCOUNT_TYPES: Record<AccountType, string> = {
    [AccountType.AWS_ACCOUNT]: "AWS",
    [AccountType.GCP_PROJECT]: "GCP",
    [AccountType.AZURE_SUBSCRIPTION]: "Azure",
};

interface PropsForAddingWebhooks {
    isOpen: boolean;
    onClose: () => void;
    eventsAvailable: WebhookEvent[];
    cloudAccounts: CloudAccount[];
    initialData?: Webhook | null;
    onSuccess: (secret: string) => void;
}

export const AddWebhook = ({
    isOpen,
    onClose,
    eventsAvailable,
    cloudAccounts,
    initialData,
    onSuccess,
}: PropsForAddingWebhooks) => {
    const [name, setName] = useState(initialData?.name ?? "");

    const [endpointUrl, setEndpointUrl] = useState(initialData?.endpointUrl ?? "");

    const [eventsSelected, setEventsSelected] = useState<string[]>(initialData?.eventTypes ?? []);

    const [accountsSelected, setAccountsSelected] = useState<string[]>(
        initialData?.cloudAccounts ?? cloudAccounts.map((account) => account.id)
    );

    const [submit, setSubmit] = useState(false);

    const [categories, setCategories] = useState<string[]>([]);

    const [search, setSearch] = useState("");

    const [accountDropdown, setAccountDropdown] = useState<"all" | "specific">(
        initialData && initialData.cloudAccounts.length < cloudAccounts.length ? "specific" : "all"
    );

    const [accountSearch, setAccountSearch] = useState("");

    const groupEvents = useMemo(() => {
        const filteredEvents = eventsAvailable.filter((event) => {
            const eventSearch = search.toLowerCase().trim();

            if (!eventSearch) {
                return true;
            }

            return (
                event.description.toLowerCase().includes(eventSearch) ||
                event.category.toLowerCase().includes(eventSearch)
            );
        });

        return filteredEvents.reduce(
            (account, event) => {
                if (!account[event.category]) {
                    account[event.category] = [];
                }

                account[event.category].push(event);

                return account;
            },
            {} as Record<string, WebhookEvent[]>
        );
    }, [eventsAvailable, search]);

    const toggleCategory = (category: string) => {
        setCategories((previous) =>
            previous.includes(category)
                ? previous.filter((forPreviousCategory) => forPreviousCategory !== category)
                : [...previous, category]
        );
    };

    const toggleEvents = (eventId: string) => {
        setEventsSelected((previous) =>
            previous.includes(eventId)
                ? previous.filter((id) => id !== eventId)
                : [...previous, eventId]
        );
    };

    const toggleCategoryEvents = (events: WebhookEvent[]) => {
        const eventIds = events.map((event) => event.id);

        const selectAll = eventIds.every((id) => eventsSelected.includes(id));

        setEventsSelected((previous) => {
            if (selectAll) {
                return previous.filter((id) => !eventIds.includes(id));
            }

            return [...previous, ...eventIds.filter((id) => !previous.includes(id))];
        });
    };

    const toggleAccount = (accountId: string) => {
        setAccountsSelected((previous) =>
            previous.includes(accountId)
                ? previous.filter((forId) => forId !== accountId)
                : [...previous, accountId]
        );
    };

    const handlingSubmit = async () => {
        if (!name || !endpointUrl || eventsSelected.length === 0) {
            return;
        }

        setSubmit(true);

        try {
            const payload: CreateWebhookPayload = {
                name,
                endpointUrl,
                eventTypes: eventsSelected,
                cloudAccounts: accountsSelected,
            };

            if (initialData) {
                await editWebhook(initialData.id, { ...payload, status: initialData.status });
                onSuccess("");
            } else {
                const result = await addWebhook(payload);
                onSuccess(result.secret);
            }
            onClose();
        } catch (error) {
            const errorMessage =
                error instanceof Error ? error.message : "Failed to save the webhook";
            alert(errorMessage);
        } finally {
            setSubmit(false);
        }
    };

    let confirmationLabel = "Create webhook";
    if (submit) {
        confirmationLabel = "Saving";
    } else if (initialData) {
        confirmationLabel = "Save changes";
    }

    const filteredCloudAccounts = useMemo(() => {
        const wordSearched = accountSearch.toLowerCase().trim();

        if (!wordSearched) {
            return cloudAccounts;
        }

        return cloudAccounts.filter(
            (account) =>
                account.displayName.toLowerCase().includes(wordSearched) ||
                account.accountType.toLowerCase().includes(wordSearched)
        );
    }, [cloudAccounts, accountSearch]);

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogContent
                className="w-[95vw] max-h-[90vh] overflow-y-auto"
                style={{ maxWidth: "1000px" }}
            >
                <DialogHeader className="pb-1">
                    <DialogTitle> {initialData ? "Edit webhook" : "Add webhook"} </DialogTitle>
                </DialogHeader>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="space-y-1.5">
                        <Label> Name </Label>

                        <Input
                            value={name}
                            onChange={(change) => setName(change.target.value)}
                            placeholder="e.g. Cost reporting"
                        />
                    </div>

                    <div className="space-y-1.5">
                        <Label> Endpoint URL </Label>

                        <Input
                            value={endpointUrl}
                            onChange={(change) => setEndpointUrl(change.target.value)}
                            placeholder="https://example.com/webhooks"
                        />
                    </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 min-h-0">
                    <div className="rounded-md border border-border bg-background overflow-hidden">
                        <div className="px-4 pt-4">
                            <Label> Events </Label>

                            <p className="text-xs text-muted-foreground mt-1">
                                {" "}
                                Select categories or individual events.{" "}
                            </p>

                            <div className="relative mt-3">
                                <Search
                                    size={15}
                                    className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground"
                                />

                                <Input
                                    value={search}
                                    onChange={(change) => setSearch(change.target.value)}
                                    placeholder="Search events"
                                    className="h-8 pl-9 text-xs"
                                />
                            </div>
                        </div>

                        <ScrollArea className="h-[285px] mt-3">
                            <div>
                                {Object.entries(groupEvents).map(([category, events]) => {
                                    const categoryEventIds = events.map((event) => event.id);

                                    const selectedCount = categoryEventIds.filter((id) =>
                                        eventsSelected.includes(id)
                                    ).length;

                                    const categorySelected =
                                        events.length > 0 && selectedCount === events.length;

                                    const categoryPartiallySelected =
                                        selectedCount > 0 && selectedCount < events.length;

                                    return (
                                        <div key={category} className="border-t border-border">
                                            <div className="flex items-center gap-2 px-4 py-2.5">
                                                <button
                                                    type="button"
                                                    onClick={() => toggleCategory(category)}
                                                    className="flex items-center justify-center text-muted-foreground hover:text-foreground"
                                                >
                                                    {categories.includes(category) ? (
                                                        <ChevronDown size={14} />
                                                    ) : (
                                                        <ChevronRight size={14} />
                                                    )}
                                                </button>

                                                <Checkbox
                                                    id={`category-${category}`}
                                                    checked={
                                                        categoryPartiallySelected
                                                            ? "indeterminate"
                                                            : categorySelected
                                                    }
                                                    onCheckedChange={() =>
                                                        toggleCategoryEvents(events)
                                                    }
                                                />

                                                <button
                                                    type="button"
                                                    onClick={() => toggleCategory(category)}
                                                    className="flex-1 text-left text-sm font-semibold"
                                                >
                                                    {" "}
                                                    {category}{" "}
                                                </button>

                                                <span className="text-[10px] text-muted-foreground">
                                                    {" "}
                                                    {events.length} events{" "}
                                                </span>
                                            </div>

                                            {categories.includes(category) && (
                                                <div className="px-10 pb-2">
                                                    {events.map((event) => (
                                                        <div
                                                            key={event.id}
                                                            className="flex items-center gap-2 py-1.5"
                                                        >
                                                            <Checkbox
                                                                id={event.id}
                                                                checked={eventsSelected.includes(
                                                                    event.id
                                                                )}
                                                                onCheckedChange={() =>
                                                                    toggleEvents(event.id)
                                                                }
                                                            />

                                                            <Label
                                                                htmlFor={event.id}
                                                                className="text-sm font-normal cursor-pointer"
                                                            >
                                                                {" "}
                                                                {event.description}{" "}
                                                            </Label>
                                                        </div>
                                                    ))}
                                                </div>
                                            )}
                                        </div>
                                    );
                                })}

                                {Object.keys(groupEvents).length === 0 && (
                                    <div className="flex items-center justify-center h-40 text-xs text-muted-foreground">
                                        {" "}
                                        No events found{" "}
                                    </div>
                                )}
                            </div>
                        </ScrollArea>

                        <div className="border-t border-border px-4 py-2.5">
                            <p className="text-[10px] text-muted-foreground">
                                {" "}
                                {eventsSelected.length} events selected{" "}
                            </p>
                        </div>
                    </div>

                    <div className="rounded-md border border-border bg-background overflow-hidden flex flex-col">
                        <div className="px-4 pt-4">
                            <div className="flex items-start justify-between gap-4">
                                <div>
                                    <Label> Cloud accounts </Label>

                                    <p className="text-xs text-muted-foreground mt-1">
                                        {" "}
                                        Applies to all selected events.{" "}
                                    </p>
                                </div>

                                <Select
                                    value={accountDropdown}
                                    onValueChange={(value) => {
                                        const selected = value as "all" | "specific";
                                        setAccountDropdown(selected);
                                        if (selected === "all") {
                                            setAccountsSelected(
                                                cloudAccounts.map((account) => account.id)
                                            );
                                        } else {
                                            setAccountsSelected([]);
                                        }
                                    }}
                                >
                                    <SelectTrigger className="w-[180px] h-9 text-xs">
                                        {" "}
                                        <SelectValue />{" "}
                                    </SelectTrigger>

                                    <SelectContent>
                                        <SelectItem value="all">
                                            {" "}
                                            All connected accounts{" "}
                                        </SelectItem>
                                        <SelectItem value="specific">
                                            {" "}
                                            Specific accounts{" "}
                                        </SelectItem>
                                    </SelectContent>
                                </Select>
                            </div>
                        </div>

                        {accountDropdown === "all" ? (
                            <div className="border-t border-border mt-4 h-[285px] flex flex-col items-center justify-center px-8 text-center">
                                <Cloud size={32} strokeWidth={1.5} className="text-primary mb-3" />

                                <p className="text-sm font-semibold text-foreground">
                                    {" "}
                                    All connected accounts{" "}
                                </p>

                                <p className="text-xs text-muted-foreground mt-2 max-w-[280px] leading-5">
                                    {" "}
                                    Events from all {cloudAccounts.length} connected accounts are
                                    included. Choose &quot;Specific accounts&quot; to narrow the
                                    selection.
                                </p>
                            </div>
                        ) : (
                            <>
                                <div className="px-4 pt-3">
                                    <div className="relative">
                                        <Search
                                            size={15}
                                            className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground"
                                        />

                                        <Input
                                            value={accountSearch}
                                            onChange={(change) =>
                                                setAccountSearch(change.target.value)
                                            }
                                            placeholder="Search accounts"
                                            className="h-9 pl-9 text-xs"
                                        />
                                    </div>
                                </div>

                                <ScrollArea className="h-[235px] mt-3">
                                    <div>
                                        {filteredCloudAccounts.map((account) => (
                                            <div
                                                key={account.id}
                                                className="flex items-start gap-3 border-t border-border px-4 py-3"
                                            >
                                                <Checkbox
                                                    id={`account-${account.id}`}
                                                    checked={accountsSelected.includes(account.id)}
                                                    onCheckedChange={() =>
                                                        toggleAccount(account.id)
                                                    }
                                                    className="mt-1"
                                                />

                                                <label
                                                    htmlFor={`account-${account.id}`}
                                                    className="flex flex-col cursor-pointer"
                                                >
                                                    <span className="text-sm font-medium">
                                                        {" "}
                                                        {account.displayName}{" "}
                                                    </span>

                                                    <span className="text-xs text-muted-foreground">
                                                        {" "}
                                                        {ACCOUNT_TYPES[account.accountType] ??
                                                            account.accountType}{" "}
                                                    </span>
                                                </label>
                                            </div>
                                        ))}

                                        {filteredCloudAccounts.length === 0 && (
                                            <div className="flex items-center justify-center h-40 text-xs text-muted-foreground">
                                                {" "}
                                                No accounts found{" "}
                                            </div>
                                        )}
                                    </div>
                                </ScrollArea>

                                <div className="border-t border-border px-4 py-2.5">
                                    <p className="text-[10px] text-muted-foreground">
                                        {" "}
                                        {accountsSelected.length} of {cloudAccounts.length} accounts
                                        selected{" "}
                                    </p>
                                </div>
                            </>
                        )}
                    </div>
                </div>

                <DialogFooter>
                    <Button variant="outline" onClick={onClose}>
                        {" "}
                        Cancel{" "}
                    </Button>

                    <Button
                        onClick={handlingSubmit}
                        disabled={submit || !name || !endpointUrl || eventsSelected.length === 0}
                    >
                        {" "}
                        {confirmationLabel}{" "}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};
