"use client";

import React, { useState, useMemo, useCallback } from "react";
import { PersistAwsConnectionRequest, createAwsConnection } from "@/lib/fetch/aws-connection-api";
import { StepThree } from "@/features/connectionManager/components/connectionManager/wizardSetup/stepThree";
import { AwsCredentialsDto } from "@/lib/fetch/dto/cloud-credentials";
import { ResourceDetail, ResourceSelectionDto } from "@/lib/fetch/dto/cloud-resource";
import { toast } from "sonner";
import {useRouter} from "next/navigation";
import {useIngestionPeriod, IngestionSlider} from "@/features/connectionManager/components/connectionManager/wizardSetup/stepThree";

interface PropsForStepThree {
    displayName: string;
    ingestionPeriod: string;
    credentials: AwsCredentialsDto;
    resources: ResourceDetail[];
    billingConfig: {
        bucketName: string;
        bucketRegion: string;
        prefix: string;
        exportName: string;
    };
    onComplete: (ingestionPeriod: string) => void;
    onBack: () => void;
}

export default function StepThreeAws({
    displayName,
    ingestionPeriod,
    credentials,
    resources,
    billingConfig,
    onComplete,
    onBack,
}: Readonly<PropsForStepThree>) {
    const router = useRouter();

    const [saving, setSaving] = useState(false);

    const [filterValue, setFilterValue] = useState("");

    const initialResourceSelections : ResourceSelectionDto[] = useMemo(() => {
        return resources.map((resource) : ResourceSelectionDto => ({
            resourceId : resource.resourceId,
            serviceType : resource.serviceCategory,
            resourceType : resource.resourceType,
            resourceName : resource.name,
            region : resource.region,
            tags : resource.tags,
            active : true,
        }));
    }, [resources]);

    const [resourceData, setResourceData] = useState<ResourceSelectionDto[]>(initialResourceSelections);

    const {activeCount, recIngestionPeriod} = useIngestionPeriod(resourceData);

    const initialPeriod = ingestionPeriod ? parseInt(ingestionPeriod) : recIngestionPeriod;

    const [ingestionPeriodState, setIngestionPeriodState] = useState<number>(initalPeriod);

    const handleSubmit = async (event: React.SubmitEvent<HTMLFormElement>) => {
        event.preventDefault();

        setSaving(true);

        try {
            const request: PersistAwsConnectionRequest = {
                userId: "",
                displayName,
                ingestionPeriod: period,
                credentials,
                resources: resources.map((resource): ResourceSelectionDto => ({
                    resourceId: resource.resourceId,
                    serviceType: resource.serviceCategory,
                    resourceType: resource.resourceType,
                    resourceName: resource.name,
                    region: resource.region,
                    tags: resource.tags,
                    active: selectedResources.includes(resource.resourceId),
                })),
                billingConfig: {
                    bucketName: billingConfig.bucketName,
                    bucketRegion: billingConfig.bucketRegion,
                    exportPrefix: billingConfig.prefix,
                    exportName: billingConfig.exportName,
                },
            };

            await createAwsConnection(request);

            onComplete(period);
            router.push("/manageConnections");
            toast.success(`Succesfully created ${displayName} AWS connection`);
        } catch (err) {
            toast.error(err instanceof Error ? err.message : "Unable to create AWS connection.");
        } finally {
            setSaving(false);
        }
    };

    const formatSeconds = (totalSeconds: string | number) => {
        const secs = Number(totalSeconds);
        if (Number.isNaN(secs) || secs <= 0) return "0 seconds";

        const minutes = Math.floor(secs / 60);
        const remainingSeconds = secs % 60;
        let minText = "";
        if (minutes > 0) {
            const labelEnding = minutes === 1 ? "" : "s";
            minText = `${minutes} minute${labelEnding}`;
        }

        let secText = "";
        if (remainingSeconds > 0) {
            const labelEnding = remainingSeconds === 1 ? "" : "s";
            secText = `${remainingSeconds} second${labelEnding}`;
        }

        if (minText && secText) return `${minText} ${secText}`;
        return minText || secText;
    };

    return (
        <StepThree
            heading="Select Instances"
            description="Select the instances you want CloudSherpa to monitor"
            onSubmit={handleSubmit}
            onBack={onBack}
            forSaving={saving}
        >
            <div className="min-h-50">
        
            </div>

            
        </StepThree>
    );
}
