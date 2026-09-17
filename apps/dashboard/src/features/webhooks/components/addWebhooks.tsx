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
                    
                </div>

                <DialogFooter>
                    <Button variant = "outline" onClick = {onClose}> Cancel </Button>

                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};