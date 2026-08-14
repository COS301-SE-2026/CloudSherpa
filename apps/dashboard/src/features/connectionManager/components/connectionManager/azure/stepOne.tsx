"use client";

import React from "react";
import { Input } from "@/components/atoms/input";
import { Label } from "@/components/atoms/label";
import { StepOne } from "@/features/connectionManager/components/connectionManager/wizardSetup/stepOne";

interface StepOnePropsForAzure {
    onNext: (data: { name: string }) => void;
}

export default function StepOneAzure({ onNext }: Readonly<StepOnePropsForAzure>) {
    const handlingSubmit = (submitting: React.SubmitEvent<HTMLFormElement>) => {
        submitting.preventDefault();

        onNext({ name: "" });
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
                    className="bg-background border-border rounded-md px-4 py-3 text-foreground placeholder:text-muted-foreground/40 focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent transition-all w-full"
                />
            </div>
        </StepOne>
    );
}
