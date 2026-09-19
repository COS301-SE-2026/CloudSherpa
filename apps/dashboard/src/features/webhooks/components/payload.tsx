"use client";

import { useState } from "react";
import { WebhookEvent } from "@/features/webhooks/types";
import { Button } from "@/components/atoms/button";

interface PropsForPayload {
    event: WebhookEvent;
}

export const ExampleForPayload = ({ event }: PropsForPayload) => {
    const [copyHeaders, setCopyHeaders] = useState(false);

    const [copyBody, setCopyBody] = useState(false);

    const headers: Record<string, string> = event.requestHeaders ?? {
        //copied
        "Content-Type": "application/json",
        "webhook-id": "msg_demo_cost_threshold_exceeded",
        "webhook-timestamp": "1789468938",
        "webhook-signature": "v1,Nev4L7n7f0Qa7Q/E55lY4Sm/mgOr2alOEQavuSeXtVg=",

        "Demo signing key": "",
    };

    const body: Record<string, unknown> = event.jsonBody ?? {
        //copied
        id: "msg_demo_cost_threshold_exceeded",
        type: "cost.threshold_exceeded",
        timestamp: "2026-09-15T10:42:18Z",
        account: {
            id: "aws-prod",
            name: "Production AWS",
            provider: "AWS",
        },
        data: {
            currency: "USD",
            cost: 1240,
            threshold: 1000,
            period_start: "2026-09-01T00:00:00Z",
            period_end: "2026-10-01T00:00:00Z",
        },
    };

    const handlingCopy = (text: string, setFlag: (value: boolean) => void) => {
        navigator.clipboard.writeText(text);

        setFlag(true);

        setTimeout(() => setFlag(false), 2000);
    };

    return (
        <div className="space-y-6">
            <div>
                <div className="flex justify-between items-center mb-2">
                    <h3 className="text-sm font-semibold text-foreground"> Request headers </h3>

                    <Button
                        variant="outline"
                        size="sm"
                        onClick={() =>
                            handlingCopy(JSON.stringify(headers, null, 2), setCopyHeaders)
                        }
                    >
                        {copyHeaders ? "Copied to clipboard." : "Copy headers"}
                    </Button>
                </div>

                <pre className="bg-background border border-border rounded-md p-3 text-xs overflow-x-auto text-foreground">
                    {" "}
                    {JSON.stringify(headers, null, 2)}{" "}
                </pre>
            </div>

            <div>
                <div className="flex justify-between items-center mb-2">
                    <h3 className="text-sm font-semibold text-foreground"> JSON body </h3>

                    <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handlingCopy(JSON.stringify(body, null, 2), setCopyBody)}
                    >
                        {copyBody ? "Copied to clipboard." : "Copy JSON"}
                    </Button>
                </div>

                <pre className="bg-background border border-border rounded-md p-3 text-xs overflow-x-auto text-foreground">
                    {" "}
                    {JSON.stringify(body, null, 2)}{" "}
                </pre>
            </div>
        </div>
    );
};
