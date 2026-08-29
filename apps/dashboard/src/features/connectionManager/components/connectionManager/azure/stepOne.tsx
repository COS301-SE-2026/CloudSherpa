"use client";

import React, { useState } from "react";
import { Input } from "@/components/atoms/input";
import { Label } from "@/components/atoms/label";
import { StepOne } from "@/features/connectionManager/components/connectionManager/wizardSetup/stepOne";

//copied and pasted from previous pr

interface StepOnePropsForAzure {
    onNext: (data: {
        displayName: string;
        subscriptionId: string;
        tenantId: string;
        clientId: string;
        clientSecret: string;
    }) => void;
}

export default function StepOneAzure({ onNext }: Readonly<StepOnePropsForAzure>) {
    const [displayName, setDisplayName] = useState("");

    const [subscriptionId, setSubscriptionId] = useState("");

    const [tenantId, setTenantId] = useState("");

    const [clientId, setClientId] = useState("");

    const [clientSecret, setClientSecret] = useState("");

    const handlingSubmit = (submitting: React.SubmitEvent<HTMLFormElement>) => {
        submitting.preventDefault();

        onNext({ displayName, subscriptionId, tenantId, clientId, clientSecret });
    };

    return (
        <StepOne
            heading="Link your Azure account"
            description="Follow the steps to begin connecting your account"
            onSubmit={handlingSubmit}
            isSubmitting={false}
        >
            <div className="space-y-2">
                <Label htmlFor="name" className="text-foreground text-sm font-medium">
                    {" "}
                    Account name{" "}
                </Label>

                <Input
                    id="name"
                    type="text"
                    placeholder="Connection name"
                    value={displayName}
                    onChange={(forChanges) => setDisplayName(forChanges.target.value)}
                    className="bg-background border-border rounded-md px-4 py-3 text-foreground placeholder:text-muted-foreground/40 focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent transition-all w-full"
                    required
                />
            </div>

            <div className="space-y-2">
                <Label htmlFor="subscriptionID" className="text-foreground text-sm font-medium">
                    {" "}
                    Subscription ID{" "}
                </Label>

                <Input
                    id="subscriptionID"
                    type="text"
                    placeholder="Azure subscription ID"
                    value={subscriptionId}
                    onChange={(forChanges) => setSubscriptionId(forChanges.target.value)}
                    className="bg-background border-border rounded-md px-4 py-3 text-foreground placeholder:text-muted-foreground/40 focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent transition-all w-full"
                    required
                />
            </div>

            <div className="space-y-2">
                <Label htmlFor="tenantID" className="text-foreground text-sm font-medium">
                    {" "}
                    Tenant ID{" "}
                </Label>

                <Input
                    id="tenantID"
                    type="text"
                    placeholder="Tenant ID"
                    value={tenantId}
                    onChange={(forChanges) => setTenantId(forChanges.target.value)}
                    className="bg-background border-border rounded-md px-4 py-3 text-foreground placeholder:text-muted-foreground/40 focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent transition-all w-full"
                    required
                />
            </div>

            <div className="space-y-2">
                <Label htmlFor="clientID" className="text-foreground text-sm font-medium">
                    {" "}
                    Client ID{" "}
                </Label>

                <Input
                    id="clientID"
                    type="text"
                    placeholder="Client ID"
                    value={clientId}
                    onChange={(forChanges) => setClientId(forChanges.target.value)}
                    className="bg-background border-border rounded-md px-4 py-3 text-foreground placeholder:text-muted-foreground/40 focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent transition-all w-full"
                    required
                />
            </div>

            <div className="space-y-2">
                <Label htmlFor="clientSecret" className="text-foreground text-sm font-medium">
                    {" "}
                    Client Secret{" "}
                </Label>

                <Input
                    id="clientSecret"
                    type="password"
                    placeholder="••••••••••••••••••••••••"
                    onChange={(forChanges) => setClientSecret(forChanges.target.value)}
                    className="bg-background border-border rounded-md px-4 py-3 text-foreground placeholder:text-muted-foreground/40 focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent transition-all w-full"
                    required
                />
            </div>
        </StepOne>
    );
}
