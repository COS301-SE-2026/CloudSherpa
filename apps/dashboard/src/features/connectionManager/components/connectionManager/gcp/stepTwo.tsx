"use client";

import React, { useState } from "react";
import { StepTwo } from "@/features/connectionManager/components/connectionManager/wizardSetup/stepTwo";
import { Button } from "@/components/atoms/button";
import {
    getCloudServices,
    generateGcpPermissionsPolicy,
    getCloudResources,
    ResourceDetail,
} from "@/lib/fetch/cloud-resource-api";
import { GcpCredentialsDto } from "@/lib/fetch/gcp-connection-api";
import { GcpBillingForm } from "./billingForm";

interface StepTwoPropsForGcp {
    displayName: string;
    credentials: GcpCredentialsDto;
    onNext: (data: {
        displayName: string;
        servicesSelected: string[];
        resources: ResourceDetail[];
        credentials: GcpCredentialsDto;
    }) => void;
    onBack?: () => void;
}

export default function StepTwoGcp({
    displayName,
    credentials,
    onNext,
    onBack,
}: Readonly<StepTwoPropsForGcp>) {
    const [servicesAvailable, setServicesAvailable] = useState<string[]>([]);
    const [selectedServices, setSelectedServices] = useState<string[]>([]);
    const [permissions, setPermissions] = useState<string[]>([]);
    const [forLoading, setForLoading] = useState(false);

    const [errors, setErrors] = useState("");

    const [optedInToBilling, setOptedInToBilling] = useState(false);

    React.useEffect(() => {
        const loadPermissions = async () => {
            if (selectedServices.length === 0) {
                setPermissions([]);
                return;
            }

            try {
                const result = await generateGcpPermissionsPolicy(selectedServices);

                setPermissions(result);
            } catch {
                setErrors("Failed to determine required GCP permissions");
            }
        };

        loadPermissions();
    }, [selectedServices]);

    React.useEffect(() => {
        const loadServices = async () => {
            try {
                setForLoading(true);
                setErrors("");

                const services = await getCloudServices("gcp");

                setServicesAvailable(services);
            } catch {
                setErrors("Failed to load supported GCP services");
            } finally {
                setForLoading(false);
            }
        };

        loadServices();
    }, []);

    const checkingService = (idForService: string) => {
        setSelectedServices((previous) =>
            previous.includes(idForService)
                ? previous.filter((id) => id !== idForService)
                : [...previous, idForService]
        );
    };

    const handlingSubmit = async (submitting: React.SubmitEvent<HTMLFormElement>) => {
        submitting.preventDefault();

        if (selectedServices.length === 0) {
            setErrors("Please select at least one service");
            return;
        }

        try {
            setForLoading(true);
            setErrors("");

            const resourcesDiscovered = await getCloudResources(
                "gcp",
                credentials,
                selectedServices
            );

            onNext({
                displayName,
                servicesSelected: selectedServices,
                resources: resourcesDiscovered,
                credentials,
            });
        } catch {
            setErrors("Failed to discover GCP resources");
        } finally {
            setForLoading(false);
        }
    };
    const handlingSelectedAll = () => {
        if (selectedServices.length == servicesAvailable.length) {
            setSelectedServices([]);
        } else {
            setSelectedServices(servicesAvailable.map((services) => services));
        }
    };

    return (
        <StepTwo
            heading="Select service"
            description="Choose which GCP service you want to monitor."
            onSubmit={handlingSubmit}
            onBack={onBack || (() => {})}
            forLoading={forLoading}
            forErrors={errors}
        >
            <GcpBillingForm
                optedInToBilling={optedInToBilling}
                handleOptedInToBillingChange={(checked) => {
                    setOptedInToBilling(checked);
                }}
            ></GcpBillingForm>
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
                        {selectedServices.length === servicesAvailable.length
                            ? "Deselect All"
                            : "Select All"}
                    </Button>
                </div>

                <div className="space-y-3">
                    {servicesAvailable.map((service) => (
                        <label
                            key={service}
                            className="flex items-center gap-3 w-full p-4 bg-background rounded-lg border border-border hover:border-primary/40 transition-all cursor-pointer focus-within:ring-2 focus-within:ring-primary focus-within:ring-offset-2"
                        >
                            <input
                                type="checkbox"
                                checked={selectedServices.includes(service)}
                                onChange={() => checkingService(service)}
                                className="w-4 h-4 rounded border-border bg-background text-primary focus:ring-2 focus:ring-primary"
                            />

                            <span className="text-foreground font-medium"> {service} </span>
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
        </StepTwo>
    );
}
