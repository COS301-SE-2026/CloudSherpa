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
            <ServicesList
                servicesAvailable={servicesAvailable}
                selectedServices={selectedService}
                onServiceToggle={checkingServices}
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
