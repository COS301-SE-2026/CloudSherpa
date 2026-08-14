"use client";

import React, { useState } from "react";
import { StepTwo } from "@/features/connectionManager/components/connectionManager/wizardSetup/stepTwo";

interface ResourcesDetails {
    id: string;
    name: string;
    type: string;
}

interface StepTwoPropsForAzure {
    onNext: (forData: { servicesSelected: string[]; resources: ResourcesDetails[] }) => void;

    onBack?: () => void;
}

export default function StepTwoAzure({ onNext, onBack }: Readonly<StepTwoPropsForAzure>) {
    const [selectedService] = useState<string[]>([]);

    const [forLoading] = useState(false);

    const [forErrors] = useState("");

    const handlingSubmit = async (forSubmitting: React.SubmitEvent<HTMLFormElement>) => {
        forSubmitting.preventDefault();

        onNext({ servicesSelected: selectedService, resources: [] });
    };

    return (
        <StepTwo
            heading="Select service"
            description="Choose which Azure service you want to monitor."
            onSubmit={handlingSubmit}
            onBack={onBack || (() => {})}
            forLoading={forLoading}
            forErrors={forErrors}
        >
            <section>
                <div className="flex flex-wrap items-center justify-between gap-2 mb-4">
                    <h3 className="text-foreground text-sm font-semibold uppercase tracking-wider opacity-80">
                        {" "}
                        Services we offer{" "}
                    </h3>
                </div>
            </section>

            <section>
                <h3 className="text-foreground text-sm font-semibold uppercase tracking-wider opacity-80 mb-4">
                    {" "}
                    Permissions{" "}
                </h3>
            </section>
        </StepTwo>
    );
}
