"use client";

import React, { useState } from "react";
import {
    StepThree,
    ResourceTable,
    IngestionSlider,
    formattingSecond,
    useIngestionPeriod,
} from "@/features/connectionManager/components/connectionManager/wizardSetup/stepThree";
import type { ResourceDetail, ResourceSelectionDto } from "@/lib/fetch/dto/cloud-resource";
import type { AzureCredentialsDto } from "@/lib/fetch/dto/cloud-credentials";
import {
    createAzureConnection,
    type PersistAzureConnectionRequest,
} from "@/lib/fetch/azure-connection-api";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import type { AzureBillingConfigType } from "./validTypes";

interface StepThreePropsForAzure {
    displayName: string;
    resources: ResourceDetail[];
    credentials: AzureCredentialsDto;
    onComplete: (ingestionPeriod: number) => void;
    onBack?: () => void;
    ingestionPeriod?: number;
    billingConfig: AzureBillingConfigType | null;
}

export default function StepThreeAzure({
    displayName,
    credentials,
    resources = [],
    onComplete,
    onBack,
    billingConfig,
}: Readonly<StepThreePropsForAzure>) {
    const [forSaving, setForSaving] = useState(false);

    const [filter, setFilter] = useState("");

    const router = useRouter();

    const [tableResources, setTableResources] = useState<ResourceSelectionDto[]>(() => {
        if (!resources || resources.length === 0) {
            return [];
        }

        return resources.map((resource) => ({
            resourceId: resource.resourceId,
            serviceType: resource.serviceCategory || "",
            resourceType: resource.resourceType || "",
            resourceName: resource.name,
            region: resource.region || "",
            tags: resource.tags || {},
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

    const handlingSubmit = async (event: React.SubmitEvent<HTMLFormElement>) => {
        event.preventDefault();

        setForSaving(true);

        try {
            const request: PersistAzureConnectionRequest = {
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

            await createAzureConnection(request);

            onComplete(forIngestionPeriod);
            router.push("/manageConnections");
            toast.success(`Succesfully created ${displayName} Azure connection`);
        } catch (err) {
            toast.error(err instanceof Error ? err.message : "Unable to create Azure connection.");
        } finally {
            setForSaving(false);
        }
    };

    return (
        <StepThree
            heading="Select instances"
            description="Select the instance you want CloudSherpa to monitor"
            onSubmit={handlingSubmit}
            onBack={onBack || (() => {})}
            forSaving={forSaving}
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
