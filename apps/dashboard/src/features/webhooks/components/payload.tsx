"use client";

import {useState} from "react";
import {Copy, Check} from "lucide-react";
import {WebhookEvent} from "@/features/webhooks/types";
import {Button} from "@/components/atoms/button";

interface PropsForPayload{
    event : WebhookEvent;
}

export const ExampleForPayload = ({event} : PropsForPayload) => {
    const [copyHeaders, setCopyHeaders] = useState(false);

    const [copyBody, setCopyBody] = useState(false);

    const headers : Record<string, string> = event.requestHeaders ?? {};

    const body : Record<string, unknown> = event.jsonBody ?? {};

    const handlingCopy = (text : string, setFlag : (value : boolean) => void) => {
        navigator.clipboard.writeText(text);

        setFlag(true);

        setTimeout(() => setFlag(false), 2000);
    };

    return(
        <div className = "space-y-6">
            
        </div>
    );
};