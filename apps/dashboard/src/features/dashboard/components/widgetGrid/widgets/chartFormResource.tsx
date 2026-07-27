"use client";

import { useState, useEffect } from "react";
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
import { MetricType, MetricStore } from "@/features/dashboard/types/metric";
import { useMetricStore } from "@/features/dashboard/stores/metric-store";
import { useConnectionStore } from "@/features/dashboard/stores/connection-store";

interface ChartFormResourceProps {
    readonly config: ChartWidgetConfig;
    readonly setConfig: (updater: (prev: ChartWidgetConfig) => ChartWidgetConfig) => void;
}

function getMetricDisplayText(value: MetricType | null, resourceId: string | null) {
    if (value) return value.toUpperCase();
    if (resourceId) return "Select metric type...";
    return "Select resource first";
}

const PROVIDERS = [{ value: "aws", label: "AWS" }];

export function ChartFormResource({ config, setConfig }: ChartFormResourceProps) {
    const [resourceOpen, setResourceOpen] = useState(false);
    const [metricOpen, setMetricOpen] = useState(false);
    const [accountOpen, setAccountOpen] = useState(false);
    const [providerOpen, setProviderOpen] = useState(false);

    // Store Hooks
    const allAvailableMetrics = useMetricStore((state: MetricStore) => state.getMetricList);

    const [selectedProvider, setSelectedProvider] = useState<string>("aws");
    const [selectedAccountId, setSelectedAccountId] = useState<string | null>(null);

    const {
        accounts,
        resourcesByAccountId,
        isLoadingAccounts,
        actions: { fetchAccounts, fetchResourcesForAccount },
        isLoadingResources,
    } = useConnectionStore();

    useEffect(() => {
        fetchAccounts();
    }, [fetchAccounts]);

    useEffect(() => {
        if (selectedAccountId) {
            fetchResourcesForAccount(selectedAccountId);
        }
    }, [selectedAccountId, fetchResourcesForAccount]);

    // Derived State
    const availableMetrics = config.resourceId
        ? (allAvailableMetrics()[config.resourceId] ?? [])
        : [];
    const availableResources = selectedAccountId
        ? resourcesByAccountId[selectedAccountId] || []
        : [];

    const selectedAccountObj = accounts.find((a) => a.id === selectedAccountId);
    const displayAccountText = selectedAccountObj
        ? selectedAccountObj.displayName
        : "Select an account...";

    const selectedResourceObj = availableResources.find((r) => r.id === config.resourceId);
    const displayResourceText = selectedResourceObj
        ? selectedResourceObj.resourceName
        : config.resourceId || "Select a resource...";

    return (
        <FieldSet>
            <div className="flex flex-row items-center gap-3">
                <FormCountCircle count={2} />
                <FieldLegend className="mb-0">Data Source</FieldLegend>
            </div>
            <FieldDescription>
                Filter by your cloud provider and account to locate the specific resource this chart
                will track.
            </FieldDescription>

            <FieldGroup className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
                <Field>
                    <FieldLabel>Cloud Provider</FieldLabel>
                    <Popover open={providerOpen} onOpenChange={setProviderOpen}>
                        <PopoverTrigger asChild>
                            <Button
                                variant="outline"
                                role="combobox"
                                aria-expanded={providerOpen}
                                className="justify-between w-full font-normal"
                                disabled={isLoadingAccounts}
                            >
                                <span className="truncate">
                                    {PROVIDERS.find((p) => p.value === selectedProvider)?.label ??
                                        "Select provider..."}
                                </span>
                                <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                            </Button>
                        </PopoverTrigger>
                        <PopoverContent className="p-0 w-[--radix-popover-trigger-width]">
                            <Command>
                                <CommandInput placeholder="Search providers..." />
                                <CommandList>
                                    <CommandEmpty>No provider found</CommandEmpty>
                                    <CommandGroup>
                                        {PROVIDERS.map((provider) => (
                                            <CommandItem
                                                key={provider.value}
                                                value={provider.value}
                                                onSelect={(currentValue) => {
                                                    setSelectedProvider(currentValue);
                                                    setProviderOpen(false);
                                                }}
                                            >
                                                <Check
                                                    className={cn(
                                                        "mr-2 h-4 w-4",
                                                        selectedProvider === provider.value
                                                            ? "opacity-100"
                                                            : "opacity-0"
                                                    )}
                                                />
                                                {provider.label}
                                            </CommandItem>
                                        ))}
                                    </CommandGroup>
                                </CommandList>
                            </Command>
                        </PopoverContent>
                    </Popover>
                </Field>

                <Field>
                    <FieldLabel className="flex justify-between w-full">Cloud Account</FieldLabel>
                    <Popover open={accountOpen} onOpenChange={setAccountOpen}>
                        <PopoverTrigger asChild>
                            <Button
                                variant="outline"
                                role="combobox"
                                aria-expanded={accountOpen}
                                className="justify-between w-full font-normal"
                                disabled={isLoadingAccounts || accounts.length === 0}
                            >
                                <span className="truncate">
                                    {accounts.length === 0 && !isLoadingAccounts
                                        ? "No accounts found"
                                        : displayAccountText}
                                </span>
                                <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                            </Button>
                        </PopoverTrigger>
                        <PopoverContent className="p-0 w-[--radix-popover-trigger-width]">
                            <Command>
                                <CommandInput placeholder="Search accounts..." />
                                <CommandList>
                                    <CommandEmpty>No accounts found</CommandEmpty>
                                    <CommandGroup>
                                        {accounts.map((account) => (
                                            <CommandItem
                                                key={account.id}
                                                value={account.displayName}
                                                onSelect={() => {
                                                    setSelectedAccountId(account.id);
                                                    setConfig((prev) => ({
                                                        ...prev,
                                                        resourceId: null,
                                                        metricType: null,
                                                    }));
                                                    setAccountOpen(false);
                                                }}
                                            >
                                                <Check
                                                    className={cn(
                                                        "mr-2 h-4 w-4",
                                                        selectedAccountId === account.id
                                                            ? "opacity-100"
                                                            : "opacity-0"
                                                    )}
                                                />
                                                {account.displayName}
                                            </CommandItem>
                                        ))}
                                    </CommandGroup>
                                </CommandList>
                            </Command>
                        </PopoverContent>
                    </Popover>
                </Field>
                <Field>
                    <FieldLabel className="flex justify-between w-full">Resource</FieldLabel>
                    <Popover open={resourceOpen} onOpenChange={setResourceOpen}>
                        <PopoverTrigger asChild>
                            <Button
                                variant="outline"
                                role="combobox"
                                aria-expanded={resourceOpen}
                                className="justify-between w-full font-normal"
                                disabled={
                                    !selectedAccountId ||
                                    (isLoadingResources[selectedAccountId] ?? false)
                                }
                            >
                                <span className="truncate">{displayResourceText}</span>
                                <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                            </Button>
                        </PopoverTrigger>
                        <PopoverContent className="p-0 w-[--radix-popover-trigger-width]">
                            <Command>
                                <CommandInput placeholder="Search resources..." />
                                <CommandList>
                                    <CommandEmpty>No resources found</CommandEmpty>
                                    <CommandGroup>
                                        {availableResources.map((resource) => (
                                            <CommandItem
                                                key={resource.id}
                                                value={resource.resourceName}
                                                onSelect={() => {
                                                    const nextMetricOptions =
                                                        allAvailableMetrics()[resource.id] ?? [];
                                                    const newMetricType =
                                                        nextMetricOptions[0] ?? null;

                                                    setConfig((prev) => ({
                                                        ...prev,
                                                        resourceId: resource.id,
                                                        metricType: newMetricType,
                                                    }));
                                                    setResourceOpen(false);
                                                }}
                                            >
                                                <Check
                                                    className={cn(
                                                        "mr-2 h-4 w-4",
                                                        config.resourceId === resource.id
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
                                        {availableMetrics.map((metric) => (
                                            <CommandItem
                                                key={metric}
                                                value={metric}
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
                                                        config.metricType === metric
                                                            ? "opacity-100"
                                                            : "opacity-0"
                                                    )}
                                                />
                                                {metric.toUpperCase()}
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
