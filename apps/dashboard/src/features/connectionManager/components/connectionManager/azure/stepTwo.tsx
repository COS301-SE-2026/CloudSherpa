"use client";

import React, { useEffect, useState } from "react";
import { StepTwo } from "@/features/connectionManager/components/connectionManager/wizardSetup/stepTwo";
import { Button } from "@/components/atoms/button";
import { ResourceDetail } from "@/lib/fetch/dto/cloud-resource";
import {
    generateAzurePermissionsPolicy,
    getCloudResources,
    getCloudServices,
} from "@/lib/fetch/cloud-resource-api";
import { Progress } from "@/components/atoms/progress";
import { CloudCredentials } from "@/lib/fetch/dto/cloud-credentials";

//copied and pasted from previous pr
interface StepTwoPropsForAzure {
    credentials: CloudCredentials | null;
    onNext: (forData: { servicesSelected: string[]; resources: ResourceDetail[] }) => void;

    onBack?: () => void;
}

export default function StepTwoAzure({
    credentials,
    onNext,
    onBack,
}: Readonly<StepTwoPropsForAzure>) {
    const [servicesAvailable, setServicesAvailable] = useState<{ id: string; name: string }[]>([]);

    const [selectedService, setSelectedService] = useState<string[]>([]);

    const [forLoading, setForLoading] = useState(false);

    const [forErrors, setForErrors] = useState("");

    const [progress, setProgress] = useState(0);

    const [currentScanningService, setCurrentScanningService] = useState("");

    const [permissions, setPermissions] = useState<string[]>([]);

    React.useEffect(() => {
        const loadPermissions = async () => {
            if (selectedService.length === 0) {
                setPermissions([]);
                return;
            }

            try {
                const result = await generateAzurePermissionsPolicy(selectedService);

                setPermissions(result);
            } catch {
                setForErrors("Failed to determine required Azure permissions");
            }
        };

        loadPermissions();
    }, [selectedService]);

    useEffect(() => {
        const loadServices = async () => {
            const services = await getCloudServices("azure");

            setServicesAvailable(
                services.map((s) => ({
                    id: s,
                    name: s.toUpperCase(),
                }))
            );
        };

        loadServices();
    }, []);

    const handlingSubmit = async (forHandlingSubmit: React.FormEvent<HTMLFormElement>) => {
        forHandlingSubmit.preventDefault();
        try {
            setForLoading(true);
            setForErrors("");
            setProgress(0);
            setCurrentScanningService("");

            let discoveredResources: ResourceDetail[] = [];
            for (let i = 0; i < selectedService.length; i++) {
                const currentService = selectedService[i];

                // Update UI text
                setCurrentScanningService(currentService);

                const resources = await getCloudResources(
                    "azure",
                    {
                        subscriptionId: credentials?.subscriptionId,
                        tenantId: credentials?.tenantId,
                        clientId: credentials?.clientId,
                        clientSecret: credentials?.clientSecret,
                    },
                    [currentService]
                );

                discoveredResources = [...discoveredResources, ...resources];

                setProgress(((i + 1) / selectedService.length) * 100);
            }

            if (discoveredResources.length === 0) {
                setForErrors("No resources were discovered.");
                return;
            }

            onNext({ servicesSelected: selectedService, resources: discoveredResources });
        } catch (err) {
            console.error(err);

            setForErrors("Failed to discover resources. Check credentials and permissions.");
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
                <div className="rounded-lg border border-border bg-background p-4 space-y-3">
                    {permissions.length === 0 ? (
                        <p className="text-sm text-muted-foreground/70">
                            {" "}
                            Select a service to view the permissions{" "}
                        </p>
                    ) : (
                        permissions.map((permissions) => (
                            <div
                                key={permissions}
                                className="rounded-md bg-card px-4 py-3 text-sm text-foreground"
                            >
                                {" "}
                                - {permissions}{" "}
                            </div>
                        ))
                    )}
                </div>
            </section>
            {forLoading && (
                <div className="space-y-2 w-full pt-4">
                    <div className="flex justify-between text-sm text-muted-foreground font-medium">
                        <span>
                            {currentScanningService
                                ? `Scanning ${currentScanningService.toUpperCase()}...`
                                : "Preparing scan..."}
                        </span>
                        <span>{Math.round(progress)}%</span>
                    </div>
                    <Progress value={progress} className="w-full h-2" />
                </div>
            )}
        </StepTwo>
    );
}
