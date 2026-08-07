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
import Dropdown from "@/components/molecules/dropdown";
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
                    <Dropdown
                        value={selectedProvider}
                        options={PROVIDERS.map((provider) => ({
                            value: provider,
                            label: provider,
                        }))}
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
                        disableSearch={true}
                        widthVariant="full"
                        placeholder="select provider..."
                    />
                </div>
                <div className="grid gap-2">
                    <Label>Connection</Label>
                    <Dropdown
                        value={selectedConnectionId}
                        options={connections.map((connection) => ({
                            value: connection.id,
                            label: connection.displayName,
                        }))}
                        onSelect={(currentValue) => {
                            setSelectedConnectionId(currentValue);
                            setConfiguration({
                                ...configuration,
                                resourceId: null,
                                metricType: null,
                            });
                            setConnectionOpen(false);
                        }}
                        widthVariant="full"
                        placeholder="select connection..."
                    />
                </div>
            </FieldGroup>
        </FieldSet>
    );
}
