"use client";

import React, { useState, useMemo } from "react";
import {
    StepThree,
    ResourceTable,
    IngestionSlider,
    formattingSecond,
    useIngestionPeriod,
} from "@/features/connectionManager/components/connectionManager/wizardSetup/stepThree";
import {
    createGcpConnection,
    GcpCredentialsDto,
    PersistGcpConnectionRequest,
    ResourceSelectionDto,
} from "@/lib/fetch/gcp-connection-api";
import { ResourceDetail } from "@/lib/fetch/cloud-resource-api";
import { useRouter } from "next/navigation";
/*
- should have tanstack table for resources, as elect & deselect all for it
- should also have pagination
*/

interface StepThreePropsForGcp {
    displayName: string;
    resources: ResourceDetail[];
    ingestionPeriod: number;
    credentials: GcpCredentialsDto;
    onComplete: (ingestionPeriod: number) => void;
    onBack?: () => void;
}

export default function StepThreeGcp({
    displayName,
    resources,
    credentials,
    onComplete,
    onBack,
    ingestionPeriod = 60,
}: Readonly<StepThreePropsForGcp>) {
    const [forSaving, setForSaving] = useState(false);

    const [errors, setErrors] = useState<string | null>(null);

    const [forIngestionPeriod, setForIngestionPeriod] = useState<number>(ingestionPeriod);

    const tableResources: ResourceSelectionDto[] = useMemo(() => {
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
    }, [resources]);

    const { activeCount, recIngestionPeriod } = useIngestionPeriod(tableResources);

    const setTableResources = (
        newData:
            ResourceSelectionDto[] | ((previous: ResourceSelectionDto[]) => ResourceSelectionDto[])
    ) => {};

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
