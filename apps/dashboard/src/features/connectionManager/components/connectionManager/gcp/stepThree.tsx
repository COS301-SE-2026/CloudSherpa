"use client";

import React, { useState } from "react";
import {
    StepThree,
    ResourceTable,
    IngestionSlider,
    formattingSecond,
    useIngestionPeriod,
} from "@/features/connectionManager/components/connectionManager/wizardSetup/stepThree";
import { createGcpConnection, PersistGcpConnectionRequest } from "@/lib/fetch/gcp-connection-api";
import { useRouter } from "next/navigation";
import { GcpBillingConfigType } from "./validTypes";
import { ResourceSelectionDto } from "@/lib/fetch/azure-connection-api";
import { GcpCredentialsDto } from "@/lib/fetch/dto/cloud-credentials";
import { ResourceDetail } from "@/lib/fetch/dto/cloud-resource";
/*
- should have tanstack table for resources, as elect & deselect all for it
- should also have pagination
*/

interface StepThreePropsForGcp {
    displayName: string;
    resources: ResourceDetail[];
    ingestionPeriod: number;
    credentials: GcpCredentialsDto;
    billingConfig: GcpBillingConfigType | null;
    onComplete: (ingestionPeriod: number) => void;
    onBack?: () => void;
}

export default function StepThreeGcp({
    displayName,
    resources,
    credentials,
    billingConfig,
    onComplete,
    onBack,
}: Readonly<StepThreePropsForGcp>) {
    const [forSaving, setForSaving] = useState(false);

    const [errors, setErrors] = useState<string | null>(null);

    const [tableResources, setTableResources] = useState<ResourceSelectionDto[]>(() => {
        if (!resources || resources.length === 0) {
            return [];
        }

        return resources.map((resource) => ({
            resourceId: resource.resourceId,
            serviceType: resource.serviceCategory,
            resourceType: resource.resourceType,
            resourceName: resource.name,
            region: resource.region,
            tags: resource.tags,
            active: true,
        }));
    });

    const { activeCount, recIngestionPeriod } = useIngestionPeriod(tableResources);

    const [forIngestionPeriod, setForIngestionPeriod] = useState<number>(recIngestionPeriod);

    const [prevActiveCount, setPrevActiveCount] = useState(activeCount);

    if (activeCount !== prevActiveCount) {
        setPrevActiveCount(activeCount);
        setForIngestionPeriod(activeCount > 0 ? recIngestionPeriod : 60);
    }

    const [filter, setFilter] = useState("");

    const router = useRouter();

    const handleSubmit = async (event: React.SubmitEvent<HTMLFormElement>) => {
        event.preventDefault();

        setForSaving(true);
        setErrors(null);

        try {
            const request: PersistGcpConnectionRequest = {
                userId: "",
                displayName,
                ingestionPeriod: forIngestionPeriod.toString(),
                credentials,
                resources: tableResources.map((resource): ResourceSelectionDto => ({
                    resourceId: resource.resourceId,
                    serviceType: resource.serviceType,
                    resourceType: resource.resourceType,
                    resourceName: resource.resourceName,
                    region: resource.region,
                    tags: resource.tags,
                    active: resource.active,
                })),
                billingConfig,
            };

            await createGcpConnection(request);

            onComplete(forIngestionPeriod);
            router.push("/manageConnections");
        } catch (err) {
            setErrors(err instanceof Error ? err.message : "Unable to create GCP connection.");
        } finally {
            setForSaving(false);
        }
    };

    return (
        <StepThree
            heading="Select instances"
            description="Select the instance you want CloudSherpa to monitor"
            onSubmit={handleSubmit}
            onBack={onBack || (() => {})}
            forSaving={forSaving}
            forErrors={errors}
        >
            <ResourceTable
                data={tableResources}
                onDataChange={setTableResources}
                onFilterChange={setFilter}
                filterValue={filter}
            />

            <IngestionSlider
                ingestionPeriod={forIngestionPeriod}
                setIngestionPeriod={setForIngestionPeriod}
                activeCount={activeCount}
                recIngestionPeriod={recIngestionPeriod}
                formatSeconds={formattingSecond}
            />
        </StepThree>
    );
}
