"use client";

import React, { useState } from "react";
import { Button } from "@/components/atoms/button";
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
import { Input } from "@/components/atoms/input";
import { Label } from "@/components/atoms/label";

interface PropsForStepThree {
    displayName: string;
    ingestionPeriod: string;
    credentials: AwsCredentialsDto;
    resources: ResourceDetail[];
    billingConfig: {
        bucketName: string;
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

export default function StepThree({
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
                    resourceType: resource.serviceCategory,
                    resourceName: resource.name,
                    tags: resource.tags,
                    active: selectedResources.includes(resource.resourceId),
                })),
                billingConfig: {
                    bucketName: billingConfig.bucketName,
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

    return (
        <div className="min-h-screen bg-background flex items-center justify-center p-8">
            <div className="w-full max-w-4xl bg-card rounded-lg shadow-none p-8">
                <div className="pb-6">
                    <div className="flex items-center gap-2 mb-4">
                        <div className="w-2 h-2 rounded-full bg-primary" />

                        <span className="text-sm font-medium text-muted-foreground/70">
                            STEP 3 OF 3
                        </span>
                    </div>

                    <h2 className="text-2xl font-semibold tracking-tight text-foreground">
                        Select Instances
                    </h2>

                    <p className="mt-2 text-muted-foreground/70">
                        Select the instances you want CloudSherpa to monitor.
                    </p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-8">
                    <div className="min-h-[200px]">
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
                                Ingestion period (seconds)
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
                                            Recommended ingestion period: {recommendedPeriod}{" "}
                                            seconds based on {selectedResources.length} selected
                                            resources. Setting the period to a lower value could
                                            incur costs due to CloudWatch API free tier limits. The
                                            ingestion period determines the frequency of dashboard
                                            timeseries updates.
                                        </p>
                                    </TooltipContent>
                                </Tooltip>
                            </TooltipProvider>
                        </div>

                        <Input
                            id="ingestionPeriod"
                            type="number"
                            min="1"
                            step="1"
                            value={period}
                            onChange={(e) => {
                                const value = e.target.value;

                                if (/^\d*$/.test(value)) {
                                    setPeriod(value);
                                }
                            }}
                            className="
      bg-background
      border-border
      rounded-md
      px-4
      py-3
      text-foreground
      placeholder:text-muted-foreground/40
      focus:outline-none
      focus:ring-2
      focus:ring-ring
      focus:border-transparent
      transition-all
      w-full
    "
                            required
                        />

                        <p className="text-xs text-muted-foreground/70">
                            Recommended: {recommendedPeriod} seconds
                        </p>
                    </div>
                    {error && (
                        <div className="rounded-md border border-red-500 bg-red-50 p-3 text-sm text-red-700">
                            {error}
                        </div>
                    )}
                    <div className="flex justify-between pt-6">
                        <Button
                            type="button"
                            disabled={saving}
                            onClick={onBack}
                            className="bg-primary hover:bg-accent hover:text-accent-foreground text-primary-foreground px-6 py-2 rounded-md transition-all duration-200 font-medium"
                        >
                            Back
                        </Button>

                        <Button
                            type="submit"
                            disabled={saving}
                            className="bg-primary hover:bg-accent hover:text-accent-foreground text-primary-foreground px-8 py-2 rounded-md transition-all duration-200 font-medium"
                        >
                            {saving ? "Saving..." : "Finish"}
                        </Button>
                    </div>
                </form>
            </div>
        </div>
    );
}
