"use client";

import {useEffect, useState, useMemo} from "react";
import {ChevronDown, ChevronRight, Cloud, Search} from "lucide-react";
import {WebhookEvent, CreateWebhookPayload, Webhook, CloudAccount} from "@/features/webhooks/types";
import {addWebhook, editWebhook} from "@/features/webhooks/webhooks";
import {Button} from "@/components/atoms/button";
import {Input} from "@/components/atoms/input";
import {Label} from "@/components/atoms/label";
import {Checkbox} from "@/components/atoms/checkbox";
import {ScrollArea} from "@/components/atoms/scroll-area";
import {Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter} from "@/components/atoms/dialog";

interface PropsForAddingWebhooks{
    isOpen : boolean;
    onClose : () => void;
    eventsAvailable : WebhookEvent[];
    cloudAccounts : CloudAccount[];
    initialData?: Webhook | null;
    onSuccess : (secret : string) => void;
}

export const AddWebhook = ({
    isOpen, onClose, eventsAvailable, cloudAccounts, initialData, onSuccess,
} : PropsForAddingWebhooks) => {
    const [name, setName] = useState("");

    const [endpointUrl, setEndpointUrl] = useState("");

    const [eventsSelected, setEventsSelected] = useState<string[]>([]);

    const [accountsSelected, setAccountsSelected] = useState<string[]>([]);

    const [submit, setSubmit] = useState(false);

    const [categories, setCategories] = useState<string[]>([]);

    const [accountDropdownOpen, setAccountDropdownOpen] = useState(false);

    const [search, setSearch] = useState("");

    // useEffect(() => {
    //     if(initialData){
    //         setName(initialData.name);
    //         setEndpointUrl(initialData.endpointUrl);
    //         setEventsSelected(initialData.eventTypes);
    //         setAccountsSelected(initialData.cloudAccounts);
    //     }else{
    //         setName("");
    //         setEndpointUrl("");
    //         setEventsSelected([]);
    //         setAccountsSelected(cloudAccounts.map((account) => account.id));
    //     }
    // }, [initialData, isOpen, cloudAccounts]);

    const groupEvents = useMemo(() => {
        const filteredEvents = eventsAvailable.filter((event) => {
            const eventSearch = search.toLowerCase().trim();

            if(!eventSearch){
                return true;
            }

            return(event.description.toLowerCase().includes(eventSearch) || event.category.toLowerCase().includes(eventSearch));
        });

        return filteredEvents.reduce((account, event) => {
            if(!account[event.category]){
                account[event.category] = [];
            }

            account[event.category].push(event);

            return account;
        }, {} as Record<string, WebhookEvent[]>);
    }, [eventsAvailable, search]);

    const toggleCategory = (category : string) => {
        setCategories((previous) => previous.includes(category) ? previous.filter((forPreviousCategory) => forPreviousCategory !== category) : [...previous, category]);
    };

    const toggleEvents = (eventId : string) => {
        setEventsSelected((previous) => previous.includes(eventId) ? previous.filter((id) => id !== eventId) : [...previous, eventId]);
    };

    const toggleCategoryEvents = (events : WebhookEvent[]) => {
        const eventIds = events.map((event) => event.id);

        const selectAll = eventIds.every((id) => eventsSelected.includes(id));

        setEventsSelected((previous) => {
            if(selectAll){
                return previous.filter((id) => !eventIds.includes(id));
            }

            return[...previous, ...eventIds.filter((id) => !previous.includes(id))];
        });
    };

    const toggleAccount = (accountId : string) => {
        setAccountsSelected((previous) => previous.includes(accountId) ? previous.filter((forId) => forId !== accountId) : [...previous, accountId]);
    };

    const handlingSubmit = async () => {
        if(!name || !endpointUrl || eventsSelected.length === 0){
            return;
        }

        setSubmit(true);

        try{
            const payload : CreateWebhookPayload = {
                name, endpointUrl, eventTypes : eventsSelected, cloudAccounts : accountsSelected,
            };

            if(initialData){
                await editWebhook(initialData.id, {...payload, status : initialData.status});
                onSuccess("");
            }else{
                const result = await addWebhook(payload);
                onSuccess(result.secret);
            }
            onClose();
        }catch(error){
            const errorMessage = error instanceof Error ? error.message : "Failed to save the webhook";
            alert(errorMessage);
        }finally{
            setSubmit(false);
        }
    };

    const allAccountsSelected = cloudAccounts.length>0 && accountsSelected.length === cloudAccounts.length;

    let confirmationLabel = "Create webhook";
    if(submit){
        confirmationLabel = "Saving";
    }else if(initialData){
        confirmationLabel = "Save changes";
    }

    return(
        <Dialog open = {isOpen} onOpenChange = {onClose}>
            <DialogContent className = "w-[95vw] max-h-[90vh] overflow-y-auto" style = {{maxWidth : "1000px"}}>
                <DialogHeader className = "pb-1">
                    <DialogTitle> {initialData ? "Edit webhook" : "Add webhook"} </DialogTitle>
                </DialogHeader>

                <div className = "grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className = "space-y-1.5">
                        <Label> Name </Label>

                        <Input value = {name} onChange = {(change) => setName(change.target.value)} placeholder = "e.g. Cost reporting"/>
                    </div>

                    <div className = "space-y-1.5">
                        <Label> Endpoint URL </Label>

                        <Input value = {endpointUrl} onChange = {(change) => setEndpointUrl(change.target.value)} placeholder = "https://example.com/webhooks"/>
                    </div>
                </div>

                <div className = "grid grid-cols-1 md:grid-cols-2 gap-4 min-h-0">
                    <div className = "rounded-md border border-border bg-background overflow-hidden">
                        <div className = "px-4 pt-4">
                            <Label> Events </Label>

                            <p className = "text-xs text-muted-foreground mt-1"> Select categories or individual events. </p>

                            <div className = "relative mt-3">
                                <Search size = {15} className = "absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground"/>

                                <Input value = {search} onChange = {(change) => setSearch(change.target.value)} placeholder = "Search events" className = "h-8 pl-9 text-xs"/>
                            </div>
                        </div>

                            <ScrollArea className = "h-[285px] mt-3">
                                <div>
                                    {Object.entries(groupEvents).map(([category, events]) => {
                                        const categoryEventIds = events.map((event) => event.id);

                                        const selectedCount = categoryEventIds.filter((id) => eventsSelected.includes(id)).length;

                                        const categorySelected = events.length>0 && selectedCount === events.length;

                                        const categoryPartiallySelected = selectedCount>0 && selectedCount<events.length;

                                        return(
                                            <div key = {category} className = "border-t border-border">
                                                <div className = "flex items-center gap-2 px-4 py-2.5">
                                                    <button type = "button" onClick = {() => toggleCategory(category)} className = "flex items-center justify-center text-muted-foreground hover:text-foreground">
                                                        {categories.includes(category) ? (<ChevronDown size = {14}/> ) : (<ChevronRight size = {14}/> )}
                                                    </button>

                                                    <Checkbox id = {`category-${category}`} checked = {categoryPartiallySelected ? "indeterminate" : categorySelected} onCheckedChange = {() => toggleCategoryEvents(events)}/>
                                                    
                                                    <button type = "button" onClick = {() => toggleCategory(category)} className = "flex-1 text-left text-xs font-semibold"> {category} </button>

                                                    <span className = "text-[10px] text-muted-foreground"> {events.length} events </span>
                                                </div>

                                                {categories.includes(category) && (
                                                    <div className = "px-10 pb-2">
                                                        {events.map((event) => (
                                                            <div key = {event.id} className = "flex items-center gap-2 py-1.5">
                                                                <Checkbox id = {event.id} checked = {eventsSelected.includes(event.id)} onCheckedChange = {() => toggleEvents(event.id)}/>

                                                                <Label htmlFor = {event.id} className = "text-xs font-normal cursor-pointer"> {event.description} </Label>
                                                            </div>
                                                        ))}
                                                    </div>
                                                )}
                                            </div>
                                        );
                                    },)}

                                    {Object.keys(groupEvents).length === 0 && (
                                        <div className = "flex items-center justify-center h-40 text-xs text-muted-foreground"> No events found </div>
                                    )}
                                </div>
                            </ScrollArea>

                            <div className = "border-t border-border px-4 py-2.5">
                                <p className = "text-[10px] text-muted-foreground"> {eventsSelected.length} events selected </p>
                            </div>
                    </div>

                    <div className = "rounded-md border border-border bg-background overflow-hidden">
                        <div className = "px-4 pt-4">
                            <div className = "flex items-start justify-between gap-4">
                                <div>
                                    <Label> Cloud accounts </Label>

                                    <p className = "text-xs text-muted-foreground mb-2"> Applies to all selected events. </p>
                                </div>

                                <div className = "relative">
                                    <Button variant = "outline" onClick = {() => setAccountDropdownOpen(!accountDropdownOpen)} className = "w-full justify-between">
                                        <span> {accountsSelected.length === cloudAccounts.length ? "All connected accounts" : `${accountsSelected.length} accounts selected`} </span>
                                        <ChevronDown size = {14}/>
                                    </Button>

                                    {accountDropdownOpen && (
                                        <div className = "absolute right-0 z-20 mt-1 w-64 rounded-md border border-border bg-popover p-2 shadow-lg">
                                            <div className = "max-h-52 overflow-y-auto space-y-1">
                                                {cloudAccounts.map((account) => (
                                                    <div key = {account.id} className = "flex items-center gap-2 p-2 hover:bg-muted rounded-md">
                                                        <Checkbox id = {account.id} checked = {accountsSelected.includes(account.id)} onCheckedChange = {() => toggleAccount(account.id)}/>

                                                        <Label htmlFor = {account.id} className = "text-sm font-normal cursor-pointer"> {account.displayName} </Label>
                                                    </div>
                                                ))}
                                            </div>
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>

                        <div className = "mt-3 border-t border-border h-[315px] flex flex-col items-center justify-center px-8 text-center">
                            <div className = "mb-3 flex h-10 w-10 items-center justify-center rounded-full">
                                <Cloud size = {32} strokeWidth = {1.5} className = "text-muted-foreground"/>
                            </div>

                            <p className = "text-xs text-muted-foreground"> {allAccountsSelected ? "All connected accounts" : `${accountsSelected.length} accounts selected`} </p>

                            <p className = "mt-1 max-w-[320px] text-[10px] leading-4 text-muted-foreground"> {allAccountsSelected ? `Events from all ${cloudAccounts.length} connected accounts are included. Choose "Specific accounts" to narrow the selection.` : "Only events from the selected accounts will be included."} </p>
                        </div>
                    </div>
                </div>

                <DialogFooter>
                    <Button variant = "outline" onClick = {onClose}> Cancel </Button>

                    <Button onClick = {handlingSubmit} disabled = {submit || !name || !endpointUrl || eventsSelected.length === 0}> {confirmationLabel} </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};