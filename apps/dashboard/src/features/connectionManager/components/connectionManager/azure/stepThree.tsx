"use client";

import React, { useState, useMemo } from "react";
import {
    StepThree,
    ResourceTable,
    IngestionSlider,
} from "@/features/connectionManager/components/connectionManager/wizardSetup/stepThree";
import { ResourceSelectionDto } from "@/lib/fetch/aws-connection-api";
import { ResourceDetail } from "@/lib/fetch/cloud-resource-api";

interface StepThreePropsForAzure {
    displayName: string;
    resources: ResourceDetail[];
    onComplete: (ingestionPeriod: number) => void;
    onBack?: () => void;
    ingestionPeriod?: number;
}

export default function StepThreeAzure({
    displayName,
    resources = [],
    onComplete,
    onBack,
    ingestionPeriod = 60,
}: Readonly<StepThreePropsForAzure>) {
    const [forSaving, setForSaving] = useState(false);

    const [errors, setErrors] = useState<string | null>(null);

    const [forIngestionPeriod, setForIngestionPeriod] = useState<number>(ingestionPeriod);

    const [filter, setFilter] = useState("");

    const tableResources: ResourceSelectionDto[] = useMemo(() => {
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
    }, [resources]);

    const setTableResources = (
        newData:
            ResourceSelectionDto[] | ((previous: ResourceSelectionDto[]) => ResourceSelectionDto[])
    ) => {};

    const activeCount = tableResources.filter((resource) => resource.active).length;

    const recIngestionPeriod = activeCount * 5 * 20;

    const formattingSecond = (totalSeconds: string | number) => {
        const seconds = Number(totalSeconds);

        if (Number.isNaN(seconds) || seconds <= 0) {
            return "0 seconds";
        }

        const minutes = Math.floor(seconds / 60);
        const secondsLeft = seconds % 60;

        let minText = "";
        if (minutes > 0) {
            const labelEnding = minutes === 1 ? "" : "s";
            minText = `${minutes} minute${labelEnding}`;
        }

        let secText = "";
        if (secondsLeft > 0) {
            const labelEnding = secondsLeft === 1 ? "" : "s";
            secText = `${secondsLeft} second${labelEnding}`;
        }

        if (minText && secText) {
            return `${minText} ${secText}`;
        }

        return minText || secText;
    };

    const handlingSubmit = async (event: React.SubmitEvent<HTMLFormElement>) => {
        event.preventDefault();

        setForSaving(true);

        setErrors(null);

        try {
            onComplete(forIngestionPeriod);
        } catch (forError) {
            setErrors(
                forError instanceof Error
                    ? forError.message
                    : "Unable to create an Azure connection"
            );
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
