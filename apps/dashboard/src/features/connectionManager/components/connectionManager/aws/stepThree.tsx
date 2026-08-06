"use client";

import React, { useState } from "react";
import { Checkbox } from "@/components/atoms/checkbox";
import { Badge } from "@/components/atoms/badge";
import {
    Tooltip,
    TooltipContent,
    TooltipProvider,
    TooltipTrigger,
} from "@/components/atoms/tooltip";
import { ResourceDetail } from "@/lib/fetch/cloud-resource-api";
import {
    AwsCredentialsDto,
    PersistAwsConnectionRequest,
    ResourceSelectionDto,
    createAwsConnection,
} from "@/lib/fetch/aws-connection-api";
import { useRouter } from "next/navigation";
import { Label } from "@/components/atoms/label";
import { Slider } from "@/components/atoms/slider";
import {StepThree} from "@/features/connectionManager/components/connectionManager/wizardSetup/stepThree";

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

interface ResourceTagsProps {
    tags: Record<string, string>;
}

interface ResourceRowProps {
    resource: ResourceDetail;
    selected: boolean;
    onToggle: (resourceId: string, checked: boolean) => void;
}

interface ResourceCategoryProps {
    serviceCategory: string;
    resources: ResourceDetail[];
    selectedResources: string[];
    onToggle: (resourceId: string, checked: boolean) => void;
}

function groupResourcesByCategory(resources: ResourceDetail[]): Record<string, ResourceDetail[]> {
    return resources.reduce(
        (groups, resource) => {
            const category = resource.serviceCategory;

            if (!groups[category]) {
                groups[category] = [];
            }

            groups[category].push(resource);

            return groups;
        },
        {} as Record<string, ResourceDetail[]>
    );
}

function ResourceTags({ tags }: Readonly<ResourceTagsProps>) {
    return (
        <div className="flex flex-wrap justify-end gap-2 max-w-md">
            {Object.entries(tags).map(([key, value]) => (
                <Badge key={`${key}-${value}`} variant="secondary">
                    {key}: {value}
                </Badge>
            ))}
        </div>
    );
}

function ResourceRow({ resource, selected, onToggle }: Readonly<ResourceRowProps>) {
    return (
        <div
            className="
        flex
        items-start
        justify-between
        gap-4
        p-4
        bg-background
        rounded-lg
        border
        border-border
        hover:border-primary/40
        transition-all
        cursor-pointer
      "
        >
            <div className="flex items-start gap-3">
                <Checkbox
                    checked={selected}
                    onCheckedChange={(checked) => onToggle(resource.resourceId, Boolean(checked))}
                />

                <div>
                    <div className="font-medium text-foreground">
                        {resource.name}

                        <span className="ml-2 text-muted-foreground">({resource.resourceId})</span>
                    </div>
                </div>
            </div>

            <ResourceTags tags={resource.tags} />
        </div>
    );
}

function ResourceCategory({
    serviceCategory,
    resources,
    selectedResources,
    onToggle,
}: Readonly<ResourceCategoryProps>) {
    return (
        <div>
            <h3 className="text-lg font-semibold text-foreground mb-4">{serviceCategory}</h3>

            <div className="space-y-3">
                {resources.map((resource) => (
                    <ResourceRow
                        key={resource.resourceId}
                        resource={resource}
                        selected={selectedResources.includes(resource.resourceId)}
                        onToggle={onToggle}
                    />
                ))}
            </div>
        </div>
    );
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
    const [selectedResources, setSelectedResources] = useState<string[]>(
        resources.map((resource) => resource.resourceId)
    );
    const router = useRouter();

    const [saving, setSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [period, setPeriod] = useState<string>(ingestionPeriod);
    const recommendedPeriod = selectedResources.length * 5 * 20;

    const groupedResources = groupResourcesByCategory(resources);

    const handleSubmit = async (event: React.SubmitEvent<HTMLFormElement>) => {
        event.preventDefault();

        setSaving(true);
        setError(null);

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
        } catch (err) {
            setError(err instanceof Error ? err.message : "Unable to create AWS connection.");
        } finally {
            setSaving(false);
        }
    };

    const handleResourceToggle = (resourceId: string, checked: boolean) => {
        setSelectedResources((previous) => {
            if (checked) {
                return previous.includes(resourceId) ? previous : [...previous, resourceId];
            }

            return previous.filter((id) => id !== resourceId);
        });
    };

    const formatSeconds = (totalSeconds: string | number) => {
        const secs = Number(totalSeconds);
        if (Number.isNaN(secs) || secs <= 0) return "0 seconds";

        const minutes = Math.floor(secs / 60);
        const remainingSeconds = secs % 60;
        let minText = "";
        if(minutes>0){
            const labelEnding = minutes === 1 ? "" : "s";
            minText = `${minutes} minute${labelEnding}`;
        }

        let secText = "";
        if(remainingSeconds>0){
            const labelEnding = remainingSeconds === 1 ? "" : "s";
            secText = `${remainingSeconds} second${labelEnding}`;
        }

        if (minText && secText) return `${minText} ${secText}`;
        return minText || secText;
    };

    return (
        <StepThree heading = "Select Instances"
                   description = "Select the instances you want CloudSherpa to monitor"
                   onSubmit = {handleSubmit} onBack = {onBack} forSaving = {saving} forErrors = {error}
        >

            <div className="min-h-50">
                <div className="space-y-8">
                    {Object.entries(groupedResources).map(
                        ([serviceCategory, categoryResources]) => (
                            <ResourceCategory
                                key={serviceCategory}
                                serviceCategory={serviceCategory}
                                resources={categoryResources}
                                selectedResources={selectedResources}
                                onToggle={handleResourceToggle}
                            />
                        )
                    )}
                </div>
            </div>
            <div className="space-y-2">
                <div className="flex items-center gap-2">
                    <Label
                        htmlFor="ingestionPeriod"
                        className="text-foreground text-sm font-medium"
                    >
                        Ingestion interval (seconds)
                    </Label>

                    <TooltipProvider>
                        <Tooltip>
                            <TooltipTrigger asChild>
                                <button
                                    type="button"
                                    className="
                                        flex
                                        items-center
                                        justify-center
                                        w-5
                                        h-5
                                        rounded-full
                                        text-xs
                                        text-muted-foreground
                                        hover:text-foreground
                                        border
                                        border-border
                                        "
                                >
                                    ?
                                </button>
                            </TooltipTrigger>

                            <TooltipContent>
                                <p>
                                    Recommended ingestion interval: {recommendedPeriod}{" "}
                                    seconds based on {selectedResources.length} selected
                                    resources. Setting the interval to a lower value could
                                    incur costs due to CloudWatch API free tier limits. The
                                    ingestion interval determines the frequency of dashboard
                                    timeseries updates.
                                </p>
                            </TooltipContent>
                        </Tooltip>
                    </TooltipProvider>
                </div>
                <div className="flex flex-col gap-2 justify-center items-end ">
                    {/* the span is meant for a visual indicator of the value of the slider */}
                    <span>{formatSeconds(period)}</span>
                    <Slider
                        value={[Number(period)]}
                        onValueChange={(vals) => setPeriod(String(vals[0]))}
                        min={60}
                        max={400}
                    />

                    <p className="text-xs text-muted-foreground/70 ">
                        Recommended: {formatSeconds(recommendedPeriod)}
                    </p>
                </div>
            </div>
        </StepThree>
    );
}
