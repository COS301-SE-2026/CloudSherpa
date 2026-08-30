"use client";

import React, { useState } from "react";
import { StepTwo } from "@/features/connectionManager/components/connectionManager/wizardSetup/stepTwo";
import {
    getCloudServices,
    generateGcpPermissionsPolicy,
    getCloudResources,
} from "@/lib/fetch/cloud-resource-api";
import { GcpBillingForm } from "./billingForm";
import {
    GcpBillingConfig,
    type GcpBillingConfigSafeParseType,
    type GcpBillingConfigType,
} from "./validTypes";
import { GcpCredentialsDto } from "@/lib/fetch/dto/cloud-credentials";
import { ResourceDetail } from "@/lib/fetch/dto/cloud-resource";
import { ServicesList } from "@/components/molecules/services-list";
import { PermissionsList } from "@/components/molecules/permissions-list";
import { ScanProgress } from "@/components/molecules/scan-progress";

export interface ServiceOption {
    id: string;
    name: string;
}

interface StepTwoPropsForGcp {
    displayName: string;
    credentials: GcpCredentialsDto;
    onNext: (data: {
        displayName: string;
        servicesSelected: string[];
        resources: ResourceDetail[];
        credentials: GcpCredentialsDto;
        billingConfig: GcpBillingConfigType | null;
    }) => void;
    onBack?: () => void;
}

export default function StepTwoGcp({
    displayName,
    credentials,
    onNext,
    onBack,
}: Readonly<StepTwoPropsForGcp>) {
    const [servicesAvailable, setServicesAvailable] = useState<ServiceOption[]>([]);
    const [selectedServices, setSelectedServices] = useState<string[]>([]);
    const [permissions, setPermissions] = useState<string[]>([]);
    const [forLoading, setForLoading] = useState(false);

    const [errors, setErrors] = useState("");

    const [optedInToBilling, setOptedInToBilling] = useState(false);
    const [billingId, setBillingId] = useState("");
    const [billingDataset, setBillingDataset] = useState("");

    const [progress, setProgress] = useState(0);
    const [currentScanningService, setCurrentScanningService] = useState("");

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
                setServicesAvailable(
                    services.map((service) => ({
                        id: service,
                        name: service,
                    }))
                );
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
        setErrors("");

        const validatedBillingConfig: GcpBillingConfigSafeParseType | null = validateBillingInput();

        if (validatedBillingConfig != null && !validatedBillingConfig.success) {
            setErrors("Please enter a valid billing configuration");
            return;
        }

        if (selectedServices.length === 0) {
            setErrors("Please select at least one service");
            return;
        }

        try {
            setForLoading(true);
            setProgress(0);
            setCurrentScanningService("");

            let discoveredResources: ResourceDetail[] = [];
            for (let i = 0; i < selectedServices.length; i++) {
                const currentService = selectedServices[i];

                setCurrentScanningService(currentService);

                const resources = await getCloudResources("gcp", credentials, [currentService]);
                discoveredResources = [...discoveredResources, ...resources];

                setProgress(((i + 1) / selectedServices.length) * 100);
            }

            onNext({
                displayName,
                servicesSelected: selectedServices,
                resources: discoveredResources,
                credentials,
                billingConfig: validatedBillingConfig?.data ?? null,
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
            setSelectedServices(servicesAvailable.map((services) => services.id));
        }
    };

    function validateBillingInput(): GcpBillingConfigSafeParseType | null {
        if (!optedInToBilling) {
            return null;
        }

        return GcpBillingConfig.safeParse({
            billingId: billingId,
            dataset: billingDataset,
        });
    }

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
                billingId={billingId}
                setBillingId={setBillingId}
                setBillingDataset={setBillingDataset}
                billingDataset={billingDataset}
                handleOptedInToBillingChange={(checked) => {
                    setOptedInToBilling(checked);
                }}
            ></GcpBillingForm>
            <ServicesList
                servicesAvailable={servicesAvailable}
                selectedServices={selectedServices}
                onServiceToggle={checkingService}
                onSelectAll={handlingSelectedAll}
                heading="Services we offer"
            />

            <PermissionsList permissions={permissions} />

            {forLoading && (
                <ScanProgress progress={progress} currentScanningService={currentScanningService} />
            )}
        </StepTwo>
    );
}
