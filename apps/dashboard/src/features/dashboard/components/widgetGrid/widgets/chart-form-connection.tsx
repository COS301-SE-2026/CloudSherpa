"use client";
import { useState, useEffect } from "react";
import { ChartWidgetConfig } from "@/features/dashboard/types/widgets";
import { Check, ChevronsUpDown } from "lucide-react";
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
import { getAwsAccountConnections, CloudAccount } from "@/lib/fetch/aws-connection-api";
import { cn } from "@/lib/utils";

const PROVIDERS = ["AWS", "AZURE", "GCP"];

interface ChartFormConnectionProps {
    configuration: ChartWidgetConfig;
    setConfiguration: (config: ChartWidgetConfig) => void;
    selectedProvider: string | null;
    setSelectedProvider: (provider: string) => void;
    selectedConnectionId: string | null;
    setSelectedConnectionId: (id: string) => void;
}

export default function ChartFormConnection({
    configuration,
    setConfiguration,
    selectedProvider,
    setSelectedProvider,
    selectedConnectionId,
    setSelectedConnectionId,
}: Readonly<ChartFormConnectionProps>) {
    const [providerOpen, setProviderOpen] = useState(false);
    const [connectionOpen, setConnectionOpen] = useState(false);

    //fetch connections
    const [connections, setConnections] = useState<CloudAccount[]>([]);

    useEffect(() => {
        if (selectedProvider === "AWS") {
            getAwsAccountConnections()
                .then((data) => {
                    setConnections(data);
                })
                .catch(console.error);
        }
    }, [selectedProvider]);

    return (
        <FieldSet>
            <div className="flex flex-row items-center gap-3">
                <FormCountCircle count={2} />
                <FieldLegend className="mb-0">Connection</FieldLegend>
            </div>
            <FieldDescription>
                Choose the connection for the resource to be displayed on the chart.
            </FieldDescription>
            <FieldGroup>
                <div className="grid gap-2">
                    <Label>Provider</Label>
                    <Popover open={providerOpen} onOpenChange={setProviderOpen}>
                        <PopoverTrigger asChild>
                            <Button
                                variant="outline"
                                role="combobox"
                                aria-expanded={providerOpen}
                                className="justify-between w-full"
                            >
                                <span className="truncate">
                                    {selectedProvider ? selectedProvider : "Select a provider..."}
                                </span>
                                <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                            </Button>
                        </PopoverTrigger>
                        <PopoverContent className="p-0 w-(--radix-popover-trigger-width)">
                            <Command>
                                <CommandInput placeholder="Search resources..." />
                                <CommandList>
                                    <CommandEmpty>No provider found</CommandEmpty>
                                    <CommandGroup>
                                        {PROVIDERS.map((provider) => (
                                            <CommandItem
                                                key={provider}
                                                value={provider}
                                                onSelect={(currentValue) => {
                                                    setSelectedProvider(currentValue.toUpperCase());

                                                    setSelectedConnectionId("");
                                                    setConfiguration({
                                                        ...configuration,
                                                        resourceId: null,
                                                        metricType: null,
                                                    });

                                                    setProviderOpen(false);
                                                }}
                                            >
                                                <Check
                                                    className={cn(
                                                        "mr-2 h-4 w-4",
                                                        selectedProvider === provider
                                                            ? "opacity-100"
                                                            : "opacity-0"
                                                    )}
                                                />
                                                {provider}
                                            </CommandItem>
                                        ))}
                                    </CommandGroup>
                                </CommandList>
                            </Command>
                        </PopoverContent>
                    </Popover>
                </div>

                <div className="grid gap-2">
                    <Label>Connection</Label>
                    <Popover open={connectionOpen} onOpenChange={setConnectionOpen}>
                        <PopoverTrigger asChild>
                            <Button
                                variant="outline"
                                role="combobox"
                                aria-expanded={connectionOpen}
                                className="justify-between w-full"
                            >
                                <span className="truncate">
                                    {selectedConnectionId
                                        ? (connections.find((c) => c.id === selectedConnectionId)
                                              ?.displayName ?? "Select connection...")
                                        : "Select a connection..."}
                                </span>
                                <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                            </Button>
                        </PopoverTrigger>
                        <PopoverContent className="p-0 w-(--radix-popover-trigger-width)">
                            <Command>
                                <CommandInput placeholder="Search connections..." />
                                <CommandList>
                                    <CommandEmpty>No connections found.</CommandEmpty>
                                    <CommandGroup>
                                        {connections.map((conn) => (
                                            <CommandItem
                                                key={conn.id}
                                                value={conn.id}
                                                onSelect={(currentValue) => {
                                                    setSelectedConnectionId(currentValue);
                                                    setConfiguration({
                                                        ...configuration,
                                                        resourceId: null,
                                                        metricType: null,
                                                    });
                                                    setConnectionOpen(false);
                                                }}
                                            >
                                                <Check
                                                    className={cn(
                                                        "mr-2 h-4 w-4",
                                                        selectedConnectionId === conn.id
                                                            ? "opacity-100"
                                                            : "opacity-0"
                                                    )}
                                                />
                                                {conn.displayName}
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
