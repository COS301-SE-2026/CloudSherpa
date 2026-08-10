"use client";
import React, { useState } from "react";
import { Input } from "@/components/atoms/input";
import { Label } from "@/components/atoms/label";
import { StepOne } from "@/features/connectionManager/components/connectionManager/wizardSetup/stepOne";

interface PropsForStepOne {
    onNext: (data: {
        displayName: string;
        accessKey: string;
        secretKey: string;
        awsRegion: string;
    }) => void;
}

export default function StepOneAws({ onNext }: Readonly<PropsForStepOne>) {
    const [formData, setFormData] = useState({
        displayName: "",
        accessKey: "",
        secretKey: "",
        awsRegion: "af-south-1",
    });

    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = (forHandlingSubmit: React.SubmitEvent<HTMLFormElement>) => {
        forHandlingSubmit.preventDefault();

        setIsSubmitting(true);

        try {
            onNext(formData);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <StepOne
            heading="Link your AWS account"
            description="Please log in to your AWS account and create a new IAM user. Enter the information below to begin the process of connecting your account."
            onSubmit={handleSubmit}
            isSubmitting={isSubmitting}
        >
            <div className="space-y-2">
                <Label htmlFor="displayName" className="text-foreground text-sm font-medium">
                    Account name
                </Label>

                <Input
                    id="displayName"
                    type="text"
                    placeholder="AWS Connection"
                    value={formData.displayName}
                    onChange={(e) => setFormData({ ...formData, displayName: e.target.value })}
                    className="bg-background border-border rounded-md px-4 py-3 text-foreground placeholder:text-muted-foreground/40 focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent transition-all w-full"
                    required
                />
            </div>

            <div className="space-y-2">
                <Label htmlFor="accessKeyId" className="text-foreground text-sm font-medium">
                    Access key ID
                </Label>

                <Input
                    id="accessKeyId"
                    type="text"
                    placeholder="EXAMPLE"
                    value={formData.accessKey}
                    onChange={(e) => setFormData({ ...formData, accessKey: e.target.value })}
                    className="bg-background border-border rounded-md px-4 py-3 text-foreground placeholder:text-muted-foreground/40 focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent transition-all w-full"
                    required
                />
            </div>

            <div className="space-y-2">
                <Label htmlFor="secretAccessKey" className="text-foreground text-sm font-medium">
                    Secret access key
                </Label>

                <Input
                    id="secretAccessKey"
                    type="password"
                    placeholder="••••••••••••••••••••••••"
                    value={formData.secretKey}
                    onChange={(e) => setFormData({ ...formData, secretKey: e.target.value })}
                    className="bg-background border-border rounded-md px-4 py-3 text-foreground placeholder:text-muted-foreground/40 focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent transition-all w-full"
                    required
                />
            </div>
        </StepOne>
    );
}
