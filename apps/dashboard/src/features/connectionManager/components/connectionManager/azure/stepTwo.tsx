"use client";

import React, { useState } from "react";
import { StepTwo } from "@/features/connectionManager/components/connectionManager/wizardSetup/stepTwo";
import { Button } from "@/components/atoms/button";

interface ResourcesDetails {
    id: string;
    name: string;
    type: string;
}

interface StepTwoPropsForAzure {
    onNext: (forData: { servicesSelected: string[]; resources: ResourcesDetails[] }) => void;

    onBack?: () => void;
}

const HARDCODED = [{ id: "idOne", name: "ServiceOne" }];

export default function StepTwoAzure({ onNext, onBack }: Readonly<StepTwoPropsForAzure>) {
    const [servicesAvailable] = useState<{ id: string; name: string }[]>(HARDCODED);
    const [selectedService, setSelectedService] = useState<string[]>([]);

    const [forLoading, setForLoading] = useState(false);

    const [forErrors, setForErrors] = useState("");

    const handlingSubmit = async (forSubmitting: React.SubmitEvent<HTMLFormElement>) => {
        forSubmitting.preventDefault();

        try {
            setForLoading(true);
            setForErrors("");

            const discoveredResources: ResourcesDetails[] = [
                { id: "azure-resource-1", name: "Resource 1", type: "typeOne" },
            ];

            onNext({ servicesSelected: selectedService, resources: discoveredResources });
        } catch {
            setForErrors("Failed to discover any resources");
        } finally {
            setForLoading(false);
        }
    };

    const checkingServices = (idForService: string) => {
        setSelectedService((previous) =>
            previous.includes(idForService)
                ? previous.filter((ids) => ids != idForService)
                : [...previous, idForService]
        );
    };

    const handlingSelectedAll = () => {
        if (selectedService.length === servicesAvailable.length) {
            setSelectedService([]);
        } else {
            setSelectedService(servicesAvailable.map((forServices) => forServices.id));
        }
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

                    <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        onClick={handlingSelectedAll}
                        className="text-primary hover:text-accent text-sm transition-colors px-0"
                    >
                        {" "}
                        {selectedService.length === servicesAvailable.length
                            ? "Deselect All"
                            : "Select All"}{" "}
                    </Button>
                </div>

                <div className="space-y-3">
                    {servicesAvailable.map((forServices) => (
                        <label
                            key={forServices.id}
                            className="flex items-center gap-3 w-full p-4 bg-background rounded-lg border border-border hover:border-primary/40 transition-all cursor-pointer focus-within:ring-2 focus-within:ring-primary focus-within:ring-offset-2"
                        >
                            <input
                                type="checkbox"
                                checked={selectedService.includes(forServices.id)}
                                onChange={() => checkingServices(forServices.id)}
                                className="w-4 h-4 rounded border-border bg-background text-primary focus:ring-2 focus:ring-primary"
                            />

                            <span className="text-foreground font-medium">
                                {" "}
                                {forServices.name}{" "}
                            </span>
                        </label>
                    ))}
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
