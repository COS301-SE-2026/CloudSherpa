"use client";
import { MetricType, MetricStore } from "@/features/dashboard/types/metric";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { useState, useEffect } from "react";
import { ChartWidgetConfig } from "@/features/dashboard/types/widgets";
import { Check, ChevronsUpDown } from "lucide-react";
import { cn } from "@/lib/utils";
import { Label } from "@/components/atoms/label";
import { Button } from "@/components/atoms/button";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/atoms/popover";
import {
    Command,
    CommandEmpty,
    CommandGroup,
    CommandInput,
    CommandItem,
    CommandList,
} from "@/components/atoms/command";
import { FieldSet, FieldLegend, FieldDescription, FieldGroup } from "@/components/atoms/field";
import { FormCountCircle } from "@/components/atoms/form-count-circle";
import {
    getAwsAccountResources,
    CloudResource,
    ResourceStatus,
} from "@/lib/fetch/aws-connection-api";

function getMetricDisplayText(value: MetricType | null, resourceId: string | null) {
    if (value) {
        return value.toUpperCase();
    }
    if (resourceId) {
        return "Select metric type...";
    }
    return "Select resource first";
}

interface ChartFormResourceProps {
    configuration: ChartWidgetConfig;
    setConfiguration: (config: ChartWidgetConfig) => void;
    selectedConnectionId: string | null;
}

export default function ChartFormResource({
    configuration,
    setConfiguration,
    selectedConnectionId,
}: Readonly<ChartFormResourceProps>) {
    const [resourceOpen, setResourceOpen] = useState(false);
    const [metricOpen, setMetricOpen] = useState(false);

    const [activeResources, setActiveResources] = useState<CloudResource[]>([]);

    useEffect(() => {
        if (selectedConnectionId) {
            getAwsAccountResources(selectedConnectionId)
                .then((resources) => {
                    const activeOnly = resources.filter((r) => r.status === ResourceStatus.ACTIVE);
                    setActiveResources(activeOnly);
                })
                .catch(console.error);
        }
    }, [selectedConnectionId]);

    const allAvailableMetrics = useMetricStore((state: MetricStore) => state.getMetricList);

    const availableMetrics = configuration.resourceId
        ? (allAvailableMetrics()[configuration.resourceId] ?? [])
        : [];

    return (
        <FieldSet>
            <div className="flex flex-row items-center gap-3">
                <FormCountCircle count={3} />
                <FieldLegend className="mb-0">Resource</FieldLegend>
            </div>
            <FieldDescription>Choose the resource for the chart.</FieldDescription>
            <FieldGroup>
                <div className="grid gap-2">
                    <Label>Resource ID</Label>
                    <Popover open={resourceOpen} onOpenChange={setResourceOpen}>
                        <PopoverTrigger asChild>
                            <Button
                                variant="outline"
                                role="combobox"
                                aria-expanded={resourceOpen}
                                className="justify-between w-full"
                            >
                                <span className="truncate">
                                    {configuration.resourceId
                                        ? (activeResources.find(
                                              (r) => r.id === configuration.resourceId
                                          )?.resourceName ?? configuration.resourceId)
                                        : "Select a resource..."}
                                </span>
                                <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                            </Button>
                        </PopoverTrigger>
                        <PopoverContent className="p-0 w-(--radix-popover-trigger-width)">
                            <Command>
                                <CommandInput placeholder="Search resources..." />
                                <CommandList>
                                    <CommandEmpty>No resource found</CommandEmpty>
                                    <CommandGroup>
                                        {activeResources.map((resource) => (
                                            <CommandItem
                                                key={resource.id}
                                                value={resource.id}
                                                onSelect={(currentValue) => {
                                                    const nextMetricOptions =
                                                        allAvailableMetrics()[currentValue] ?? [];
                                                    const metricType =
                                                        nextMetricOptions[0] ?? "anon";
                                                    setConfiguration({
                                                        ...configuration,
                                                        resourceId: currentValue,
                                                        metricType: metricType,
                                                    });
                                                    setResourceOpen(false);
                                                }}
                                            >
                                                <Check
                                                    className={cn(
                                                        "mr-2 h-4 w-4",
                                                        configuration.resourceId === resource.id
                                                            ? "opacity-100"
                                                            : "opacity-0"
                                                    )}
                                                />
                                                {resource.resourceName}
                                            </CommandItem>
                                        ))}
                                    </CommandGroup>
                                </CommandList>
                            </Command>
                        </PopoverContent>
                    </Popover>
                </div>

                <div className="grid gap-2">
                    <Label>Metric Type</Label>
                    <Popover open={metricOpen} onOpenChange={setMetricOpen}>
                        <PopoverTrigger asChild>
                            <Button
                                variant="outline"
                                role="combobox"
                                aria-expanded={metricOpen}
                                className="justify-between w-full"
                                disabled={!configuration.resourceId}
                            >
                                <span className="truncate">
                                    {getMetricDisplayText(
                                        configuration.metricType,
                                        configuration.resourceId
                                    )}
                                </span>
                                <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                            </Button>
                        </PopoverTrigger>
                        <PopoverContent className="p-0 w-(--radix-popover-trigger-width)">
                            <Command>
                                <CommandInput placeholder="Search metric types..." />
                                <CommandList>
                                    <CommandEmpty>No metric type found.</CommandEmpty>
                                    <CommandGroup>
                                        {availableMetrics.map((type) => (
                                            <CommandItem
                                                key={type}
                                                value={type}
                                                onSelect={(currentValue) => {
                                                    setConfiguration({
                                                        ...configuration,
                                                        metricType: currentValue as MetricType,
                                                    });
                                                    setMetricOpen(false);
                                                }}
                                            >
                                                <Check
                                                    className={cn(
                                                        "mr-2 h-4 w-4",
                                                        configuration.metricType === type
                                                            ? "opacity-100"
                                                            : "opacity-0"
                                                    )}
                                                />
                                                {type.toUpperCase()}
                                            </CommandItem>
                                        ))}
                                    </CommandGroup>
                                </CommandList>
                            </Command>
                        </PopoverContent>
                    </Popover>
                </div>
            </FieldGroup>
        </FieldSet>
    );
}
