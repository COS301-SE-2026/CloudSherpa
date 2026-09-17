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
            <div>
                <div className = "flex justify-between items-center mb-2">
                    <h3 className = "text-sm font-semibold text-foreground"> Request headers </h3>

                    <Button variant = "outline" size = "sm" onClick = {() => handlingCopy(JSON.stringify(headers, null, 2), setCopyHeaders)}>
                        {copyHeaders ? (
                            <Check size = {12} className = "mr-1"/>
                        ) : (
                            <Copy size = {12} className = "mr-1"/>
                        )}

                        {copyHeaders ? "Copied to clipboard." : "Copy headers"}
                    </Button>
                </div>

                <pre className = "bg-background border border-border rounded-md p-3 text-xs overflow-x-auto text-foreground"> {JSON.stringify(headers, null, 2)} </pre>
            </div>
        </div>
    );
};