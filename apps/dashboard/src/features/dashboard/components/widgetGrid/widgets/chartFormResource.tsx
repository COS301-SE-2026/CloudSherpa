"use client";

import { useState } from "react";
import { Check, ChevronsUpDown } from "lucide-react";
import { cn } from "@/lib/utils";

import {
    FieldSet,
    FieldLegend,
    FieldDescription,
    FieldGroup,
    Field,
    FieldLabel,
} from "@/components/atoms/field";
import { FormCountCircle } from "@/components/atoms/form-count-circle";
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

import { ChartWidgetConfig } from "@/features/dashboard/types/widgets";
import {
    useResourceNameStore,
    ResourceNameStore,
} from "@/features/dashboard/stores/resource-store";
import { MetricType, MetricStore } from "@/features/dashboard/types/metric";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";

interface ChartFormResourceProps {
    readonly config: ChartWidgetConfig;
    readonly setConfig: (updater: (prev: ChartWidgetConfig) => ChartWidgetConfig) => void;
}

function getMetricDisplayText(value: MetricType | null, resourceId: string | null) {
    if (value) return value.toUpperCase();
    if (resourceId) return "Select metric type...";
    return "Select resource first";
}

export function ChartFormResource({ config, setConfig }: ChartFormResourceProps) {
    const [resourceOpen, setResourceOpen] = useState(false);
    const [metricOpen, setMetricOpen] = useState(false);

    // Store Hooks
    const resourceNamesById = useResourceNameStore(
        (state: ResourceNameStore) => state.resourcesById
    );
    const allAvailableMetrics = useMetricStore((state: MetricStore) => state.getMetricList);

    // Derived State
    const availableMetrics = config.resourceId
        ? (allAvailableMetrics()[config.resourceId] ?? [])
        : [];
    const metricsByResource = allAvailableMetrics();
    const metricResourceIds = Object.keys(metricsByResource);
    const availableResources =
        metricResourceIds.length > 0 ? metricResourceIds : Object.keys(resourceNamesById);

    return (
        <FieldSet>
            <div className="flex flex-row items-center gap-3">
                <FormCountCircle count={2} />
                <FieldLegend className="mb-0">Data Source</FieldLegend>
            </div>
            <FieldDescription>
                Select the specific resource and metric this chart will track.
            </FieldDescription>
            <FieldGroup>
                <Field>
                    <FieldLabel>Resource ID</FieldLabel>
                    <Popover open={resourceOpen} onOpenChange={setResourceOpen}>
                        <PopoverTrigger asChild>
                            <Button
                                variant="outline"
                                role="combobox"
                                aria-expanded={resourceOpen}
                                className="justify-between w-full font-normal"
                            >
                                <span className="truncate">
                                    {config.resourceId
                                        ? (resourceNamesById[config.resourceId] ??
                                          config.resourceId)
                                        : "Select a resource..."}
                                </span>
                                <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                            </Button>
                        </PopoverTrigger>
                        <PopoverContent className="p-0 w-[--radix-popover-trigger-width]">
                            <Command>
                                <CommandInput placeholder="Search resources..." />
                                <CommandList>
                                    <CommandEmpty>No resource found</CommandEmpty>
                                    <CommandGroup>
                                        {availableResources.map((resource) => (
                                            <CommandItem
                                                key={resource}
                                                value={resource}
                                                onSelect={(currentValue) => {
                                                    const nextMetricOptions =
                                                        allAvailableMetrics()[currentValue] ?? [];
                                                    const newMetricType =
                                                        nextMetricOptions[0] ??
                                                        ("anon" as MetricType);

                                                    setConfig((prev) => ({
                                                        ...prev,
                                                        resourceId: currentValue,
                                                        metricType: newMetricType,
                                                    }));
                                                    setResourceOpen(false);
                                                }}
                                            >
                                                <Check
                                                    className={cn(
                                                        "mr-2 h-4 w-4",
                                                        config.resourceId === resource
                                                            ? "opacity-100"
                                                            : "opacity-0"
                                                    )}
                                                />
                                                {resourceNamesById[resource] ?? resource}
                                            </CommandItem>
                                        ))}
                                    </CommandGroup>
                                </CommandList>
                            </Command>
                        </PopoverContent>
                    </Popover>
                </Field>

                <Field>
                    <FieldLabel>Metric Type</FieldLabel>
                    <Popover open={metricOpen} onOpenChange={setMetricOpen}>
                        <PopoverTrigger asChild>
                            <Button
                                variant="outline"
                                role="combobox"
                                aria-expanded={metricOpen}
                                className="justify-between w-full font-normal"
                                disabled={!config.resourceId}
                            >
                                <span className="truncate">
                                    {getMetricDisplayText(config.metricType, config.resourceId)}
                                </span>
                                <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                            </Button>
                        </PopoverTrigger>
                        <PopoverContent className="p-0 w-[--radix-popover-trigger-width]">
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
                                                    setConfig((prev) => ({
                                                        ...prev,
                                                        metricType: currentValue as MetricType,
                                                    }));
                                                    setMetricOpen(false);
                                                }}
                                            >
                                                <Check
                                                    className={cn(
                                                        "mr-2 h-4 w-4",
                                                        config.metricType === type
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
                </Field>
            </FieldGroup>
        </FieldSet>
    );
}
