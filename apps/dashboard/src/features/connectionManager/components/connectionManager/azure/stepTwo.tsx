"use client";

import React, { useEffect, useState } from "react";
import { StepTwo } from "@/features/connectionManager/components/connectionManager/wizardSetup/stepTwo";
import { ResourceDetail } from "@/lib/fetch/dto/cloud-resource";
import {
    generateAzurePermissionsPolicy,
    getCloudResources,
    getCloudServices,
} from "@/lib/fetch/cloud-resource-api";
import { CloudCredentials } from "@/lib/fetch/dto/cloud-credentials";
import { ServicesList } from "@/components/molecules/services-list";
import { PermissionsList } from "@/components/molecules/permissions-list";
import { ScanProgress } from "@/components/molecules/scan-progress";
import { AzureBillingForm } from "./billingForm";
import {
    AzureBillingConfig,
    AzureBillingConfigSafeParseType,
    AzureBillingConfigType,
} from "./validTypes";

interface StepTwoPropsForAzure {
    credentials: CloudCredentials | null;
    onNext: (forData: {
        servicesSelected: string[];
        resources: ResourceDetail[];
        billingConfig: AzureBillingConfigType | null;
    }) => void;

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

    const [optedInToBilling, setOptedInToBilling] = useState(false);
    const [storageAccountName, setStorageAccountName] = useState("");
    const [blobContainerName, setBlobContainerName] = useState("");
    const [exportDirectory, setExportDirectory] = useState("");
    const [exportName, setExportName] = useState("");

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

    const handlingSubmit = async (forHandlingSubmit: React.SubmitEvent<HTMLFormElement>) => {
        forHandlingSubmit.preventDefault();

        const validatedBillingConfig: AzureBillingConfigSafeParseType | null =
            validateBillingConfig();

        if (optedInToBilling && validatedBillingConfig != null && !validatedBillingConfig.success) {
            setForErrors("Please enter a valid billing configuration");
            return;
        }

        try {
            setForLoading(true);
            setForErrors("");
            setProgress(0);
            setCurrentScanningService("");

            let discoveredResources: ResourceDetail[] = [];
            for (let i = 0; i < selectedService.length; i++) {
                const currentService = selectedService[i];

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

            onNext({
                servicesSelected: selectedService,
                resources: discoveredResources,
                billingConfig: validatedBillingConfig?.data ?? null,
            });
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

    function validateBillingConfig(): AzureBillingConfigSafeParseType | null {
        if (!optedInToBilling) {
            return null;
        }

        return AzureBillingConfig.safeParse({
            storageAccountName: storageAccountName,
            blobContainerName: blobContainerName,
            exportDirectory: exportDirectory,
            exportName: exportName,
        });
    }

    return (
        <StepTwo
            heading="Select service"
            description="Choose which Azure service you want to monitor."
            onSubmit={handlingSubmit}
            onBack={onBack || (() => {})}
            forLoading={forLoading}
            forErrors={forErrors}
            cloudProvider="azure"
        >
            <AzureBillingForm
                optedInToBilling={optedInToBilling}
                handleOptedInToBillingChange={(checked: boolean) => {
                    setOptedInToBilling(checked);
                }}
                storageAccountName={storageAccountName}
                setStorageAccountName={setStorageAccountName}
                blobContainerName={blobContainerName}
                setBlobContainerName={setBlobContainerName}
                exportDirectory={exportDirectory}
                setExportDirectory={setExportDirectory}
                exportName={exportName}
                setExportName={setExportName}
            />
            <div className="rounded-lg border border-border bg-background p-4">
                <div className="mb-4 rounded-md border border-amber-300/60 bg-amber-50 p-3 text-sm text-amber-900">
                    Billing ingestion is account-wide and not limited by selected services. Select
                    services to discover resources and monitor usage metrics alongside billing
                    trends.
                </div>

                <ServicesList
                    servicesAvailable={servicesAvailable}
                    selectedServices={selectedService}
                    onServiceToggle={checkingServices}
                    onSelectAll={handlingSelectedAll}
                    heading="Services we offer"
                />
            </div>

            <PermissionsList permissions={permissions} />

            {forLoading && (
                <ScanProgress progress={progress} currentScanningService={currentScanningService} />
            )}
        </StepTwo>
    );
}
