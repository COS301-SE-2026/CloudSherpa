"use client";

import {useEffect, useState} from "react";
import {ChevronDown, ChevronRight} from "lucide-react";
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

    useEffect(() => {
        if(initialData){
            setName(initialData.name);
            setEndpointUrl(initialData.endpointUrl);
            setEventsSelected(initialData.eventTypes);
            setAccountsSelected(initialData.cloudAccounts);
        }else{
            setName("");
            setEndpointUrl("");
            setEventsSelected([]);
            setAccountsSelected(cloudAccounts.map((account) => account.id));
        }
    }, [initialData, isOpen, cloudAccounts]);

    const groupEvents = eventsAvailable.reduce((account, event) => {
        if(!account[event.category]){
            account[event.category] = [];
        }

        account[event.category].push(event);

        return account;
    }, {} as Record<string, WebhookEvent[]>);

    const toggleCategory = (category : string) => {
        setCategories((previous) => previous.includes(category) ? previous.filter((forPreviousCategory) => forPreviousCategory !== category) : [...previous, category]);
    };

    const toggleEvents = (eventId : string) => {
        setEventsSelected((previous) => previous.includes(eventId) ? previous.filter((id) => id !== eventId) : [...previous, eventId]);
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

    return(
        <Dialog open = {isOpen} onOpenChange = {onClose}>
            <DialogContent className = "max-w-4xl max-h-[90vh] overflow-y-auto">
                <DialogHeader>
                    <DialogTitle> {initialData ? "Edit webhook" : "Add webhook"} </DialogTitle>
                </DialogHeader>

                <div className = "grid grid-cols-1 md:grid-cols-2 gap-6 py-4">
                    <div className = "space-y-2">
                        <Label> Name </Label>

                        <Input value = {name} onChange = {(change) => setName(change.target.value)} placeholder = "e.g. Cost reporting"/>
                    </div>

                    <div className = "space-y-2">
                        <Label> Endpoint URL </Label>

                        <Input value = {endpointUrl} onChange = {(change) => setEndpointUrl(change.target.value)} placeholder = "https://example.com/webhooks"/>
                    </div>
                </div>

                <div className = "grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className = "space-y-2">
                        <Label> Events </Label>

                        <p className = "text-xs text-muted-foreground mb-2"> Select categories or individual events. </p>

                        <ScrollArea className = "h-60 rounded-md border border-border bg-background">
                            {Object.entries(groupEvents).map(([category, events]) => (
                                <div key = {category} className = "border-b border-border last:border-0">
                                    <Button variant = "ghost" onClick = {() => toggleCategory(category)} className = "w-full justify-between px-3 py-2 h-auto rounded-none">
                                        <div className = "flex items-center gap-2">
                                            {categories.includes(category) ? (
                                                <ChevronDown size = {16}/>
                                            ) : (
                                                <ChevronRight size = {16}/>
                                            )}
                                            <span> {category} </span>
                                        </div>
                                        <span className = "text-muted-foreground text-xs"> {events.length} events </span>
                                    </Button>

                                    {categories.includes(category) && (
                                        <div className = "px-6 pb-2 space-y-1">
                                            {events.map((event) => (
                                                <div key = {event.id} className = "flex items-center gap-2 py-1">
                                                    <Checkbox id = {event.id} checked = {eventsSelected.includes(event.id)} onCheckedChange = {() => toggleEvents(event.id)}/>

                                                    <Label htmlFor = {event.id} className = "text-sm font-normal cursor-pointer"> {event.description} </Label>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>
                            ))}
                        </ScrollArea>

                        <p className = "text-xs text-muted-foreground mt-1"> {eventsSelected.length} events selected </p>
                    </div>

                    <div className = "space-y-2">
                        <Label> Cloud accounts </Label>

                        <p className = "text-xs text-muted-foreground mb-2"> Applies to all selected events. </p>

                        <div className = "relative">
                            <Button variant = "outline" onClick = {() => setAccountDropdownOpen(!accountDropdownOpen)} className = "w-full justify-between">
                                <span> {accountsSelected.length === cloudAccounts.length ? "All connected accounts" : `${accountsSelected.length} accounts selected`} </span>
                                <ChevronDown size = {16}/>
                            </Button>

                            {accountDropdownOpen && (
                                <div className = "absolute z-10 mt-1 w-full bg-popover border border-border rounded-md shadow-md max-h-60 overflow-y-auto p-2 space-y-1">
                                    {cloudAccounts.map((account) => (
                                        <div key = {account.id} className = "flex items-center gap-2 p-2 hover:bg-muted rounded-md">
                                            <Checkbox id = {account.id} checked = {accountsSelected.includes(account.id)} onCheckedChange = {() => toggleAccount(account.id)}/>

                                            <Label htmlFor = {account.id} className = "text-sm font-normal cursor-pointer"> {account.displayName} </Label>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                <DialogFooter>
                    <Button variant = "outline" onClick = {onClose}> Cancel </Button>

                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};